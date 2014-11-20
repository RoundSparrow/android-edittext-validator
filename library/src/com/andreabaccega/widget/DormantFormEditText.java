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
public class DormantFormEditText extends FormEditText {

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


    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        // Populate the partner
        if (partnerTextView != null)
            partnerTextView.setText(text, type);
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
}
