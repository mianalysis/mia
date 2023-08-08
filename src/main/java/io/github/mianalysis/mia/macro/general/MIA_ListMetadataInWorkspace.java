package io.github.mianalysis.mia.macro.general;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.metadata.Metadata;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_ListMetadataInWorkspace extends MacroOperation {
    public MIA_ListMetadataInWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        Metadata metadata = workspace.getMetadata();
        for (String metadataName:metadata.keySet()) {
            if (row != 0) rt.incrementCounter();

            rt.setValue("Metadata name",row,metadataName);
            rt.setValue("Metadata value",row,metadata.getAsString(metadataName));

            row++;

        }

        rt.show("Metadata in workspace");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Returns a list of metadata values currently in the workspace.";
    }
}
