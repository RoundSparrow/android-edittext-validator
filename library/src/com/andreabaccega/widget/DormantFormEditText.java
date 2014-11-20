package com.andreabaccega.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private int myInstanceIndex = -1;
    private TextView partnerTextView;
    private LinearLayout controllingViewGrouper;

    public int getMyInstanceIndex()
    {
        return this.myInstanceIndex;
    }

    public LinearLayout getControllingViewGrouper()
    {
        return this.controllingViewGrouper;
    }


    public boolean onReadOnlyTextViewMode = true;


    public void createTextViewWhileEditInactive()
    {
        // Start on the TextView, hide self on creation.
        this.setVisibility(GONE);

        final DormantFormEditText mySelf = this;
        final int myIndex = DormantTracking.createCallCount.getAndIncrement();
        mySelf.myInstanceIndex = myIndex;

        this.partnerTextView = new TextView(this.getContext());
        this.partnerTextView.setId(R.id.dormantFormEditText_partnerTextView);
        this.controllingViewGrouper = new LinearLayout(this.getContext());
        this.controllingViewGrouper.setId(R.id.dormantFormEditText_controllingViewGroup);

        // Index order is: EditText view first, TextView second
        this.controllingViewGrouper.addView(this);
        this.controllingViewGrouper.addView(partnerTextView);

        onReadOnlyTextViewMode = true;

        if (DormantTracking.settingAnimationsType1Enabled) {
            // Optimize to not load hundreds of these ;)
            if (DormantTracking.commonAnimationIn == null) {
                DormantTracking.commonAnimationIn = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_in_1200ms);
                DormantTracking.commonAnimationOut = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_out_1200ms);
                // we have no specific View to make invisible at end? commonAnimationOut.setAnimationListener(commonAnimationOutListener);
            }
        }

        partnerTextView.setOnClickListener(DormantTracking.commonListener);
    }


    public DormantFormEditText(Context context) {
        super(context);
        createTextViewWhileEditInactive();
    }

    public DormantFormEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        createTextViewWhileEditInactive();
    }

    public DormantFormEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createTextViewWhileEditInactive();
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
            android.util.Log.d("DormantEdit", "onFocusChanged no longer focused? count: " + DormantTracking.createCallCount + ":" + this.getMyInstanceIndex());
            executeOnFocusLoss(this, "onFocusedChange was here");
        }
        else
        {
            android.util.Log.d("DormantEdit", "onFocusChanged incoming focus? count: " + DormantTracking.createCallCount + ":" + this.getMyInstanceIndex());
        }
    }


    protected void executeOnFocusLoss(final DormantFormEditText thisView, final String inCallerNote)
    {
        final DormantFormEditText stableSelf = thisView;
        final View stablePartner = thisView.partnerTextView;
        stableSelf.onReadOnlyTextViewMode = false;

        if (DormantTracking.settingAnimationsType1Enabled) {
            stableSelf.startAnimation(DormantTracking.commonAnimationOut);
            stableSelf.postDelayed(new Runnable() {
                @Override
                public void run() {
                    android.util.Log.d("DormantEdit", "executeOnFocusLoss changing out views. count: " + DormantTracking.createCallCount + ":" + stableSelf.getMyInstanceIndex() + " note: " + inCallerNote);
                    stableSelf.setVisibility(GONE);
                    stablePartner.setVisibility(VISIBLE);
                    stablePartner.startAnimation(DormantTracking.commonAnimationIn);
                }
            }, 1300L);
        }
        else
        {
            stableSelf.setVisibility(GONE);
            stablePartner.setVisibility(VISIBLE);
        }

        if (DormantTracking.settingOnlyOneEditAtTimeForceOff) {
            android.util.Log.d("DormantEdit", "nulling out lastEditedControlGroup count: " + DormantTracking.createCallCount + ":" + thisView.getMyInstanceIndex());
            DormantTracking.lastEditedControlGroup = null;
        }
    }
}
