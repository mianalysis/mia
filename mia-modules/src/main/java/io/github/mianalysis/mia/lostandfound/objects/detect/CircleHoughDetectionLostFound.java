package io.github.mianalysis.mia.lostandfound.objects.detect;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.detect.CircleHoughDetection;

public class CircleHoughDetectionLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new CircleHoughDetection(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"HoughObjectDetection"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        return new HashMap<String,String>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
