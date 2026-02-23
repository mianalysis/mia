package io.github.mianalysis.mia.gui.regions.workflowmodules;

import javax.swing.table.DefaultTableModel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.module.ModulesI;

public class DraggableTableModel extends DefaultTableModel {
    /**
     *
     */
    private static final long serialVersionUID = 3569731151635741445L;
    private ModulesI modules;

    public DraggableTableModel(Object[][] data, Object[] columnNames, ModulesI modules) {
        super(data, columnNames);
        this.modules = modules;

    }

    public void reorder(int[] fromIndices, int toIndex) {
        Module[] toMove = new Module[fromIndices.length];
        for (int i=0;i<fromIndices.length;i++) {
            toMove[i] = (Module) getValueAt(fromIndices[i],0);
        }

        ModuleI moduleToFollow;
        if (toIndex == 0) moduleToFollow = null;
        else moduleToFollow = (Module) getValueAt(toIndex-1,0);

        modules.reorder(fromIndices, toIndex);
        // modules.reorderByModules(toMove,moduleToFollow);

        GUI.updateModules(true, moduleToFollow);
        GUI.updateParameters(false, null);

    }
}
