package com.example.moritztomasi.clicklesstextenricherapplication.common;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.example.moritztomasi.clicklesstextenricherapplication.AsyncResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Enrich {

    private static final String CLASS_TAG = "Enrich";
    private static final String URL = "http://131.130.133.58:8080/enrich";

    private String source;
    private String target;
    private Boolean enrich;
    private String fileType;
    private String imagePath;
    private String text;

    public void enrichFromImage(AsyncResponse asyncResponse, String source, String target, Boolean enrich, String imagePath, String text) throws ValidationException, SupportException {
        Log.i(CLASS_TAG, "enrichFromImage in Enrich called with parameters: source=" + source + " target=" + target + " enrich=" + enrich);

        List<String> fromLanguages = new ArrayList<String>();
        List<String> toLanguages = new ArrayList<String>();

        fromLanguages.add("eng");
        fromLanguages.add("deu");
        fromLanguages.add("ita");
        fromLanguages.add("unk");

        toLanguages.add("eng");
        toLanguages.add("deu");
        toLanguages.add("ita");

        this.fileType = "png";

        if(source == null) {
            Log.d(CLASS_TAG, "source cannot be null");
            throw new ValidationException("You have to choose a language to translate from.");
        }
        else this.source = source;

        if(target == null) {
            Log.d(CLASS_TAG, "target cannot be null");
            throw new ValidationException("You have to choose a language to translate to.");
        }
        else this.target = target;

        if(enrich == null) {
            Log.d(CLASS_TAG, "enrich cannot be null");
            throw new ValidationException("You have to choose if the text should be enriched.");
        }
        else this.enrich = enrich;

        if(imagePath == null) {
            Log.d(CLASS_TAG, "imagePath cannot be null");
            throw new ValidationException("You have to choose a image.");
        }
        else this.imagePath = imagePath;

        if(text != null && text.length() > 0) {
            this.text = text;
        }

        if(!fromLanguages.contains(source)) {
            Log.d(CLASS_TAG, "source language is not supported.");
            throw new SupportException("source language is not supported.");
        }

        if(!toLanguages.contains(target)) {
            Log.d(CLASS_TAG, "target language is not supported.");
            throw new SupportException("target language is not supported.");
        }

        new ImageTransferTask(asyncResponse).execute();
    }

    public class ImageTransferTask extends AsyncTask<Void, Void, JSONObject> {

        private AsyncResponse asyncResponse;

        public ImageTransferTask(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected JSONObject doInBackground(Void... parameters) {
            File image = new File(imagePath.substring(5, imagePath.length()));
            String responseString = "";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(URL);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            if(!source.equals("unk")) entityBuilder.addTextBody("source", source);
            entityBuilder.addTextBody("target", target);
            entityBuilder.addTextBody("enrich", enrich.toString());
            entityBuilder.addTextBody("filetype", fileType);

            if(text != null && text.length() > 0) {
                entityBuilder.addTextBody("text", Base64.encodeToString(text.getBytes(), Base64.DEFAULT));
            }
            else if(image != null) {
                entityBuilder.addBinaryBody("image", image);
            }
            else {
                responseString = "{ 'error': 'Could not send request.' }";
                return getJSON(responseString);
            }

            HttpEntity httpEntity = entityBuilder.build();
            httpPost.setEntity(httpEntity);
            HttpParams params = httpClient.getParams();
            httpPost.setParams(params);

            HttpResponse httpResponse;
            try {
                httpResponse = httpClient.execute(httpPost);
            }
            catch(IOException e) {
                responseString = "{ 'error': 'Could not send request.' }";
                return getJSON(responseString);
            }

            HttpEntity httpEntityResponse = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            try {
                if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                    responseString = "{ 'error': '" + EntityUtils.toString(httpEntityResponse) + "' }";
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    responseString = "{ 'error': '" + EntityUtils.toString(httpEntityResponse) + "' }";
                } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    responseString = "{ 'error': '" + EntityUtils.toString(httpEntityResponse) + "' }";
                } else if (statusCode == HttpStatus.SC_OK) {
                    responseString = EntityUtils.toString(httpEntityResponse);
                } else {
                    responseString = "{ 'error': 'Response faulty.' }";
                }
            }
            catch(IOException e) {
                responseString = "{ 'error': 'Response faulty.' }";
            }

            return getJSON(responseString);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            asyncResponse.postFinish(json);
        }

        private JSONObject getJSON(String response) {
            JSONObject json = null;
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                Log.d(CLASS_TAG, "Exception while putting in JSONObject");
                e.printStackTrace();
            }

            return json;
        }
    }
}
