package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import java.awt.datatransfer.DataFlavor;

public class ModuleCollectionDataFlavor extends DataFlavor {
    public ModuleCollectionDataFlavor() throws ClassNotFoundException {
        super(DataFlavor.javaJVMLocalObjectMimeType + ";class=wbif.sjx.MIA.Object.ModuleCollection");

    }
}
