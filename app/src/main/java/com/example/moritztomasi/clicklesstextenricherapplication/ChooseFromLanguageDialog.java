package com.example.moritztomasi.clicklesstextenricherapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

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
        LayoutInflater inflater = getActivity().getLayoutInflater();

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
