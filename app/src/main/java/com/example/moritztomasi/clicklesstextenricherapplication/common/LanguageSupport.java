/**
 * Copyright 2015 Moritz Tomasi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.example.moritztomasi.clicklesstextenricherapplication.common;

import java.util.Arrays;
import java.util.HashMap;

/**
 * This class offers support information and conversion methods for the language codes used by the
 * application and the web service.
 */
public final class LanguageSupport {

    private LanguageSupport() {}

    /**
     * Converts a language code into a plain text string representation. If lang contains the
     * substring "detected:" the rest of lang should already be a plain text string representation,
     * and is replaced by "".
     *
     * @param lang Language code.
     * @return Coverted language code in plain text.
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
     * Checks if source language is supported.
     *
     * @param lang Language code.
     * @return True if supported, false otherwise.
     */
    public static boolean fromSupported(String lang) {
        String fromLanguages[] = {"en", "de", "it", "unk"};
        return Arrays.asList(fromLanguages).contains(lang);
    }

    /**
     * Checks if target language is supported.
     *
     * @param lang Language code.
     * @return True if supported, false otherwise.
     */
    public static boolean toSupported(String lang) {
        String toLanguages[] = {"en", "de", "it"};
        return Arrays.asList(toLanguages).contains(lang);
    }
}
