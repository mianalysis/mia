package io.github.mianalysis.mia.process.documentation;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Keeps fixed paths to guides in one place so they can be easily updated
public class SitePaths {
    private HashMap<String, String> paths = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(new SitePaths().replacePaths("please see the <a href=\"${PATH_USEEXISTING}\">Using existing workflows<a href=\"${PATH_CREATENEW}\"></a> guide"));
    }

    public SitePaths() {
        paths.put("PATH_GETTINGSTARTED", "${PTR}/html/guides/1gettingstarted.html");
        paths.put("PATH_EXAMPLEWORKFLOWS", "${PTR}/html/guides/2exampleworkflows.html");
        paths.put("PATH_MIASTRUCTURE", "${PTR}/html/guides/3miastructure.html");
        paths.put("PATH_PROCESSINGVIEW", "${PTR}/html/guides/3processingview.html");
        paths.put("PATH_USEEXISTING", "${PTR}/html/guides/4useexisting.html");
        paths.put("PATH_EDITINGVIEW", "${PTR}/html/guides/5editingview.html");
        paths.put("PATH_CREATENEW", "${PTR}/html/guides/6createnew.html");
        paths.put("PATH_RESULTSFORMAT", "${PTR}/html/guides/8resultsformat.html");
        paths.put("PATH_BATCHPROCESSING", "${PTR}/html/guides/9batchprocessing.html");
        paths.put("PATH_TROUBLESHOOTING", "${PTR}/html/guides/troubleshooting.html");
        
        paths.put("PATH_MODULE_LIST", "${PTR}/html/modules/modules.html");
        paths.put("PATH_MODULE_INPUTCONTROL", "${PTR}/html/modules/core/inputcontrol.html");
        paths.put("PATH_MODULE_OUTPUTCONTROL", "${PTR}/html/modules/core/outputcontrol.html");
        paths.put("PATH_MODULE_INPUTOUTPUT", "${PTR}/html/modules/inputoutput/inputoutput.html");        
        paths.put("PATH_MODULE_LOADIMAGE", "${PTR}/html/modules/inputoutput/loadimage.html");        

        paths.put("PATH_PUBLICATIONS", "${PTR}/html/publications.html");
        
    }

    public String replacePaths(String str) {
        // Pattern pattern = Pattern.compile("\\${(GUIDE_[^}])}");
        Pattern pattern = Pattern.compile("\\$\\{(PATH_[^\\}]+)\\}");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            String oldValue = matcher.group();            
            String newValue = paths.get(oldValue.substring(2, oldValue.length()-1));
            str = str.replace(oldValue, newValue);            
        }

        return str;

    }
}
