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
        paths.put("GUIDE_GETTINGSTARTED", "${PTR}/html/guides/1gettingstarted.html");
        paths.put("GUIDE_EXAMPLEWORKFLOWS", "${PTR}/html/guides/2exampleworkflows.html");
        paths.put("GUIDE_MIASTRUCTURE", "${PTR}/html/guides/3miastructure.html");
        paths.put("GUIDE_PROCESSINGVIEW", "${PTR}/html/guides/3processingview.html");
        paths.put("GUIDE_USEEXISTING", "${PTR}/html/guides/4useexisting.html");
        paths.put("GUIDE_EDITINGVIEW", "${PTR}/html/guides/5editingview.html");
        paths.put("GUIDE_CREATENEW", "${PTR}/html/guides/6createnew.html");
        paths.put("GUIDE_RESULTSFORMAT", "${PTR}/html/guides/8resultsformat.html");
        paths.put("GUIDE_BATCHPROCESSING", "${PTR}/html/guides/9batchprocessing.html");
        paths.put("GUIDE_TROUBLESHOOTING", "${PTR}/html/guides/troubleshooting.html");
        
        paths.put("GUIDE_MODULE_LIST", "${PTR}/html/modules/modules.html");
        paths.put("GUIDE_MODULE_INPUTCONTROL", "${PTR}/html/modules/core/inputcontrol.html");
        paths.put("GUIDE_MODULE_OUTPUTCONTROL", "${PTR}/html/modules/core/outputcontrol.html");
        paths.put("GUIDE_MODULE_INPUTOUTPUT", "${PTR}/html/modules/inputoutput/inputoutput.html");        
        paths.put("GUIDE_MODULE_LOADIMAGE", "${PTR}/html/modules/inputoutput/loadimage.html");        
        
        
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
