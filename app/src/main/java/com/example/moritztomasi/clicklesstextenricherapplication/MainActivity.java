package com.example.moritztomasi.clicklesstextenricherapplication;

import android.app.DialogFragment;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.example.moritztomasi.clicklesstextenricherapplication.common.SlidingTabLayout;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ViewPagerAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends FragmentActivity implements
        ChooseFromLanguageDialog.ChooseFromLanguageListener,
        ChooseToLanguageDialog.ChooseToLanguageListener,
        ChooseEnrichLanguageDialog.ChooseEnrichLanguageListener {

    private static final CharSequence titles[] = {"CAMERA", "GALLERY"};
    private static final int NUM_TABS = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentImagePath;
    private File currentImage;

    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;

    private String fromLanguage = null;
    private String toLanguage = null;
    private Boolean enrich = null;

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
            case 3: this.fromLanguage = null;
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

    /** Image From Camera **/

    public void getImageFromCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(imageFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                MediaScannerConnection.scanFile(this, new String[] { imageFile.toString() }, null, null);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CTE");

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentImagePath = "file:" + image.getAbsolutePath();
        currentImage = image;

        return image;
    }

    /** /Image From Camera **/

    /** Image from Gallery **/

    

    /** /Image from Gallery **/
}