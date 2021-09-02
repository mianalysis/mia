package io.github.mianalysis.MIA.GUI.Regions.WorkflowModules;

import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;

import javax.swing.table.DefaultTableModel;

public class DraggableTableModel extends DefaultTableModel {
    /**
     *
     */
    private static final long serialVersionUID = 3569731151635741445L;
    private Modules modules;

    public DraggableTableModel(Object[][] data, Object[] columnNames, Modules modules) {
        super(data, columnNames);
        this.modules = modules;

    }

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
