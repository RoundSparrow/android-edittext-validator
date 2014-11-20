# Android Form EditText - With TextView-to-EditText hides

fork of vekexasia/android-edittext-validator


STATUS: Incomplete for XML layouts. My App is still early in development and I'm short on time. I'm only using this library to programaticaly create EditText views.  Feel free to test and share waht needs to work on in-XML layout.


# TextView-to-EditText hides

Conserve screen space by showing a TextView in place of the EditText until touched.  This is an optional feature, select the DormantFormEditText instead of FormEditText and it should work automatically.

NOTE: I started out wrapping both the TextView and EditText in a ViewFlapper with the intelligent animation system to show the transition in and out.  However, the ViewFlapper seems to consume width with layout wrap_content - which defeats the entire purpose of my hiding the EditText until it is needed.  You can review older commits for the ViewFlapper implementation.  The end result: now using a LinearLayout and manually triggering the animations.

ToDo: the focus loss isn't working perfectly when multiple DormantFormEditText are on screen at the same time. The bug may have existed in the original FormEditText library I forked from - but was not as visually obvious. Out of time to devise a solution. Help wanted!  opened issue: https://github.com/vekexasia/android-edittext-validator/issues/32

# Usage - Programatically

Simple example. You are in the onCreate of an Activity - and your ViewGroup (layout) is in the variable rootViewGroup.

      DormantFormEditText newEditText = new com.andreabaccega.widget.DormantFormEditText(this);
      newEditText.setText("This is the text user will edit");
      # -- Note when adding to the layout you likely inflated: go into the object and pull the viewGroup that was created to hold the two items - not the DormantFormEditText you just instantiated.
      rootViewGroup.addView(newEditText.controllingViewGrouper);
