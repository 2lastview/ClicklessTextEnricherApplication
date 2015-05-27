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

package com.example.moritztomasi.clicklesstextenricherapplication.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moritztomasi.clicklesstextenricherapplication.common.LanguageSupport;
import com.example.moritztomasi.clicklesstextenricherapplication.enrichment.Wikipedia;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SlidingTabLayout;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SupportException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SelectionText;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ValidationException;
import com.example.moritztomasi.clicklesstextenricherapplication.R;
import com.example.moritztomasi.clicklesstextenricherapplication.enrichment.WikipediaResponse;
import com.example.moritztomasi.clicklesstextenricherapplication.enrichment.Wiktionary;
import com.example.moritztomasi.clicklesstextenricherapplication.enrichment.WiktionaryResponse;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

/**
 * This is the activity shown after information is returned from the web service. It is used to
 * display said information. The user is also given the possibility of enriching the gathered
 * information by searching Wikipedia, Wiktionary and Google. The associated layout file is
 * activity_result.
 */
public class ResultActivity extends Activity implements
        SelectionText.SelectionListener,
        WikipediaResponse,
        WiktionaryResponse {

    private static final String CLASS_TAG = "ResultActivity";
    private static final CharSequence TAB_TITLES[] = {"ORIGINAL + TRANSLATION", "IMAGE"};
    private static final int NUM_TABS = 2;

    private EditText originalText;
    private EditText translationText;

    private SlidingUpPanelLayout slidingPaneLayout;

    private Button wikipedia;
    private Button wiktionary;
    private Button google;
    private TextView webTextView;
    private ScrollView webScrollView;

    private String source;
    private String target;
    private String detected;
    private String text;
    private String translation;
    private String imagePath;
    private File imageFile;
    private String selectedText;

    private Intent intent;


    /***************************** INIT *****************************/

    /**
     * First method called in Activity lifecycle. Initializes tabs in {@link SlidingTabLayout} and
     * sets color for tab indicator. The information passed on by the intent is stored in local
     * variables.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(CLASS_TAG, "onCreate in ResultActivity called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ViewPagerAdapter adapter =  new ViewPagerAdapter();

        ViewPager pager = (ViewPager) findViewById(R.id.pager_st);
        pager.setAdapter(adapter);

        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs_st);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(pager);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.rgb(250, 250, 250);
            }
        });

        this.intent = getIntent();

        this.source = intent.getExtras().getString("SOURCE_LANGUAGE");
        this.target = intent.getExtras().getString("TARGET_LANGUAGE");
        this.detected = intent.getExtras().getString("DETECTED_LANGUAGE");
        this.text = intent.getExtras().getString("TEXT");
        this.translation = intent.getExtras().getString("TRANSLATION");
        this.imagePath = intent.getExtras().getString("IMAGE_PATH");
        this.imageFile = (File) intent.getExtras().get("IMAGE_FILE");
    }

    /***************************** /INIT *****************************/


    /***************************** TRANSLATION SETTINGS *****************************/

    /**
     * Show the original image.
     */
    public void showOriginalImage(View view) {
        Log.i(CLASS_TAG, "showOriginalImage in ResultActivity called");

        if(this.imagePath != null && this.imagePath.length() > 0) {
            Intent showImageIntent = new Intent(Intent.ACTION_VIEW);
            showImageIntent.setDataAndType(Uri.parse(this.imagePath), "image/*");
            startActivity(showImageIntent);
        }
        else {
            Log.d(CLASS_TAG, "imagePath cannot be null when showing original image");
            showToast("You have to choose an image.");
        }
    }

    /***************************** /TRANSLATION SETTINGS *****************************/


    /***************************** START RETRY ACTIVITY *****************************/

    /**
     * Starts a {@link RetryActivity}. All the information is passed on using the same
     * {@link Intent}.
     */
    public void correctAndRetry(View view) {
        this.intent.setClass(this, RetryActivity.class);
        startActivity(this.intent);
    }

    /***************************** /START RETRY ACTIVITY *****************************/


    /***************************** ACTUAL ENRICHMENT *****************************/

    /**
     * This method is called by {@link SelectionText} over the interface {@link SelectionText.SelectionListener}
     * every time a word is selected in the corresponding {@link SelectionText} or the selection
     * changes. The selected text is then extracted and stored in a local variable.
     *
     * @param selStart Start index of the selection
     * @param selEnd End index of the selection
     */
    @Override
    public void onSelected(int selStart, int selEnd) {

        if(selStart != selEnd) {
            if(originalText.isFocused()) {
                Log.i(CLASS_TAG, "onSelected in ResultActivity called");
                Log.d(CLASS_TAG, "original text is focused");
                this.selectedText = originalText.getText().toString().substring(selStart, selEnd);
            }
            else if(translationText.isFocused()) {
                Log.i(CLASS_TAG, "onSelected in ResultActivity called");
                Log.d(CLASS_TAG, "translation text is focused");
                this.selectedText = translationText.getText().toString().substring(selStart, selEnd);
            }
        }
    }

    /**
     * Instantiates a {@link Wikipedia} service object and passes source language, target language
     * and the selected text. The next step will be delegated by use of the
     * interface {@link WikipediaResponse} and the method {@link WikipediaResponse#wikipediaFinished(JSONObject)}.
     */
    public void wikipedia(View view) {
        Log.i(CLASS_TAG, "wikipedia in ResultActivity called");

        this.webTextView.setText("Loading...");

        if(translationText.isFocused()) {
            try {
                Log.i(CLASS_TAG, "instantiation of Wikipedia");

                Wikipedia wikipedia = new Wikipedia();
                wikipedia.enrichFromWikipedia(this,
                        this.selectedText,
                        this.source,
                        this.target);
            }
            catch (ValidationException e) {
                Log.d(CLASS_TAG, e.getMessage());
                this.webTextView.setText("No results found");
            }
            catch (SupportException e) {
                Log.d(CLASS_TAG, e.getMessage());
                this.webTextView.setText("No results found");
            }
        }
    }

    /**
     * This method is called by {@link Wikipedia} over the interface
     * {@link WikipediaResponse} as soon as the Wikipedia returns its json response.
     * If the json is null or error is found inside said json, the method returns and shows
     * the error in the TextView. Otherwise an the returned information is displayed in said TextView.
     *
     * @param json Response from Wikipedia in form of a json.
     */
    @Override
    public void wikipediaFinished(JSONObject json) {
        Log.i(CLASS_TAG, "wikipediaFinished in ResultActivity called");

        if(json == null) {
            Log.d(CLASS_TAG, "json cannot be null");
            this.webTextView.setText("No results found");
            return;
        }

        String extract = "";

        try {
            if(json.has("error")) {
                Log.d(CLASS_TAG, json.getString("error"));
                this.webTextView.setText("No results found");
                return;
            }

            if(json.has("query")) {
                JSONObject query = json.getJSONObject("query");
                if(query.has("pages")) {
                    JSONObject pages = query.getJSONObject("pages");
                    Iterator iterator = pages.keys();

                    String id = (String) iterator.next();
                    if(!id.equals("-1")) {
                        JSONObject page = pages.getJSONObject(id);
                        String wikipediaLink = "";
                        if(page.has("pageid")) wikipediaLink = "http://" + this.target.trim() + ".wikipedia.org/?curid=" + page.getString("pageid");
                        if(page.has("title")) extract = "<a href='" + wikipediaLink + "'><b>Wikipedia:</b></a> " + page.getString("title") + "<br/>";
                        if(page.has("extract")) extract += page.getString("extract");

                        extract = extract.replace("\n\n", "\n");
                        extract = extract.replace("\n", "<br/><br/>");
                    }
                    else {
                        Log.d(CLASS_TAG, "No results found");
                        this.webTextView.setText("No results found");
                        return;
                    }
                }
                else {
                    Log.d(CLASS_TAG, "pages is missing in json");
                    this.webTextView.setText("No results found");
                    return;
                }
            }
            else {
                Log.d(CLASS_TAG, "query is missing in json");
                this.webTextView.setText("No results found");
                return;
            }
        } catch (JSONException e) {
            Log.d(CLASS_TAG, "Exception while checking JSONObject");
            this.webTextView.setText("No results found");
            return;
        }

        this.webTextView.setText(Html.fromHtml(extract));
    }

    /**
     * Instantiates a {@link Wiktionary} service object and passes source language, target language
     * and the selected text. The next step will be delegated by use of the
     * interface {@link WiktionaryResponse} and the method {@link WiktionaryResponse#wiktionaryFinished(JSONObject)}.
     */
    public void wiktionary(View view) {
        Log.i(CLASS_TAG, "wiktionary in ResultActivity called");

        this.webTextView.setText("Loading...");

        if(translationText.isFocused()) {
            try {
                Log.i(CLASS_TAG, "instantiation of Wiktionary");

                Wiktionary wiktionary = new Wiktionary();
                wiktionary.enrichFromWiktionary(this,
                        this.selectedText,
                        this.source,
                        this.target);
            }
            catch(ValidationException e) {
                Log.d(CLASS_TAG, e.getMessage());
                this.webTextView.setText("No results found");
            }
            catch(SupportException e) {
                Log.d(CLASS_TAG, e.getMessage());
                this.webTextView.setText("No results found");
            }
        }
    }

    /**
     * This method is called by {@link Wiktionary} over the interface
     * {@link WiktionaryResponse} as soon as Wiktionary returns its json response.
     * If the json is null or error is found inside said json, the method returns and shows
     * the error in the TextView. Otherwise an the returned information is displayed in said TextView.
     *
     * Wiktionary is not queried directly. A Wiktionary Parser written by Yves Bourques is used.
     * Information regarding this parser can be found on his website:
     * http://www.igrec.ca/projects/wiktionary-text-parser/
     *
     * @param json Response from Wikipedia in form of a json.
     */
    @Override
    public void wiktionaryFinished(JSONObject json) {
        Log.i(CLASS_TAG, "wiktionaryFinished in ResultActivity called");

        if(json == null) {
            Log.d(CLASS_TAG, "json cannot be null");
            this.webTextView.setText("No results found");
            return;
        }

        String extract;

        try {
            if(json.has("error")) {
                Log.d(CLASS_TAG, json.getString("error"));
                this.webTextView.setText("No results found");
                return;
            }

            if(json.has("wiktionary")) {
                extract = "<b>Wiktionary:</b> " + this.selectedText + "<br/>" + json.getString("wiktionary");

                extract = extract.replace("|", "<br/>");
                extract = extract.replace("#", "<br/>");
                extract = extract.replace("{{", "");
                extract = extract.replace("}}", "");
                extract = extract.replace("[[", "");
                extract = extract.replace("]]", "");
                extract = extract.replace("lang", "Language");
                extract = extract.replace("=", " ");
            }
            else {
                Log.d(CLASS_TAG, "query is missing in json");
                this.webTextView.setText("No results found");
                return;
            }
        } catch (JSONException e) {
            Log.d(CLASS_TAG, "Exception while checking JSONObject");
            this.webTextView.setText("No results found");
            return;
        }

        this.webTextView.setText(Html.fromHtml(extract));
    }

    /**
     * Opens a browser and a google search for the selected text.
     */
    public void google(View view) {
        Log.i(CLASS_TAG, "google in ResultActivity called");

        Uri uri = Uri.parse("http://www.google.com/#q=" + this.selectedText +
                "&hl=" + this.target.trim() +

                "&lr=lang_" + this.target.trim());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /***************************** /ACTUAL ENRICHMENT *****************************/


    /***************************** HELPER METHODS *****************************/

    private void showToast(String text) {
        Log.i(CLASS_TAG, "showToast in MainActivity called");

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

        toast.show();
    }

    /***************************** /HELPER METHODS *****************************/


    /***************************** HELPER CLASSES *****************************/

    /**
     * Custom ViewPagerAdapter extended from {@link PagerAdapter} for displaying tabs.
     * Contains a tab for displaying text extracted from the chosen image as well as the
     * corresponding translation, which is represented by the layout tab_original_translation_enrichment,
     * and a tab for showing the original image, which is represented by the layout tab_original_image.
     * To show the enrichment information a {@link SlidingUpPanelLayout} is used. This
     * SlidingPaneLayout has to be initialized and modified in this class. All Callbacks for
     * starting of a contextual action bar menu are contained in this class. This is the case
     * when text is selected in a TextView.
     */
    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;

            // TAB ORIGINAL + TRANSLATION
            if(position == 0) {
                view = getLayoutInflater().inflate(R.layout.tab_original_translation_enrichment, container, false);

                // SLIDING PANE LAYOUT
                slidingPaneLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_pane_layout);
                slidingPaneLayout.setGravity(Gravity.BOTTOM);
                slidingPaneLayout.setAnchorPoint(0.4f);
                slidingPaneLayout.setTouchEnabled(false);
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                // BUTTONS
                wikipedia = (Button) view.findViewById(R.id.wikipedia_button);

                wiktionary = (Button) view.findViewById(R.id.wiktionary_button);

                google = (Button) view.findViewById(R.id.google_button);

                // TEXTVIEW FOR ENRICHMENT INFORMATION
                webScrollView = (ScrollView) view.findViewById(R.id.web_scrollView);
                webTextView = (TextView) view.findViewById(R.id.web_textView);
                webTextView.setMovementMethod(LinkMovementMethod.getInstance());

                // ORIGINAL TEXT
                originalText = (SelectionText) view.findViewById(R.id.original_text_editText);
                originalText.setText(text);

                // Set Callback for custom action car menu on original selected.
                originalText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                        wikipedia.setVisibility(View.GONE);

                        wiktionary.setVisibility(View.GONE);

                        webScrollView.setVisibility(View.GONE);

                        RelativeLayout.LayoutParams googleParams = (RelativeLayout.LayoutParams) google.getLayoutParams();
                        googleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                        googleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        google.setLayoutParams(googleParams);

                        slidingPaneLayout.setGravity(Gravity.BOTTOM);
                        slidingPaneLayout.setAnchorPoint(0.4f);
                        slidingPaneLayout.setTouchEnabled(false);

                        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

                        mode.getMenuInflater().inflate(R.menu.context_menu, menu);

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        slidingPaneLayout.setAnchorPoint(0.4f);
                        slidingPaneLayout.setTouchEnabled(false);

                        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    }
                });

                TextView originalTitleText = (TextView) view.findViewById(R.id.original_title_textView);
                String title = "ORIGINAL: <font color='#696969'>" + LanguageSupport.convert(source.trim()) + "</font>";

                if(LanguageSupport.convert(detected.trim()) != null) {
                    title += " DETECTED: <font color='#696969'>" + LanguageSupport.convert(detected.trim()) + "</font>";
                }

                originalTitleText.setText(Html.fromHtml(title));

                // TRANSLATION TEXT
                translationText = (SelectionText) view.findViewById(R.id.translation_text_editText);
                translationText.setText(translation);

                // Set Callback for custom action car menu on translation selected.
                translationText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                        wikipedia.setVisibility(View.VISIBLE);

                        wiktionary.setVisibility(View.VISIBLE);

                        webScrollView.setVisibility(View.VISIBLE);

                        RelativeLayout.LayoutParams wikiParams = (RelativeLayout.LayoutParams) wikipedia.getLayoutParams();
                        wikiParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        wikiParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        wikipedia.setLayoutParams(wikiParams);

                        RelativeLayout.LayoutParams dictParams = (RelativeLayout.LayoutParams) wiktionary.getLayoutParams();
                        dictParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        dictParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        wiktionary.setLayoutParams(dictParams);

                        RelativeLayout.LayoutParams googleParams = (RelativeLayout.LayoutParams) google.getLayoutParams();
                        googleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        googleParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        google.setLayoutParams(googleParams);

                        RelativeLayout.LayoutParams webParams = (RelativeLayout.LayoutParams) webScrollView.getLayoutParams();
                        webParams.addRule(RelativeLayout.BELOW, 0);
                        webParams.addRule(RelativeLayout.ABOVE, R.id.wikipedia_button);
                        webScrollView.setLayoutParams(webParams);

                        slidingPaneLayout.setGravity(Gravity.TOP);
                        slidingPaneLayout.setAnchorPoint(0.42f);
                        slidingPaneLayout.setTouchEnabled(false);

                        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

                        mode.getMenuInflater().inflate(R.menu.context_menu, menu);

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        slidingPaneLayout.setAnchorPoint(0.42f);
                        slidingPaneLayout.setTouchEnabled(false);

                        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    }
                });

                TextView translationTitleText = (TextView) view.findViewById(R.id.translation_title_textView);
                title = "TRANSLATION: <font color='#696969'>" + LanguageSupport.convert(target.trim()) + "</font>";
                translationTitleText.setText(Html.fromHtml(title));
            }
            // TAB IMAGE
            else if(position == 1) {
                view = getLayoutInflater().inflate(R.layout.tab_original_image, container, false);
                ImageView chosenImage = (ImageView) view.findViewById(R.id.original_image_imageView);

                imageFile = new File(imagePath.substring(5, imagePath.length()));
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                chosenImage.setImageBitmap(bitmap);
            }

            if(view != null) container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /***************************** HELPER CLASSES *****************************/
}
