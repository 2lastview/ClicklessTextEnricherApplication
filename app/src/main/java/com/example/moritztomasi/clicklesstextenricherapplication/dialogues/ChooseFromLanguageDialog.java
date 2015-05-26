/**
 * Copyright 2015 Moritz Tomasi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.example.moritztomasi.clicklesstextenricherapplication.dialogues;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * This is a custom extension of a {@link DialogFragment}. This Dialog is shown if the user wants
 * to choose a source language.
 */
public class ChooseFromLanguageDialog extends DialogFragment {

    private ChooseFromLanguageListener fromLanguageListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            fromLanguageListener = (ChooseFromLanguageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChooseFromLanguageListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] languages = {"English", "German", "Italian", "Unknown"};

        builder.setTitle("TRANSLATE FROM")
               .setItems(languages, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fromLanguageListener.onFromLanguageDialogClick(which);
                    }
                });

        return builder.create();
    }

    public interface ChooseFromLanguageListener {

        public void onFromLanguageDialogClick(int which);
    }
}
