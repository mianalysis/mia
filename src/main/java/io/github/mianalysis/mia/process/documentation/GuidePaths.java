package io.github.mianalysis.mia.process.documentation;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Keeps fixed paths to guides in one place so they can be easily updated
public class GuidePaths {
    private HashMap<String, String> paths = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(new GuidePaths().replacePaths("please see the <a href=\"${GUIDE_USEEXISTING}\">Using existing workflows<a href=\"${GUIDE_CREATENEW}\"></a> guide"));
    }

    public GuidePaths() {
        // Getting started
        paths.put("GUIDE_PROCESSINGVIEW", "${PTR}/html/guides/gettingstarted/2processingview.html");
        paths.put("GUIDE_USEEXISTING", "${PTR}/html/guides/gettingstarted/3useexisting.html");
        paths.put("GUIDE_EDITINGVIEW", "${PTR}/html/guides/gettingstarted/4editingview.html");
        paths.put("GUIDE_CREATENEW", "${PTR}/html/guides/gettingstarted/5createnew.html");
        paths.put("GUIDE_RESULTSFORMAT", "${PTR}/html/guides/gettingstarted/6resultsformat.html");
        paths.put("GUIDE_BATCHPROCESSING", "${PTR}/html/guides/gettingstarted/7batchprocessing.html");

        // General
        paths.put("GUIDE_TROUBLESHOOTING", "${PTR}/html/guides/troubleshooting.html");
        
    }

    public String replacePaths(String str) {
        // Pattern pattern = Pattern.compile("\\${(GUIDE_[^}])}");
        Pattern pattern = Pattern.compile("\\$\\{(GUIDE_[^\\}]+)\\}");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            String oldValue = matcher.group();            
            String newValue = paths.get(oldValue.substring(2, oldValue.length()-1));
            str = str.replace(oldValue, newValue);            
        }

        return str;

    }
}
