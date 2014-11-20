# Android Form EditText - With TextView-to-EditText hides

fork of vekexasia/android-edittext-validator


STATUS: Incomplete for XML layouts. My App is still early in development and I'm short on time. I'm only using this library to programaticaly create EditText views.  Feel free to test and share waht needs to work on in-XML layout.


# TextView-to-EditText hides

Conserve screen space by showing a TextView in place of the EditText until touched.  This is an optional feature, select the DormantFormEditText instead of FormEditText and it should work automatically.  A ViewFlapper is used, and an animation shows the transition in and out.

ToDo: the focus loss isn't working perfectly when multiple DormantFormEditText are on screen at the same time. The bug may have existed in the original FormEditText library I forked from - but was not as visually obvious. Out of time to devise a solution. Help wanted!

# Usage - Programatically

Simple example. You are in the onCreate of an Activity - and your ViewGroup (layout) is in the variable rootViewGroup.

      DormantFormEditText newEntry = new com.andreabaccega.widget.DormantFormEditText(this);
      newEntry.controlIndexID.setText("This is the text user will edit");
      # -- Note: go into the object and reference the controllingViewFlipper - not the DormantFormEditText you just originated.
      rootViewGroup.addView(newEntry.controlIndexID.controllingViewFlipper);
