package io.github.mianalysis.MIA.GUI.Regions.WorkflowModules;

import java.awt.datatransfer.DataFlavor;

public class ModuleCollectionDataFlavor extends DataFlavor {
    public ModuleCollectionDataFlavor() throws ClassNotFoundException {
        super(DataFlavor.javaJVMLocalObjectMimeType + ";class=io.github.mianalysis.MIA.Module.ModuleCollection");

    }
}
