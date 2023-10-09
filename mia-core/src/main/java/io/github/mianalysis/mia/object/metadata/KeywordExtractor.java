package io.github.mianalysis.mia.object.metadata;

import java.util.StringTokenizer;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class KeywordExtractor {
    private static final String name = "Keyword";
    private static String[] keywordArray;

    public KeywordExtractor(String keywords) {
        StringTokenizer tokenizer = new StringTokenizer(keywords,",");
        keywordArray = new String[tokenizer.countTokens()];

        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            keywordArray[i++] = tokenizer.nextToken().trim();
        }
    }

    public String getName() {
        return name;
    }

    public boolean extract(Metadata result, String str) {
        int length = Integer.MIN_VALUE;
        String currentKeyword = "";
        for (String keyword:keywordArray) {
            if (str.contains(keyword) && keyword.length() > length) {
                currentKeyword = keyword;
                length = keyword.length();
            }
        }

        if (currentKeyword.equals("")) {
            result.put(Metadata.KEYWORD,"");
            return false;
        }

        result.put(Metadata.KEYWORD,currentKeyword);
        return true;

    }
}
