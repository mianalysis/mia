package io.github.mianalysis.mia.module.lostandfound;

import java.util.HashMap;
import java.util.List;

import org.scijava.plugin.PluginInfo;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.process.ClassHunter;

public class LostAndFound {
    private HashMap<String, String> lostModules = new HashMap<>();
    private HashMap<String, HashMap<String, String>> lostParameterNames = new HashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, String>>> lostParameterValues = new HashMap<>();

    public LostAndFound() {
        new ClassHunter<LostAndFoundItem>();
        List<PluginInfo<LostAndFoundItem>> lostAndFoundItems = ClassHunter.getPlugins(LostAndFoundItem.class);
            
        for (PluginInfo<LostAndFoundItem> lostAndFoundItem : lostAndFoundItems) {
            try {
                LostAndFoundItem item = lostAndFoundItem.createInstance();

                String moduleName = item.getModuleName();

                for (String previousName : item.getPreviousModuleNames())
                    lostModules.put(previousName, moduleName);

                lostParameterNames.put(moduleName, item.getPreviousParameterNames());
                lostParameterValues.put(moduleName, item.getPreviousParameterValues());

            } catch (Exception e) {
                MIA.log.writeError(e);
            }
        }        
    }

    public LostAndFound(HashMap<String, String> lostModules,
            HashMap<String, HashMap<String, String>> lostParameterNames,
            HashMap<String, HashMap<String, HashMap<String, String>>> lostParameterValues) {
                this.lostModules = lostModules;
                this.lostParameterNames = lostParameterNames;
                this.lostParameterValues = lostParameterValues;
                
    }

    public String findModule(String oldName) {
        String newName = lostModules.get(oldName);

        // If this module isn't in the lost and found, its new and old names should be
        // the same
        if (newName == null)
            newName = oldName;

        if (!newName.equals(oldName))
            newName = findModule(newName);

        return newName;

    }

    public String findParameter(String moduleSimpleName, String oldName) {
        String finalModuleName = findModule(moduleSimpleName);

        // If no name is found, return the old name
        HashMap<String, String> currentParameters = lostParameterNames.get(finalModuleName);
        if (currentParameters == null)
            return oldName;

        String newName = currentParameters.get(oldName);
        if (newName == null)
            return oldName;
        else
            return newName;

    }

    public String findParameterValue(String moduleSimpleName, String parameterName, String oldValue) {
        String finalModuleName = findModule(moduleSimpleName);
        String finalParameterName = findParameter(finalModuleName, parameterName);

        HashMap<String, HashMap<String, String>> currentParameters = lostParameterValues.get(finalModuleName);
        if (currentParameters == null)
            return oldValue;

        HashMap<String, String> currentValues = currentParameters.get(finalParameterName);
        if (currentValues == null)
            return oldValue;

        String newValue = currentValues.get(oldValue);
        if (newValue == null)
            return oldValue;
        else
            return newValue;
    }

    public void addLostModuleAssignment(String oldName, String newName) {
        lostModules.put(oldName, newName);

    }

    public void addLostParameterAssignment(String moduleName, String oldName, String newName) {
        HashMap<String, String> currentParameters = lostParameterNames.putIfAbsent(moduleName, new HashMap<>());
        currentParameters.put(oldName, newName);

    }
}
// <3 Ada and Evelyn