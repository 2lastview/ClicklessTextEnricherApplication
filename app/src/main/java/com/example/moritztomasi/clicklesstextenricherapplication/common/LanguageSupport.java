package com.example.moritztomasi.clicklesstextenricherapplication.common;

import java.util.Arrays;
import java.util.HashMap;

/**
 *
 */
public final class LanguageSupport {

    private LanguageSupport() {}

    /**
     *
     * @param lang
     * @return
     */
    public static String convert(String lang) {
        HashMap<String, String> languages = new HashMap<String, String>();
        languages.put("en", "English");
        languages.put("de", "German");
        languages.put("it", "Italian");
        languages.put("unk", "Unknown");

        if(lang.contains("detected:")) {
            lang = lang.replace("detected:", "");
            return lang;
        }

        return languages.get(lang);
    }

    /**
     *
     * @param lang
     * @return
     */
    public static boolean fromSupported(String lang) {
        String fromLanguages[] = {"en", "de", "it", "unk"};
        return Arrays.asList(fromLanguages).contains(lang);
    }

    /**
     *
     * @param lang
     * @return
     */
    public static boolean toSupported(String lang) {
        String toLanguages[] = {"en", "de", "it"};
        return Arrays.asList(toLanguages).contains(lang);
    }
}
