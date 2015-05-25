package com.example.moritztomasi.clicklesstextenricherapplication.enrichment;

import org.json.JSONObject;

/**
 *
 */
public interface TranslateResponse {

    /**
     *
     * @param json
     */
    public void translateFinished(JSONObject json);
}
