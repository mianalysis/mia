package io.github.mianalysis.mia.lostandfound.objects.relate;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.relate.TrackObjects;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class TrackObjectsLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new TrackObjects(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Preferred direction (-180 to 180 degs)", TrackObjects.PREFERRED_DIRECTION);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
