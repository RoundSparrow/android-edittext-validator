package com.andreabaccega.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.andreabaccega.formedittextvalidator.Validator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * EditText Extension to be used in order to create forms in android.
 *
 * Portions (c) Copyright 2014 Stephen A. Gutknecht. All rights reserved.
 *
 * @author Andrea Baccega <me@andreabaccega.com>
 * @author Stephen A. Gutknecht
 */
public class DormantFormEditText extends EditText {

    public TextView partnerTextView;
    public ViewSwitcher controllingViewSwitcher;
    public static OnClickListener commonListener = null;
    public boolean onReadOnlyTextViewMode = true;


    public void fakeTextViewWhileInactive()
    {
        partnerTextView = new TextView(this.getContext());
        controllingViewSwitcher = new ViewSwitcher(this.getContext());

        controllingViewSwitcher.addView(this);
        controllingViewSwitcher.addView(partnerTextView);
        // Start on the TextView
        controllingViewSwitcher.showNext();
        onReadOnlyTextViewMode = true;

        if (commonListener == null)
        {
            commonListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Method just sent us the precise view, so use it.
                    ViewSwitcher parentViewSwitcher = (ViewSwitcher) v.getParent();
                    if (parentViewSwitcher != null) {
                        // By logic, the TextView was this listener, so it had to have been the visible partner for this code path to be hit.
                        onReadOnlyTextViewMode = false;
                        parentViewSwitcher.showPrevious();
                    }
                }
            };
        }

        partnerTextView.setOnClickListener(commonListener);
    }


    public DormantFormEditText(Context context) {
        super(context);
        //support dynamic new FormEditText(context)
        editTextValidator = new DefaultEditTextValidator(this, context);
        fakeTextViewWhileInactive();
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        // Populate the partner
        if (partnerTextView != null)
            partnerTextView.setText(text, type);
    }


    public DormantFormEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        editTextValidator = new DefaultEditTextValidator(this, attrs, context);
        fakeTextViewWhileInactive();
    }

    public DormantFormEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        editTextValidator = new DefaultEditTextValidator(this, attrs, context);
        fakeTextViewWhileInactive();
    }

    /**
     * Add a validator to this FormEditText. The validator will be added in the
     * queue of the current validators.
     *
     * @param theValidator
     * @throws IllegalArgumentException
     *             if the validator is null
     */
    public void addValidator(Validator theValidator) throws IllegalArgumentException {
        editTextValidator.addValidator(theValidator);
    }

    public EditTextValidator getEditTextValidator() {
        return editTextValidator;
    }

    public void setEditTextValidator(EditTextValidator editTextValidator) {
        this.editTextValidator = editTextValidator;
    }

    /**
     * Calling *testValidity()* will cause the EditText to go through
     * customValidators and call {@link com.andreabaccega.formedittextvalidator.Validator#isValid(android.widget.EditText)}
     *
     * @return true if the validity passes false otherwise.
     */
    public boolean testValidity() {
        return editTextValidator.testValidity();
    }

    private EditTextValidator editTextValidator;


    /**
     * Keep track of which icon we used last
     */
    private Drawable lastErrorIcon = null;

    /**
     * Don't send delete key so edit text doesn't capture it and close error
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (TextUtils.isEmpty(getText().toString())
                && keyCode == KeyEvent.KEYCODE_DEL)
            return true;
        else
            return super.onKeyPreIme(keyCode, event);
    }

    /**
     * Resolve an issue where the error icon is hidden under some cases in JB
     * due to a bug http://code.google.com/p/android/issues/detail?id=40417
     */
    @Override
    public void setError(CharSequence error, Drawable icon) {
        super.setError(error, icon);
        lastErrorIcon = icon;

        // if the error is not null, and we are in JB, force
        // the error to show
        if (error != null /* !isFocused() && */) {
            showErrorIconHax(icon);
        }
    }


    /**
     * In onFocusChanged() we also have to reshow the error icon as the Editor
     * hides it. Because Editor is a hidden class we need to cache the last used
     * icon and use that
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        showErrorIconHax(lastErrorIcon);
        // populate the partner
        partnerTextView.setText(this.getText());
        // Did we just have loss of focus?
        if (! focused)
        {
            onReadOnlyTextViewMode = false;
            this.controllingViewSwitcher.showPrevious();
        }
    }



    /**
     * Use reflection to force the error icon to show. Dirty but resolves the
     * issue in 4.2
     */
    private void showErrorIconHax(Drawable icon) {
        if (icon == null)
            return;

        // only for JB 4.2 and 4.2.1
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN_MR1)
            return;

        try {
            Class<?> textview = Class.forName("android.widget.TextView");
            Field tEditor = textview.getDeclaredField("mEditor");
            tEditor.setAccessible(true);
            Class<?> editor = Class.forName("android.widget.Editor");
            Method privateShowError = editor.getDeclaredMethod("setErrorIcon",
                    Drawable.class);
            privateShowError.setAccessible(true);
            privateShowError.invoke(tEditor.get(this), icon);
        } catch (Exception e) {
            // e.printStackTrace(); // oh well, we tried
        }
    }
}
