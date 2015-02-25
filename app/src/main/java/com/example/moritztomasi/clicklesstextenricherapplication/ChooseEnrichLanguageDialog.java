package com.example.moritztomasi.clicklesstextenricherapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ChooseEnrichLanguageDialog extends DialogFragment {

    private ChooseEnrichLanguageListener enrichLanguageListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            enrichLanguageListener = (ChooseEnrichLanguageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChooseEnrichLanguageListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] choices = {"Yes", "No"};

        builder.setTitle("ENRICH")
                .setItems(choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        enrichLanguageListener.onEnrichDialogClick(which);
                    }
                });

        return builder.create();
    }

    public interface ChooseEnrichLanguageListener {
        public void onEnrichDialogClick(int which);
    }
}
