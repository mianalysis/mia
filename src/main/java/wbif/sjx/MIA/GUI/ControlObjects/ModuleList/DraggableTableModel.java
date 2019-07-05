package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;

import javax.swing.table.DefaultTableModel;

public class DraggableTableModel extends DefaultTableModel {
    private ModuleCollection modules;

    public DraggableTableModel(Object[][] data, Object[] columnNames, ModuleCollection modules) {
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
