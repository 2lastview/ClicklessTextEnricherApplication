package com.example.moritztomasi.clicklesstextenricherapplication;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.moritztomasi.clicklesstextenricherapplication.common.Enrich;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SlidingTabLayout;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SupportException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ValidationException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ViewPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends FragmentActivity implements
        ChooseFromLanguageDialog.ChooseFromLanguageListener,
        ChooseToLanguageDialog.ChooseToLanguageListener,
        ChooseEnrichLanguageDialog.ChooseEnrichLanguageListener,
        AsyncResponse {

    private static final String CLASS_TAG = "MainActivity";
    private static final CharSequence titles[] = {"CAMERA", "GALLERY"};
    private static final int NUM_TABS = 2;
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_GALLERY = 1;

    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;

    private String fromLanguage;
    private String toLanguage;
    private Boolean enrich;
    private String imagePath;
    private File imageFile;

    public MainActivity() {
        this.pager = null;
        this.adapter = null;
        this.tabs = null;

        this.fromLanguage = null;
        this.toLanguage = null;
        this.enrich = null;
        this.imagePath = null;
        this.imageFile = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles, NUM_TABS);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);

        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Integer.parseInt("#FAFAFA".replaceFirst("^#",""), 16);
            }
        });

        tabs.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /** TRANSLATION SETTINGS **/

    public void showTranslateFromDialog(View view) {
        DialogFragment dialog = new ChooseFromLanguageDialog();
        dialog.show(getFragmentManager(), "ChooseFromLanguageDialog");
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
    public void onFromLanguageDialogClick(int which) {
        Button button = (Button) findViewById(R.id.from_language_button);
        switch(which) {
            case 0: this.fromLanguage = "eng";
                    button.setText("FROM\n" + "English");
                    break;
            case 1: this.fromLanguage = "deu";
                    button.setText("FROM\n" + "German");
                    break;
            case 2: this.fromLanguage = "ita";
                    button.setText("FROM\n" + "Italian");
                    break;
            case 3: this.fromLanguage = "unk";
                    button.setText("FROM\n" + "Unknown");
                    break;
        }
    }

    @Override
    public void onToLanguageDialogClick(int which) {
        Button button = (Button) findViewById(R.id.to_language_button);
        switch(which) {
            case 0: this.toLanguage = "eng";
                    button.setText("TO\n" + "English");
                    break;
            case 1: this.toLanguage = "deu";
                    button.setText("TO\n" + "German");
                    break;
            case 2: this.toLanguage = "ita";
                    button.setText("TO\n" + "Italian");
                    break;
        }
    }

    @Override
    public void onEnrichDialogClick(int which) {
        Button button = (Button) findViewById(R.id.enrich_language_button);
        switch(which) {
            case 0: this.enrich = true;
                    button.setText("ENRICH\n" + "Yes");
                    break;
            case 1: this.enrich = false;
                    button.setText("ENRICH\n" + "No");
                    break;
        }
    }

    /** /TRANSLATION SETTINGS **/


    /** FROM CAMERA OR FROM GALLERY **/

    public void getImageFromCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                Log.d(CLASS_TAG, "Exception while creating file");
            }

            if (imageFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    public void getImageFromGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Choose one image"), REQUEST_GALLERY);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAMERA) {
            if(resultCode == Activity.RESULT_OK) {
                MediaScannerConnection.scanFile(this, new String[] { this.imageFile.toString() }, null, null);
                ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                openCameraImageButton.setImageResource(R.drawable.camera_icon_checked);
                openGalleryImageButton.setImageResource(R.drawable.gallery_icon);
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                this.imageFile.delete();
                MediaScannerConnection.scanFile(this, new String[] { this.imageFile.toString() }, null, null);
                this.imagePath = null;
                this.imageFile = null;
                ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                openCameraImageButton.setImageResource(R.drawable.camera_icon);
                openGalleryImageButton.setImageResource(R.drawable.gallery_icon);
            }
        }
        else if(requestCode == REQUEST_GALLERY) {
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
            }
            else if(resultCode == Activity.RESULT_CANCELED) {
                this.imagePath = null;
                this.imageFile = null;
                ImageButton openCameraImageButton = (ImageButton) findViewById(R.id.open_camera_imageButton);
                ImageButton openGalleryImageButton = (ImageButton) findViewById(R.id.open_gallery_imageButton);
                openCameraImageButton.setImageResource(R.drawable.camera_icon);
                openGalleryImageButton.setImageResource(R.drawable.gallery_icon);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CTE");
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        this.imagePath = "file:" + image.getAbsolutePath();
        this.imageFile = image;

        return image;
    }

    /** /FROM CAMERA OR FROM GALLERY **/


    /** GO TO TRANSLATION AND ENRICHMENT **/

    public void go(View view) {

        try {
            Enrich enrich = new Enrich();
            enrich.enrichFromImage(this,
                    this.fromLanguage,
                    this.toLanguage,
                    this.enrich,
                    this.imagePath,
                    null);
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