package wbif.sjx.MIA.Process.AnalysisHandling;

import java.util.HashMap;

public class LostAndFound {
    private HashMap<String, String> lostModules = new HashMap<>();


    public LostAndFound() {
        // Populating hard-coded module reassignments
        lostModules.put("ConditionalAnalysisTermination", "WorkflowHandling");

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

    public void addLostModuleAssignment(String oldName, String newName) {
        lostModules.put(oldName, newName);

    }
}