package com.byagowi.persiancalendar.view.preferences;

import android.os.Build;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

/**
 * Created by ebraminio on 2/21/16.
 */
public class AthanNumericDialog extends EditTextPreferenceDialogFragmentCompat {

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = view.findViewById(android.R.id.edit);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // on platforms supporting direction as LTR direction is more handy on editing numbers
        editText.setTextDirection(View.TEXT_DIRECTION_LTR);
        editText.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }
}
