# Android Form EditText - With TextView-to-EditText hides

fork of vekexasia/android-edittext-validator


STATUS: Incomplete for XML layouts, only tested for programatic EditText creation. My App is still early in development and I'm short on time. I'm only using this library to programaticaly created views.  Feel free to test and provide tips, code or other feedback.


# TextView-to-EditText hides

PURPOSE: Conserve screen space by showing a TextView in place of the EditText until touched.  This is an optional feature of the EditText-validator. It's yoru choice to select the DormantFormEditText instead of FormEditText and the on-touch switching should work automatically.

NOTE: DormantFormEditText started out wrapping both the TextView and EditText in a ViewFlapper with the intelligent animation system to show the transition in and out.  However, the ViewFlapper seems to consume width with layout wrap_content - which defeats the entire purpose of my hiding the EditText until it is needed by the user.  You can review older commits for the ViewFlapper implementation.  The end result: DormantFormEditText is now using a LinearLayout and manually triggering the animations.

ToDo: the focus loss isn't working perfectly when multiple DormantFormEditText are on screen at the same time. The bug may have existed in the original FormEditText library I forked from - but was not as visually obvious. Out of time to devise a solution. Help wanted!  opened issue: https://github.com/vekexasia/android-edittext-validator/issues/32

# Usage - Programatically

Simple example. You are in the onCreate of an Activity - and your ViewGroup (layout) is in the variable rootViewGroup.  Add a new smart EditText to your rootView:

      DormantFormEditText newEditText = new com.andreabaccega.widget.DormantFormEditText(this);
      newEditText.setText("This is the text user will edit");
      # -- Note when adding to the layout you likely inflated: go into the object and pull the viewGroup that was created to hold the two items - not the DormantFormEditText you just instantiated.
      rootViewGroup.addView(newEditText.controllingViewGrouper);
