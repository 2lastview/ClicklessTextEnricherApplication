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
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.moritztomasi.clicklesstextenricherapplication.common.LanguageSupport;
import com.example.moritztomasi.clicklesstextenricherapplication.dialogues.ChooseFromLanguageDialog;
import com.example.moritztomasi.clicklesstextenricherapplication.dialogues.ChooseToLanguageDialog;
import com.example.moritztomasi.clicklesstextenricherapplication.enrichment.Translate;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SlidingTabLayout;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SupportException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ValidationException;
import com.example.moritztomasi.clicklesstextenricherapplication.R;
import com.example.moritztomasi.clicklesstextenricherapplication.enrichment.TranslateResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This is the first Activity shown after opening the application. It contains mechanisms
 * for selecting a certain image and configuring the translation settings. The associated
 * layout file is activity_main.
 */
public class MainActivity extends Activity implements
        ChooseFromLanguageDialog.ChooseFromLanguageListener,
        ChooseToLanguageDialog.ChooseToLanguageListener,
        Animation.AnimationListener,
        TranslateResponse {

    private static final String CLASS_TAG = "MainActivity";
    private static final CharSequence TAB_TITLES[] = {"CAMERA", "IMAGE GALLERY"};
    private static final int NUM_TABS = 2;
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_EDIT = 2;

    private int lastRequest;

    private Animation animFadeIn;
    private RelativeLayout progressBar;

    private String source;
    private String target;
    private String imagePath;
    private File imageFile;


    /***************************** INIT *****************************/

    /**
     * First method called in Activity lifecycle. Initializes tabs in {@link SlidingTabLayout} and
     * sets color for tab indicator.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(CLASS_TAG, "onCreate in MainActivity called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPagerAdapter adapter = new ViewPagerAdapter();

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(false);
        tabs.setViewPager(pager);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.rgb(250, 250, 250);
            }
        });

        this.progressBar = (RelativeLayout) findViewById(R.id.progress_bar_relativeLayout);

        this.animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        this.animFadeIn.setAnimationListener(this);
    }

    /***************************** /INIT *****************************/


    /***************************** TRANSLATION SETTINGS *****************************/

    /**
     * Opens a dialog for choosing the source language.
     */
    public void showTranslateFromDialog(View view) {
        Log.i(CLASS_TAG, "showTranslateFromDialog in MainActivity called");

        DialogFragment dialog = new ChooseFromLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseFromLanguageDialog");

        Log.d(CLASS_TAG, "FROM dialog is showing");
    }

    /**
     * This method is called by {@link ChooseFromLanguageDialog} over the interface
     * {@link ChooseFromLanguageDialog.ChooseFromLanguageListener} when an option in the dialog is
     * chosen or the dialog is closed. If the dialog is just closed none of the cases are executed.
     * A string representation of the source language is stored in source.
     *
     * @param which Integer indicating which source language was chosen
     */
    @Override
    public void onFromLanguageDialogClick(int which) {
        Log.i(CLASS_TAG, "dialog finished, onFromLanguageDialogClick in MainActivity called");

        Button button = (Button) findViewById(R.id.from_language_button);
        switch(which) {
            case 0: this.source = "en";
                button.setText("FROM\n" + LanguageSupport.convert(this.source));
                break;
            case 1: this.source = "de";
                button.setText("FROM\n" + LanguageSupport.convert(this.source));
                break;
            case 2: this.source = "it";
                button.setText("FROM\n" + LanguageSupport.convert(this.source));
                break;
            case 3: this.source = "unk";
                button.setText("FROM\n" + LanguageSupport.convert(this.source));
                break;
        }

        Log.d(CLASS_TAG, "source language set to: source=" + this.source);
    }

    /**
     * Opens a dialog for choosing a target language.
     */
    public void showTranslateToDialog(View view) {
        Log.i(CLASS_TAG, "showTranslateToDialog in MainActivity called");

        DialogFragment dialog = new ChooseToLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseToLanguageDialog");

        Log.d(CLASS_TAG, "TO dialog is showing");
    }

    /**
     * This method is called by {@link ChooseToLanguageDialog} over the interface
     * {@link ChooseToLanguageDialog.ChooseToLanguageListener} when an option in the dialog is
     * chosen or the dialog is closed. If the dialog is just closed none of the cases are executed.
     * A string representation of the target language is stored in target.
     *
     * @param which Integer indicating which target language was chosen
     */
    @Override
    public void onToLanguageDialogClick(int which) {
        Log.i(CLASS_TAG, "dialog finished, onToLanguageDialogClick in MainActivity called");

        Button button = (Button) findViewById(R.id.to_language_button);
        switch(which) {
            case 0: this.target = "en";
                button.setText("TO\n" + LanguageSupport.convert(this.target));
                break;
            case 1: this.target = "de";
                button.setText("TO\n" + LanguageSupport.convert(this.target));
                break;
            case 2: this.target = "it";
                button.setText("TO\n" + LanguageSupport.convert(this.target));
                break;
        }

        Log.d(CLASS_TAG, "target language set to: target=" + this.target);
    }

    /**
     * Opens a system editor. The edited image image is stored separately, which means the original
     * is not overridden. In case a device does not support ACTION_EDIT the {@link ActivityNotFoundException}
     * will be caught and a toast shown onscreen.
     */
    public void editOriginalImage(View view) {
        Log.i(CLASS_TAG, "editOriginalImage in MainActivity called");

        if(this.imagePath != null && this.imagePath.length() > 0) {
            Intent editIntent = new Intent(Intent.ACTION_EDIT);
            editIntent.setDataAndType(Uri.parse(this.imagePath), "image/*");

            try {
                startActivityForResult(editIntent, REQUEST_EDIT);
            }
            catch(ActivityNotFoundException e) {
                Log.d(CLASS_TAG, "edit image activity is not supported");
                showToast("This activity is not supported by this device.");
            }
        }
        else {
            Log.d(CLASS_TAG, "imagePath cannot be null when showing original image");
            showToast("You have to choose an image.");
        }
    }

    /***************************** /TRANSLATION SETTINGS *****************************/


    /***************************** TABS (GALLERY OR CAMERA) *****************************/

    /**
     *  Opens the camera and creates a temporary file where said image will be stored if the
     *  result is ok. Exceptions are caught and a toast shown onscreen.
     */
    public void getImageFromCamera(View view) {
        Log.i(CLASS_TAG, "getImageFromCamera in MainActivity called and camera interaction started");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                Log.d(CLASS_TAG, "Exception while creating file");
                showToast("Could not create a new image file");
            }

            if (imageFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    /**
     * Opens the gallery for image selection.
     */
    public void getImageFromGallery(View view) {
        Log.i(CLASS_TAG, "getImageFromGallery in MainActivity called and gallery interaction started");

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Choose one image"), REQUEST_GALLERY);
        }
    }

    /**
     * This method deals with the results of choosing an image from the gallery, taking an
     * image with the devices camera or editing the chosen image.
     *
     * @param requestCode The request code which the action was started with
     * @param resultCode Result code indicating if result was ok or canceled
     * @param data Data returned by the action
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(CLASS_TAG, "onActivityResult in MainActivity called with resultCode=" + resultCode);

        if(requestCode == REQUEST_CAMERA) {
            if(resultCode == Activity.RESULT_OK) {
                MediaScannerConnection.scanFile(this, new String[] { this.imageFile.toString() }, null, null);
                ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                openCameraImageButton.setImageResource(R.drawable.camera_icon_checked);
                openGalleryImageButton.setImageResource(R.drawable.gallery_icon);
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                boolean deleted = this.imageFile.delete();

                if(deleted) Log.d(CLASS_TAG, "created image for camera deleted successfully");
                else Log.d(CLASS_TAG, "created image for camera could not be deleted");

                MediaScannerConnection.scanFile(this, new String[] { this.imageFile.toString() }, null, null);
                this.imagePath = null;
                this.imageFile = null;
                ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                openCameraImageButton.setImageResource(R.drawable.camera_icon);
                openGalleryImageButton.setImageResource(R.drawable.gallery_icon);
            }

            this.lastRequest = REQUEST_CAMERA;
        }
        else if(requestCode == REQUEST_GALLERY || requestCode == REQUEST_EDIT) {
            if(resultCode == Activity.RESULT_OK) {
                Uri image = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(image, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                this.imagePath = "file:" + cursor.getString(columnIndex);
                this.imageFile = new File(this.imagePath);
                ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                openCameraImageButton.setImageResource(R.drawable.camera_icon);
                openGalleryImageButton.setImageResource(R.drawable.gallery_icon_checked);

                cursor.close();

                if(requestCode == REQUEST_EDIT) {
                    if(this.lastRequest == REQUEST_CAMERA) {
                        openCameraImageButton.setImageResource(R.drawable.camera_icon_checked);
                        openGalleryImageButton.setImageResource(R.drawable.gallery_icon);
                    }
                }
                else {
                    this.lastRequest = REQUEST_GALLERY;
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                if(requestCode == REQUEST_GALLERY) {
                    this.imagePath = null;
                    this.imageFile = null;
                    ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                    ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                    openCameraImageButton.setImageResource(R.drawable.camera_icon);
                    openGalleryImageButton.setImageResource(R.drawable.gallery_icon);

                    this.lastRequest = REQUEST_GALLERY;
                }
            }
        }
    }

    /***************************** /TABS (GALLERY OR CAMERA) *****************************/


    /***************************** ANIMATIONS *****************************/

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    /**
     * This method is executed when the user taps on the progress bar layout when it is showing.
     * It is supposed to stay empty so that no underlying buttons can be clicked while the
     * progress bar is showing.
     */
    public void untouchable(View view) {
    }

    /***************************** /ANIMATIONS *****************************/


    /***************************** ENRICHMENT AND START RESULT ACTIVITY *****************************/

    /**
     * Instantiates a {@link Translate} service object and passes source language, target language
     * and the path to the selected image. The next step will be delegated by use of the
     * interface {@link TranslateResponse} and the method {@link TranslateResponse#translateFinished(JSONObject)}.
     * During the execution of this method a progress bar should be showing, and then disappear right
     * before the next activity is started, or after an error occurs.
     */
    public void go(View view) {
        Log.i(CLASS_TAG, "go in MainActivity called");

        this.progressBar.setVisibility(View.VISIBLE);
        this.progressBar.startAnimation(animFadeIn);

        try {
            Log.i(CLASS_TAG, "instantiation of Translate");

            Translate translate = new Translate();
            translate.translateFromImage(this,
                    this.source,
                    this.target,
                    this.imagePath,
                    null);
        }
        catch(ValidationException e) {
            Log.d(CLASS_TAG, e.getMessage());
            this.progressBar.setVisibility(View.GONE);
            showToast(e.getMessage());
        }
        catch(SupportException e) {
            Log.d(CLASS_TAG, e.getMessage());
            this.progressBar.setVisibility(View.GONE);
            showToast(e.getMessage());
        }
    }

    /**
     * This method is called by {@link Translate} over the interface
     * {@link TranslateResponse} as soon as the web service returns its json response.
     * If the json is null or error is found inside said json, the method returns and shows
     * a toast with the provided error message. Otherwise an {@link Intent} is created and a new
     * activity started. During the execution of this method a progress bar should be showing, and
     * then disappear right before a {@link ResultActivity} is started, or after an error occurs.
     *
     * @param json Response from web service in form of a json.
     */
    @Override
    public void translateFinished(JSONObject json) {
        Log.i(CLASS_TAG, "translateFinished in MainActivity called");

        if(json == null) {
            Log.d(CLASS_TAG, "json cannot be null");
            this.progressBar.setVisibility(View.GONE);
            showToast("Response faulty");
            return;
        }

        String detected = "";
        String text = "";
        String translation = "";

        try {
            if(json.has("error")) {
                Log.d(CLASS_TAG, json.getString("error"));
                this.progressBar.setVisibility(View.GONE);
                showToast(json.getString("error"));
                return;
            }

            if(json.has("detected")) detected = json.getString("detected");
            if(json.has("text")) text = json.getString("text");
            if(json.has("translation")) translation = json.getString("translation");
        } catch (JSONException e) {
            Log.d(CLASS_TAG, "Exception while checking JSONObject");
            this.progressBar.setVisibility(View.GONE);
            showToast("Response faulty");
            return;
        }

        Intent intent = new Intent(this, ResultActivity.class);

        intent.putExtra("SOURCE_LANGUAGE", this.source);
        intent.putExtra("TARGET_LANGUAGE", this.target);
        intent.putExtra("DETECTED_LANGUAGE", detected);
        intent.putExtra("TEXT", text);
        intent.putExtra("TRANSLATION", translation);
        intent.putExtra("IMAGE_PATH", this.imagePath);
        intent.putExtra("IMAGE_FILE", this.imageFile);

        this.progressBar.setVisibility(View.GONE);
        startActivity(intent);
    }

    /***************************** /ENRICHMENT AND START RESULT ACTIVITY *****************************/


    /***************************** HELPER METHODS *****************************/

    private File createImageFile() throws IOException {
        Log.i(CLASS_TAG, "createImageFile in MainActivity called");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en")).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CTE");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        this.imagePath = "file:" + image.getAbsolutePath();
        this.imageFile = image;

        Log.i(CLASS_TAG, "new image file created at imagePath=" + this.imagePath);

        return image;
    }

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
     * Contains a tab for opening the camera, which is represented by the layout
     * tab_open_camera, and a tab for opening the gallery, which is represented by the
     * layout tab_open_gallery. All layout elements inside the tabs, which are
     * changed programmatically, have to be initialized while the tabs themselves are
     * initialized.
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
            if(position == 0) {
                view = getLayoutInflater().inflate(R.layout.tab_open_camera, container, false);
            }
            else if(position == 1) {
                view = getLayoutInflater().inflate(R.layout.tab_open_gallery, container, false);
            }

            if(view != null) container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /***************************** /HELPER CLASSES *****************************/
}