package com.andreabaccega.widget;

import android.view.View;
import android.view.animation.Animation;

/**
 * Created by adminsag on 11/20/14.
 */
public class DormantTracking {
    public static boolean settingAnimationsType1Enabled = true;
    public static boolean settingOnlyOneEditAtTimeForceOff = true;

    public static DormantFormEditText lastCreatedControl = null;

    public static View.OnClickListener commonListener = null;

    public static Animation commonAnimationIn = null;
    public static Animation commonAnimationOut = null;

    public static int createCallCount = 0;
}
