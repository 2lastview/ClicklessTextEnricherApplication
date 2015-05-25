package com.example.moritztomasi.clicklesstextenricherapplication.enrichment;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.moritztomasi.clicklesstextenricherapplication.common.LanguageSupport;
import com.example.moritztomasi.clicklesstextenricherapplication.common.SupportException;
import com.example.moritztomasi.clicklesstextenricherapplication.common.ValidationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 */
public class Wiktionary {

    /**
     *
     */
    private static final String CLASS_TAG = "Wiktionary";

    /**
     *
     */
    private String WIKTIONARY_URL = "http://www.igrec.ca/project-files/wikparser/wikparser.php?query=def&count=5&word=";

    /**
     *
     */
    private String word;

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
    private WiktionaryResponse wiktionaryResponse;

    /**
     *
     * @param wiktionaryResponse
     * @param word
     * @param source
     * @param target
     * @throws ValidationException
     * @throws SupportException
     */
    public void enrichFromWiktionary(WiktionaryResponse wiktionaryResponse, String word, String source, String target) throws ValidationException, SupportException {
        Log.i(CLASS_TAG, "enrichFromWiktionary in Wiktionary called with wiktionaryResponse and parameters: word=" + word + " source=" + source + " target=" + target);

        if(wiktionaryResponse == null) {
            Log.d(CLASS_TAG, "wiktionaryResponse cannot be null");
            throw new ValidationException("Could not send request.");
        }
        else {
            Log.d(CLASS_TAG, "wiktionaryResponse passed correctly");
            this.wiktionaryResponse = wiktionaryResponse;
        }

        if(word == null || word.length() <= 0) {
            Log.d(CLASS_TAG, "word cannot be null or empty");
            throw new ValidationException("You have to choose word for Wiktionary.");
        }
        else {
            Log.d(CLASS_TAG, "word=" + word);
            this.word = word;
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

        if(!LanguageSupport.fromSupported(source)) {
            Log.d(CLASS_TAG, "source language is not supported.");
            throw new SupportException("This source language is not supported.");
        }

        if(!LanguageSupport.toSupported(target)) {
            Log.d(CLASS_TAG, "target language is not supported.");
            throw new SupportException("This target language is not supported.");
        }

        new WiktionaryTask().execute();
    }

    /**
     *
     */
    private class WiktionaryTask extends AsyncTask<Void, Void, JSONObject> {

        /**
         *
         * @param params
         * @return
         */
        @Override
        protected JSONObject doInBackground(Void... params) {
            Log.i(CLASS_TAG, "WiktionaryTask started running by calling doInBackground");

            String responseString;

            HttpClient httpClient = new DefaultHttpClient();
            String url = WIKTIONARY_URL;

            try {
                url += URLEncoder.encode(word.trim(), "UTF-8");
                url += "&lang=";
                url += URLEncoder.encode(target.trim(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.d(CLASS_TAG, "Exception while executing request and encoding url");
                responseString = "{ 'error': 'Could not send request.' }";
                return getJSON(responseString);
            }

            HttpGet httpGet = new HttpGet(Uri.parse(url).toString());
            Log.d(CLASS_TAG, "HttpGet created with URI: " + httpGet.getURI().toString());

            HttpResponse httpResponse;
            try {
                httpResponse = httpClient.execute(httpGet);
            } catch (IOException e) {
                Log.d(CLASS_TAG, "Exception while executing get request");
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

                    if(responseString.contains("ERROR")) {
                        responseString = "{ 'error': '" + responseString + "' }";
                    }
                    else {
                        responseString = "{ 'wiktionary': '" + responseString + "' }";
                    }
                } else {
                    responseString = "{ 'error': 'Response faulty.' }";
                }
            }
            catch(IOException e) {
                Log.d(CLASS_TAG, "Exception while reading from http get response");
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
            Log.i(CLASS_TAG, "WiktionaryTask finished running by calling onPostExecute and wiktionaryFinished with json");
            wiktionaryResponse.wiktionaryFinished(json);
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
