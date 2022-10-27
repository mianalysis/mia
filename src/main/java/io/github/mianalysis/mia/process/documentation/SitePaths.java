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
        paths.put("PATH_GETTINGSTARTED", "${PTR}/guides/gettingstarted.html");
        paths.put("PATH_EXAMPLEWORKFLOWS", "${PTR}/guides/exampleworkflows.html");
        paths.put("PATH_MIASTRUCTURE", "${PTR}/guides/miastructure.html");
        paths.put("PATH_PROCESSINGVIEW", "${PTR}/guides/processingview.html");
        paths.put("PATH_USEEXISTING", "${PTR}/guides/useexisting.html");
        paths.put("PATH_EDITINGVIEW", "${PTR}/guides/editingview.html");
        paths.put("PATH_CREATENEW", "${PTR}/guides/createnew.html");
        paths.put("PATH_RESULTSFORMAT", "${PTR}/guides/resultsformat.html");
        paths.put("PATH_BATCHPROCESSING", "${PTR}/guides/batchprocessing.html");
        paths.put("PATH_RUNNINGHEADLESS", "${PTR}/guides/runningheadless.html");
        paths.put("PATH_TROUBLESHOOTING", "${PTR}/guides/troubleshooting.html");
        
        paths.put("PATH_MODULE_LIST", "${PTR}/modules/modules.html");
        paths.put("PATH_MODULE_INPUTCONTROL", "${PTR}/modules/core/inputcontrol.html");
        paths.put("PATH_MODULE_OUTPUTCONTROL", "${PTR}/modules/core/outputcontrol.html");
        paths.put("PATH_MODULE_INPUTOUTPUT", "${PTR}/modules/inputoutput/inputoutput.html");        
        paths.put("PATH_MODULE_LOADIMAGE", "${PTR}/modules/inputoutput/loadimage.html");        

        paths.put("PATH_PUBLICATIONS", "${PTR}/publications.html");
        
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
