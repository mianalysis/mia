package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class MyTableModel extends DefaultTableModel implements Reorderable {
    private ModuleCollection modules;

    public MyTableModel(Object[][] data, Object[] columnNames, ModuleCollection modules) {
        super(data, columnNames);
        this.modules = modules;

    }

    @Override
    public void reorder(int[] fromIndices, int toIndex) {
        // Creating a list of initial indices
        ArrayList<Integer> inIdx = new ArrayList<>();
        for (int i=0;i<modules.size();i++) inIdx.add(i);

        // Creating a list of the indices to move
        ArrayList<Integer> toMove = new ArrayList<>();
        for (int fromIndex:fromIndices) toMove.add(fromIndex);

        // Removing the indices to be moved
        inIdx.removeAll(toMove);

        // Iterating over all input indices, when we get to the target index, add the moved values
        ModuleCollection newModules = new ModuleCollection();
        for (int idx=0;idx<inIdx.size()+fromIndices.length+1;idx++) {
            // If this is the target, move the relevant indices, else move the current value
            if (idx == toIndex) {
                for (int toMoveIdx:toMove) newModules.add(modules.get(toMoveIdx));
            }

            if (idx < modules.size() &! toMove.contains(idx)) {
                newModules.add(modules.get(idx));
            }
        }

        GUI.getAnalysis().setModules(newModules);
        GUI.updateModules();
        GUI.updateParameters();

    }
}
