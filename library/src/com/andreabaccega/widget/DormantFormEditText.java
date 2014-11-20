package com.andreabaccega.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
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

    public DormantFormEditText mySelf = null;
    public TextView partnerTextView;
    public LinearLayout controllingViewGrouper;

    public boolean onReadOnlyTextViewMode = true;


    public void createTextViewWhileEditInactive()
    {
        mySelf = this;
        DormantTracking.createCallCount++;

        /*
        Unable to determine why, but onFocusChange is not being called consistently...
        Issue confirmation? http://stackoverflow.com/questions/9427506/onfocuschange-not-always-working
         */
        if (DormantTracking.settingOnlyOneEditAtTimeForceOff) {
            if (DormantTracking.lastCreatedControl != null)
            {
                // Make the previous one go away!
                android.util.Log.d("DormantEdit", "forcing hide of lastCreatedControl call: " + DormantTracking.createCallCount);
                DormantTracking.lastCreatedControl.executeOnFocusLoss();
            }
            else
            {
                android.util.Log.d("DormantEdit", "null for lastCreatedControl call: " + DormantTracking.createCallCount);
            }
            DormantTracking.lastCreatedControl = this;
        }

        partnerTextView = new TextView(this.getContext());
        partnerTextView.setId(R.id.dormantFormEditText_partnerTextView);
        controllingViewGrouper = new LinearLayout(this.getContext());
        controllingViewGrouper.setId(R.id.dormantFormEditText_controllingViewGroup);

        // Index order is: EditText view first, TextView second
        controllingViewGrouper.addView(this);
        controllingViewGrouper.addView(partnerTextView);

        // Start on the TextView
        this.setVisibility(GONE);
        onReadOnlyTextViewMode = true;

        if (DormantTracking.settingAnimationsType1Enabled) {
            // Optimize to not load hundreds of these ;)
            if (DormantTracking.commonAnimationIn == null) {
                DormantTracking.commonAnimationIn = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_in_1200ms);
                DormantTracking.commonAnimationOut = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_out_1200ms);
                // we have no specific View to make invisible at end? commonAnimationOut.setAnimationListener(commonAnimationOutListener);
            }
        }

        // Optimize to not load hundreds of these ;)
        if (DormantTracking.commonListener == null)
        {
            DormantTracking.commonListener = new OnClickListener() {
                @Override
                public void onClick(final View textViewTouched) {
                    // Method just sent us the precise view, so use it.
                    final LinearLayout parentViewGrouper = (LinearLayout) textViewTouched.getParent();
                    if (parentViewGrouper != null) {
                        // By logic, the TextView was this listener, so it had to have been the visible partner for this code path to be hit.
                        final DormantFormEditText neighborPartnerEditText = (DormantFormEditText) parentViewGrouper.getChildAt(0);
                        neighborPartnerEditText.onReadOnlyTextViewMode = false;

                        if (DormantTracking.settingAnimationsType1Enabled) {
                            textViewTouched.startAnimation(DormantTracking.commonAnimationOut);

                            // I am a bit lost how the animation listener can know which view to hide in onAnimationEnd - so let's just directly tell the view to hide itself
                            // The downside to this approach is it relies on precise timing.
                            textViewTouched.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // hide the TextView
                                    textViewTouched.setVisibility(GONE);
                                    // Show the DormantFormEditText
                                    neighborPartnerEditText.setVisibility(VISIBLE);
                                    neighborPartnerEditText.startAnimation(DormantTracking.commonAnimationIn);
                                }
                            }, 1300L /* Animation is 1200ms, so give system extra time */);
                        }
                        else
                        {
                            // hide the TextView
                            textViewTouched.setVisibility(GONE);
                            // Show the DormantFormEditText
                            neighborPartnerEditText.setVisibility(VISIBLE);
                        }
                    }
                }
            };
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
            executeOnFocusLoss();
        }
    }


    protected void executeOnFocusLoss()
    {
        onReadOnlyTextViewMode = false;

        final View stableSelf = this;
        final View stablePartner = partnerTextView;

        if (DormantTracking.settingAnimationsType1Enabled) {
            this.startAnimation(DormantTracking.commonAnimationOut);
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
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
            android.util.Log.d("DormantEdit", "nulling out lastCreatedControl count: " + DormantTracking.createCallCount);
            DormantTracking.lastCreatedControl = null;
        }
    }
}
