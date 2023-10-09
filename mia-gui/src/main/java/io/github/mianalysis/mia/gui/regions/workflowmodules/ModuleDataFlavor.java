package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.datatransfer.DataFlavor;

public class ModuleDataFlavor extends DataFlavor {
    public ModuleDataFlavor() throws ClassNotFoundException {
        super(DataFlavor.javaJVMLocalObjectMimeType + ";class=io.github.mianalysis.MIA.Module.Modules");

    }
}
