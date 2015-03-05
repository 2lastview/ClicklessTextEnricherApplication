package com.example.moritztomasi.clicklesstextenricherapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

public class ShowTranslationActivity extends Activity {

    private static final String CLASS_TAG = "ShowTranslationActivity";

    private Intent intent;

    private String source;
    private String target;
    private String detected;
    private String text;
    private String translation;

    private TextView originalTitle;
    private TextView originalText;
    private TextView translationTitle;
    private TextView translationText;

    private HashMap<String, String> languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_translation);

        this.languages = new HashMap<String, String>();
        this.languages.put("eng", "English");
        this.languages.put("deu", "German");
        this.languages.put("ita", "Italian");
        this.languages.put("unk", "Unknown");

        this.intent = getIntent();

        this.source = intent.getExtras().getString("SOURCE_LANGUAGE");
        this.target = intent.getExtras().getString("TARGET_LANGUAGE");
        this.detected = intent.getExtras().getString("DETECTED_LANGUAGE");
        this.text = intent.getExtras().getString("TEXT");
        this.translation = intent.getExtras().getString("TRANSLATION");

        this.originalTitle = (TextView) findViewById(R.id.original_title_textView);
        this.originalText = (TextView) findViewById(R.id.original_textView);
        this.translationTitle = (TextView) findViewById(R.id.translation_title_textView);
        this.translationText = (TextView) findViewById(R.id.translation_textView);

        this.originalTitle.setText("ORIGINAL " + this.languages.get(source));
        if(!detected.equals("")) this.originalTitle.setText("ORIGINAL " + this.languages.get(source) + "/" + detected);

        this.originalText.setText(text);
        this.translationTitle.setText("TRANSLATION " + this.languages.get(target));
        this.translationText.setText(translation);
    }

    public void correctAndRetry(View view) {
        this.intent.setClass(this, CorrectAndRetryActivity.class);
        startActivity(this.intent);
    }
}
