package com.example.moritztomasi.clicklesstextenricherapplication.dialogues;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 *
 */
public class ChooseToLanguageDialog extends DialogFragment {

    /**
     *
     */
    private ChooseToLanguageListener toLanguageListener;

    /**
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            toLanguageListener = (ChooseToLanguageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChooseToLanguageListener");
        }
    }

    /**
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] languages = {"English", "German", "Italian"};

        builder.setTitle("TRANSLATE FROM")
               .setItems(languages, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       toLanguageListener.onToLanguageDialogClick(which);
                   }
               });

        return builder.create();
    }

    /**
     *
     */
    public interface ChooseToLanguageListener {

        /**
         * 
         * @param which
         */
        public void onToLanguageDialogClick(int which);
    }
}
