package io.github.mianalysis.mia.lostandfound.images.transform.registration;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.transform.registration.UnwarpManual;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class UnwarpManualLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new UnwarpManual(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"ManualUnwarp"};
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
