package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import io.github.mianalysis.mia.module.Modules;

public class ModuleTransfer implements Transferable, ClipboardOwner {
    private Modules modules;

    public ModuleTransfer(Modules modules) {
        this.modules = modules;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        try {
            DataFlavor dataFlavor = new ModuleDataFlavor();
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
    public Object getTransferData(DataFlavor flavor) {
        return modules;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }
}
