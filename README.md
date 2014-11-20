# Android Form EditText - With TextView-to-EditText hides

fork of vekexasia/android-edittext-validator


# TextView-to-EditText hides

Conserve screen space by showing a TextView in place of the EditText until touched.  This is an optional feature, select the DormantFormEditText instead of FormEditText and it should work automatically.  A ViewFlapper is used, and an animation shows the transition in and out.

ToDo: the focus loss isn't working perfectly when multiple DormantFormEditText are on screen at the same time. The bug may have existed in the original FormEditText library I forked from - but was not as visually obvious. Out of time to devise a solution. Help wanted!
