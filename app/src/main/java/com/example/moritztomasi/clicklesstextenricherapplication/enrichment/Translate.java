package com.example.moritztomasi.clicklesstextenricherapplication.enrichment;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.example.moritztomasi.clicklesstextenricherapplication.common.LanguageSupport;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SupportException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ValidationException;

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

/**
 *
 */
public class Translate {

    /**
     *
     */
    private static final String CLASS_TAG = "Translate";

    /**
     *
     */
    private static final String TRANSLATE_URL = "";

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
    private String text;

    /**
     *
     */
    private String fileType;

    /**
     *
     */
    private TranslateResponse translateResponse;

    /**
     *
     * @param translateResponse
     * @param source
     * @param target
     * @param imagePath
     * @param text
     * @throws ValidationException
     * @throws SupportException
     */
    public void translateFromImage(TranslateResponse translateResponse, String source, String target, String imagePath, String text) throws ValidationException, SupportException {
        Log.i(CLASS_TAG, "translateFromImage in Translate called with translateResponse and parameters: source=" + source + " target=" + target);

        this.fileType = "jpg";

        if(translateResponse == null) {
            Log.d(CLASS_TAG, "translateResponse cannot be null");
            throw new ValidationException("Could not send request.");
        }
        else {
            Log.d(CLASS_TAG, "translateResponse passed correctly");
            this.translateResponse = translateResponse;
        }

        if(source == null) {
            Log.d(CLASS_TAG, "source cannot be null");
            throw new ValidationException("You have to choose a language to translate from.");
        }
        else {
            Log.d(CLASS_TAG, "source=" + source);
            this.source = source;
        }

        if(target == null) {
            Log.d(CLASS_TAG, "target cannot be null");
            throw new ValidationException("You have to choose a language to translate to.");
        }
        else {
            Log.d(CLASS_TAG, "target=" + target);
            this.target = target;
        }

        if(imagePath == null) {
            Log.d(CLASS_TAG, "imagePath cannot be null");
            throw new ValidationException("You have to choose an image.");
        }
        else {
            Log.d(CLASS_TAG, "imagePath=" + imagePath);
            this.imagePath = imagePath;
        }

        if(text != null && text.length() > 0) {
            Log.d(CLASS_TAG, "text is used instead of image");
            this.text = text;
        }

        if(!LanguageSupport.fromSupported(source)) {
            Log.d(CLASS_TAG, "source language is not supported.");
            throw new SupportException("This source language is not supported.");
        }

        if(!LanguageSupport.toSupported(target)) {
            Log.d(CLASS_TAG, "target language is not supported.");
            throw new SupportException("This target language is not supported.");
        }

        new TranslateTask().execute();
    }

    /**
     *
     */
    private class TranslateTask extends AsyncTask<Void, Void, JSONObject> {

        /**
         *
         * @param parameters
         * @return
         */
        @Override
        protected JSONObject doInBackground(Void... parameters) {
            Log.i(CLASS_TAG, "TranslateTask started running by calling doInBackground");

            File image = new File(imagePath.substring(5, imagePath.length()));
            String responseString;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(TRANSLATE_URL);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            if(!source.equals("unk")) entityBuilder.addTextBody("source", source.trim());
            entityBuilder.addTextBody("target", target.trim());
            entityBuilder.addTextBody("filetype", fileType);

            if(text != null && text.length() > 0) {
                Log.d(CLASS_TAG, "text is used instead of image");
                entityBuilder.addTextBody("text", Base64.encodeToString(text.getBytes(), Base64.DEFAULT));
            }
            else {
                Log.d(CLASS_TAG, "image is used instead of text");
                entityBuilder.addBinaryBody("image", image);
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
                Log.d(CLASS_TAG, "Exception while executing post request");
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
                Log.d(CLASS_TAG, "Exception while reading from http post response");
                responseString = "{ 'error': 'Response faulty.' }";
            }

            return getJSON(responseString);
        }

        /**
         *
         * @param json
         */
        @Override
        protected void onPostExecute(JSONObject json) {
            Log.i(CLASS_TAG, "TranslateTask finished running by calling onPostExecute and translateFinished with json");
            translateResponse.translateFinished(json);
        }

        /**
         *
         * @param response
         * @return
         */
        private JSONObject getJSON(String response) {
            JSONObject json = null;
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                Log.d(CLASS_TAG, "Exception while creating JSONObject from response");
            }

            return json;
        }
    }
}
