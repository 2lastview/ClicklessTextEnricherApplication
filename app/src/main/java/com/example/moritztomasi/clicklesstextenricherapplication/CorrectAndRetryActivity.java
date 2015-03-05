package com.example.moritztomasi.clicklesstextenricherapplication;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moritztomasi.clicklesstextenricherapplication.common.Enrich;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SupportException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ValidationException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class CorrectAndRetryActivity extends Activity implements
        ChooseToLanguageDialog.ChooseToLanguageListener,
        ChooseEnrichLanguageDialog.ChooseEnrichLanguageListener,
        AsyncResponse {

    private static final String CLASS_TAG = "CorrectAndRetryActivity";

    private Intent intent;

    private String target;
    private String text;

    private String fromLanguage;
    private String toLanguage;
    private String imagePath;
    private File imageFile;
    private Boolean enrich;

    private EditText editText;
    private Button toButton;
    private Button enrichButton;

    private HashMap<String, String> languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct_and_retry);

        this.languages = new HashMap<String, String>();
        this.languages.put("eng", "English");
        this.languages.put("deu", "German");
        this.languages.put("ita", "Italian");

        this.intent = getIntent();

        this.fromLanguage = intent.getExtras().getString("SOURCE_LANGUAGE");
        this.toLanguage = intent.getExtras().getString("TARGET_LANGUAGE");
        this.imagePath = intent.getExtras().getString("IMAGE_PATH");
        this.imageFile = (File) intent.getExtras().get("IMAGE_FILE");

        this.editText = (EditText) findViewById(R.id.edit_original_editText);
        this.toButton = (Button) findViewById(R.id.to_language_button);
        this.enrichButton = (Button) findViewById(R.id.enrich_language_button);

        this.target = intent.getExtras().getString("TARGET_LANGUAGE");
        this.text = intent.getExtras().getString("TEXT");
        this.enrich = intent.getExtras().getBoolean("ENRICHED");

        this.editText.setText(text);
        this.toButton.setText("TO\n" + this.languages.get(target));

        if(enrich) this.enrichButton.setText("ENRICH\nYes");
        else this.enrichButton.setText("ENRICH\nNo");
    }

    /** TRANSLATION SETTINGS **/

    public void showOriginalImage(View view) {
        String imagePath = intent.getExtras().getString("IMAGE_PATH");

        Intent showImageIntent = new Intent(Intent.ACTION_VIEW);
        showImageIntent.setDataAndType(Uri.parse(imagePath), "image/*");
        startActivity(Intent.createChooser(showImageIntent, "Chosen image"));
    }

    public void showTranslateToDialog(View view) {
        DialogFragment dialog = new ChooseToLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseToLanguageDialog");
    }

    public void showEnrichDialog(View view) {
        DialogFragment dialog = new ChooseEnrichLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseEnrichLanguageDialog");
    }

    @Override
    public void onToLanguageDialogClick(int which) {
        switch(which) {
            case 0: this.toLanguage = "eng";
                    this.toButton.setText("TO\n" + "English");
                    break;
            case 1: this.toLanguage = "deu";
                    this.toButton.setText("TO\n" + "German");
                    break;
            case 2: this.toLanguage = "ita";
                    this.toButton.setText("TO\n" + "Italian");
                    break;
        }
    }

    @Override
    public void onEnrichDialogClick(int which) {
        switch(which) {
            case 0: this.enrich = true;
                    this.enrichButton.setText("ENRICH\n" + "Yes");
                    break;
            case 1: this.enrich = false;
                    this.enrichButton.setText("ENRICH\n" + "No");
                    break;
        }
    }

    /** /TRANSLATION SETTINGS **/

    /** GO TO TRANSLATION AND ENRICHMENT **/

    public void go(View view) {

        try {
            Enrich enrich = new Enrich();
            enrich.enrichFromImage(this,
                    this.fromLanguage,
                    this.toLanguage,
                    this.enrich,
                    this.imagePath,
                    this.editText.getText().toString());
        }
        catch(ValidationException e) {
            Log.d(CLASS_TAG, e.getMessage());
            showToast(e.getMessage());
            return;
        }
        catch (SupportException e) {
            Log.d(CLASS_TAG, e.getMessage());
            showToast(e.getMessage());
            return;
        }
    }

    @Override
    public void postFinish(String response) {
        JSONObject json = null;
        try {
            json = new JSONObject(response);
        } catch (JSONException e) {
            Log.d(CLASS_TAG, "Exception while putting in JSONObject");
            e.printStackTrace();
        }

        String detected = "";
        String text = "";
        String translation = "";
        try {
            if(json.has("error")) {
                showToast(json.getString("error"));
                return;
            }

            if(json.has("detected")) detected = json.getString("detected");
            if(json.has("text")) text = json.getString("text");
            if(json.has("translation")) translation = json.getString("translation");
        } catch (JSONException e) {
            Log.d(CLASS_TAG, "Exception while checking JSONObject");
        }

        Intent intent = new Intent(this, ShowTranslationActivity.class);

        intent.putExtra("SOURCE_LANGUAGE", this.fromLanguage);
        intent.putExtra("TARGET_LANGUAGE", this.toLanguage);
        intent.putExtra("DETECTED_LANGUAGE", detected);
        intent.putExtra("TEXT", text);
        intent.putExtra("TRANSLATION", translation);
        intent.putExtra("ENRICHED", this.enrich);
        intent.putExtra("IMAGE_PATH", this.imagePath);
        intent.putExtra("IMAGE_FILE", this.imageFile);

        startActivity(intent);
    }

    /** /GO TO TRANSLATION AND ENRICHMENT **/

    private void showToast(String text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

        toast.show();
    }
}
