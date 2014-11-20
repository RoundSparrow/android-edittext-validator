package com.andreabaccega.widget;

import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2014-11-20
 * Class code is (c) Copyright 2014 Stephen A. Gutknecht. All rights reserved.
 *
 * @author Stephen A. Gutknecht
 */
public class DormantTracking {
    public static boolean settingAnimationsType1Enabled = true;
    public static boolean settingOnlyOneEditAtTimeForceOff = true;
    public static boolean settingHeavyLoggingSwappingViews = false;

    public static DormantFormEditText lastEditedControlGroup = null;

    /*
    So confused over lifecycle of View class object, creating a super firewall - all vars are only from the parameter.
    Probably caused by a single View self-creating a ViewGroup parent. Or some obvious bug I keep overlooking ;)
     */
    public static View.OnClickListener commonListener = new View.OnClickListener() {
        @Override
        public void onClick(final View textViewTouched) {
            // Method just sent us the precise view, so use it.
            final LinearLayout parentViewGrouper = (LinearLayout) textViewTouched.getParent();
            if (parentViewGrouper != null) {
                // By logic, the TextView was this listener, so it had to have been the visible partner for this code path to be hit.
                final DormantFormEditText neighborPartnerEditText = (DormantFormEditText) parentViewGrouper.getChildAt(0);

                if (settingHeavyLoggingSwappingViews)
                    android.util.Log.d("DormantEdit", "SecondTry onClick for TextView count: " + DormantTracking.createCallCount + "?" + neighborPartnerEditText.getMyInstanceIndex());

                neighborPartnerEditText.onReadOnlyTextViewMode = false;

                /*
                Unable to determine why, but onFocusChange is not being called consistently...
                Issue confirmation? http://stackoverflow.com/questions/9427506/onfocuschange-not-always-working
                */
                if (DormantTracking.settingOnlyOneEditAtTimeForceOff) {
                    if (DormantTracking.lastEditedControlGroup != null)
                    {
                        // Make the previous one go away!
                        if (settingHeavyLoggingSwappingViews)
                            android.util.Log.d("DormantEdit", "forcing hide of lastEditedControlGroup call: " + DormantTracking.createCallCount + ":" + DormantTracking.lastEditedControlGroup.getMyInstanceIndex());
                        DormantTracking.lastEditedControlGroup.executeOnFocusLoss(DormantTracking.lastEditedControlGroup, "hide previous " + DormantTracking.lastEditedControlGroup.getMyInstanceIndex());
                    }
                    else
                    {
                        if (settingHeavyLoggingSwappingViews)
                            android.util.Log.d("DormantEdit", "null for lastEditedControlGroup call: " + DormantTracking.createCallCount + ":" + neighborPartnerEditText.getMyInstanceIndex());
                    }
                    DormantTracking.lastEditedControlGroup = neighborPartnerEditText;
                }

                if (DormantTracking.settingAnimationsType1Enabled) {
                    textViewTouched.startAnimation(DormantTracking.commonAnimationOut);

                    // I am a bit lost how the animation listener can know which view to hide in onAnimationEnd - so let's just directly tell the view to hide itself
                    // The downside to this approach is it relies on precise timing.
                    textViewTouched.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // hide the TextView
                            textViewTouched.setVisibility(View.GONE);
                            // Show the DormantFormEditText
                            neighborPartnerEditText.setVisibility(View.VISIBLE);
                            // Make it focused
                            neighborPartnerEditText.requestFocus();
                            // Animate it into visibility
                            neighborPartnerEditText.startAnimation(DormantTracking.commonAnimationIn);
                        }
                    }, 1300L /* Animation is 1200ms, so give system extra time */);
                } else {
                    // hide the TextView
                    textViewTouched.setVisibility(View.GONE);
                    // Show the DormantFormEditText
                    neighborPartnerEditText.setVisibility(View.VISIBLE);
                    // Make it focused
                    neighborPartnerEditText.requestFocus();
                }
            }
        }
    };

    public static Animation commonAnimationIn = null;
    public static Animation commonAnimationOut = null;

    public static AtomicInteger createCallCount = new AtomicInteger(0);
}
