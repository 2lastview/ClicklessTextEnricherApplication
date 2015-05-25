package com.example.moritztomasi.clicklesstextenricherapplication.enrichment;

import org.json.JSONObject;

/**
 *
 */
public interface WiktionaryResponse {

    /**
     *
     * @param json
     */
    public void wiktionaryFinished(JSONObject json);
}
