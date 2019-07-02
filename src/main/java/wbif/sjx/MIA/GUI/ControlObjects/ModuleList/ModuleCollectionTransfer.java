package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.ModuleCollection;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ModuleCollectionTransfer implements Transferable {
    private ModuleCollection modules;

    public ModuleCollectionTransfer(ModuleCollection modules) {
        this.modules = modules;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        try {
            DataFlavor dataFlavor = new ModuleCollectionDataFlavor();
            return new DataFlavor[]{dataFlavor};

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            return new DataFlavor[0];
        }
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return true;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return modules;
    }
}
