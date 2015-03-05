package com.example.moritztomasi.clicklesstextenricherapplication.common;

import android.os.AsyncTask;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Enrich {

    private static final String CLASS_TAG = "Enrich";

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

        if(source == null) {
            Log.d(CLASS_TAG, "source cannot be null");
            throw new ValidationException("You have to choose a language to translate from.");
        }

        if(target == null) {
            Log.d(CLASS_TAG, "target cannot be null");
            throw new ValidationException("You have to choose a language to translate to.");
        }

        if(enrich == null) {
            Log.d(CLASS_TAG, "enrich cannot be null");
            throw new ValidationException("You have to choose if the text should be enriched.");
        }

        if(imagePath == null) {
            Log.d(CLASS_TAG, "imagePath cannot be null");
            throw new ValidationException("You have to choose a image.");
        }

        if(!fromLanguages.contains(source)) {
            Log.d(CLASS_TAG, "source language is not supported.");
            throw new SupportException("source language is not supported.");
        }

        if(!toLanguages.contains(target)) {
            Log.d(CLASS_TAG, "target language is not supported.");
            throw new SupportException("target language is not supported.");
        }

        String url = "";
        if (!source.equals("unk")) url = "http://131.130.133.240:8080/enrich?source=" + source + "&target=" + target + "&enrich=" + enrich.toString() + "&filetype=png";
        else url = "http://131.130.133.240:8080/enrich?target=" + target + "&enrich=" + enrich.toString() + "&filetype=png";

        if(text == null || text.length() <= 0) {
            new ImageTransferTask(asyncResponse).execute(url, imagePath);
        }
        else {
            new ImageTransferTask(asyncResponse).execute(url, imagePath, text);
        }
    }

    public class ImageTransferTask extends AsyncTask<String, Void, String> {

        private AsyncResponse asyncResponse;

        public ImageTransferTask(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected String doInBackground(String... parameters) {
            String url = parameters[0];
            String imagePath = parameters[1].substring(5, parameters[1].length());

            File image = null;
            String text = null;
            String responseString = "";

            if(parameters.length == 2) {
                image = new File(imagePath);
            }
            else if(parameters.length == 3) {
                text = parameters[2];
            }
            else {
                responseString = "{ 'error': 'Could not send request.' }";
                return responseString;
            }

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            if(image != null) entityBuilder.addBinaryBody("image", image);
            else if(text != null) entityBuilder.addTextBody("text", text);
            else {
                responseString = "{ 'error': 'Could not send request.' }";
                return responseString;
            }

            HttpEntity httpEntity = entityBuilder.build();
            httpPost.setEntity(httpEntity);
            HttpParams params = httpClient.getParams();
            httpPost.setParams(params);

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpPost);
            }
            catch(IOException e) {
                responseString = "{ 'error': 'Could not send request.' }";
                return responseString;
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

            return responseString;
        }

        @Override
        protected void onPostExecute(String response) {
            asyncResponse.postFinish(response);
        }
    }
}
