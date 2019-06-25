package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class DraggableTableModel extends DefaultTableModel implements Reorderable {
    private ModuleCollection modules;

    public DraggableTableModel(Object[][] data, Object[] columnNames, ModuleCollection modules) {
        super(data, columnNames);
        this.modules = modules;

    }

    @Override
    public void reorder(int[] fromIndices, int toIndex) {
        Module[] toMove = new Module[fromIndices.length];
        for (int i=0;i<fromIndices.length;i++) {
            toMove[i] = (Module) getValueAt(fromIndices[i],0);
        }

        Module moduleToFollow;
        if (toIndex == 0) moduleToFollow = null;
        else moduleToFollow = (Module) getValueAt(toIndex-1,0);

        modules.reorder(toMove,moduleToFollow);

        GUI.updateModules();
        GUI.updateParameters();

    }
}
