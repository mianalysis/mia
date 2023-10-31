package io.github.mianalysis.mia.lostandfound.objects.relate;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.relate.RelateManyToOne;

public class RelateManyToOneLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new RelateManyToOne(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Reference point", RelateManyToOne.REFERENCE_MODE);
        parameterNames.put("Minimum percentage overlap", RelateManyToOne.MINIMUM_OVERLAP);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
