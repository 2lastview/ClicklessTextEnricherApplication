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
 *
 */
public class MainActivity extends Activity implements
        ChooseFromLanguageDialog.ChooseFromLanguageListener,
        ChooseToLanguageDialog.ChooseToLanguageListener,
        Animation.AnimationListener,
        TranslateResponse {

    /**
     *
     */
    private static final String CLASS_TAG = "MainActivity";

    /**
     *
     */
    private static final CharSequence TAB_TITLES[] = {"CAMERA", "IMAGE GALLERY"};

    /**
     *
     */
    private static final int NUM_TABS = 2;

    /**
     *
     */
    private static final int REQUEST_CAMERA = 0;

    /**
     *
     */
    private static final int REQUEST_GALLERY = 1;

    /**
     *
     */
    private static final int REQUEST_EDIT = 2;

    /**
     *
     */
    private int lastRequest;

    /**
     *
     */
    private Animation animFadeIn;

    /**
     *
     */
    private RelativeLayout progressBar;

    /**
     *
     */
    private String source;

    /**
     *
     */
    private String target;

    /**
     *
     */
    private String imagePath;

    /**
     *
     */
    private File imageFile;


    /***************************** INIT *****************************/

    /**
     *
     * @param savedInstanceState
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
     *
     * @param view
     */
    public void showTranslateFromDialog(View view) {
        Log.i(CLASS_TAG, "showTranslateFromDialog in MainActivity called");

        DialogFragment dialog = new ChooseFromLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseFromLanguageDialog");

        Log.d(CLASS_TAG, "FROM dialog is showing");
    }

    /**
     *
     * @param which
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
     *
     * @param view
     */
    public void showTranslateToDialog(View view) {
        Log.i(CLASS_TAG, "showTranslateToDialog in MainActivity called");

        DialogFragment dialog = new ChooseToLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseToLanguageDialog");

        Log.d(CLASS_TAG, "TO dialog is showing");
    }

    /**
     *
     * @param which
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
     *
     * @param view
     */
    public void editOriginalImage(View view) {
        Log.i(CLASS_TAG, "editOriginalImage in MainActivity called");

        if(this.imagePath != null && this.imagePath.length() > 0) {
            Intent showImageIntent = new Intent(Intent.ACTION_EDIT);
            showImageIntent.setDataAndType(Uri.parse(this.imagePath), "image/*");

            try {
                startActivityForResult(showImageIntent, REQUEST_EDIT);
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
     *
     * @param view
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
     *
     * @param view
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
     *
     * @param requestCode
     * @param resultCode
     * @param data
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

    /***************************** /ANIMATIONS *****************************/


    /***************************** ENRICHMENT AND START RESULT ACTIVITY *****************************/

    /**
     *
     * @param view
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
     *
     * @param json
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

    /**
     *
     * @param view
     */
    public void untouchable(View view) {
    }

    /***************************** /ENRICHMENT AND START RESULT ACTIVITY *****************************/


    /***************************** HELPER METHODS *****************************/

    /**
     *
     * @return
     * @throws IOException
     */
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

    /**
     *
     * @param text
     */
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
     *
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