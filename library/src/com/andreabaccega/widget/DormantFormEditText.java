package com.andreabaccega.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.andreabaccega.formedittext.R;

/**
 * EditText Extension to be used in order to create forms in android.
 *
 * Portions (c) Copyright 2014 Stephen A. Gutknecht. All rights reserved.
 *
 * Warning: "EditText is perhaps the #1 most-hacked widget by device manufacturers (e.g., implementing their own long-click logic for a not-quite context menu). While your technique will probably work on many devices, I would not trust it across the board, particularly since the alternative (see accepted answer) is not especially difficult to use. –  CommonsWare Nov 9 '12 at 15:37"
 *   Probably best to use parallel and avoid getting too deep into EditText layout.
 *   ViewSwitcher likely inferior: "ViewFlipper supports more than two and has extra features, such as animated transitions between them. I have only used ViewFlipper"
 *
 * // ToDo: bug on focus loss - it doesn't always detect focus blur - and run the desired logic. Did this bug exist in the original FormEditText - but just not as obvious to visually see?
 * //       for my purposes, I'm out of time - so the bug isn't causing any real issue - as the use case for my App is that most of the time the Views are never altered/touched.
 *
 * @author Andrea Baccega <me@andreabaccega.com>
 * @author Stephen A. Gutknecht
 */
public class DormantFormEditText extends FormEditText {

    public TextView partnerTextView;
    public ViewFlipper controllingViewFlipper;
    public static OnClickListener commonListener = null;
    public boolean onReadOnlyTextViewMode = true;
    public static Animation commonAnimationIn = null;
    public static Animation commonAnimationOut = null;
    public static OnFocusChangeListener secondaryFocusChangeListner = null;


    public void fakeTextViewWhileInactive()
    {
        partnerTextView = new TextView(this.getContext());
        controllingViewFlipper = new ViewFlipper(this.getContext());

        controllingViewFlipper.addView(this);
        controllingViewFlipper.addView(partnerTextView);
        // Start on the TextView
        controllingViewFlipper.showNext();
        onReadOnlyTextViewMode = true;

        // Optimize to not load hundreds of these ;)
        if (commonAnimationIn == null)
        {
            commonAnimationIn  = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_in_1200ms);
            commonAnimationOut = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_out_1200ms);
        }

        controllingViewFlipper.setInAnimation(commonAnimationIn);
        controllingViewFlipper.setOutAnimation(commonAnimationOut);

        // Optimize to not load hundreds of these ;)
        if (commonListener == null)
        {
            commonListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Method just sent us the precise view, so use it.
                    ViewFlipper parentViewSwitcher = (ViewFlipper) v.getParent();
                    if (parentViewSwitcher != null) {
                        // By logic, the TextView was this listener, so it had to have been the visible partner for this code path to be hit.
                        onReadOnlyTextViewMode = false;
                        parentViewSwitcher.showPrevious();
                    }
                }
            };
        }

        partnerTextView.setOnClickListener(commonListener);

        // The internal onFocusedChange out isn't always calling
        if (secondaryFocusChangeListner == null)
        {
            secondaryFocusChangeListner = new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (! hasFocus)
                    {
                        // Method just sent us the precise view, so use it.
                        ViewFlipper parentViewSwitcher = (ViewFlipper) v.getParent();
                        if (parentViewSwitcher != null) {
                            DormantFormEditText mySelfAsParam = (DormantFormEditText) v;
                            mySelfAsParam.executeOnFocusLoss();
                        }
                    }
                }
            };
        }

        this.setOnFocusChangeListener(secondaryFocusChangeListner);
    }


    public DormantFormEditText(Context context) {
        super(context);
        fakeTextViewWhileInactive();
    }

    public DormantFormEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        fakeTextViewWhileInactive();
    }

    public DormantFormEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

        // populate the partner
        partnerTextView.setText(this.getText());
        // Did we just have loss of focus?
        if (! focused)
        {
            executeOnFocusLoss();
        }
    }


    protected void executeOnFocusLoss()
    {
        onReadOnlyTextViewMode = false;
        this.controllingViewFlipper.showPrevious();
    }
}
