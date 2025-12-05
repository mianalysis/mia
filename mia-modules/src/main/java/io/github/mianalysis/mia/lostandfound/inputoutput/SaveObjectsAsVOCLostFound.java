package io.github.mianalysis.mia.lostandfound.inputoutput;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.inputoutput.SaveObjectsAsVOC;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class SaveObjectsAsVOCLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new SaveObjectsAsVOC(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "Export VOC annotations" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String, String> parameterNames = new HashMap<String, String>();

        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, HashMap<String, String>> parameterValues = null;

        return parameterValues;

    }
}
