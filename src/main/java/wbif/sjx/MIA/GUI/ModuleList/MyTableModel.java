package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class MyTableModel extends DefaultTableModel implements Reorderable {
    ModuleCollection modules;

    public MyTableModel(Object[][] data, Object[] columnNames, ModuleCollection modules) {
        super(data, columnNames);
        this.modules = modules;

    }

    @Override
    public void reorder(int[] fromIndex, int toIndex) {
        moveRow(fromIndex[0],fromIndex[fromIndex.length-1],toIndex);
        GUI.updateModules();MIA.log.writeDebug("Moving row(s): ["+Arrays.toString(fromIndex)+"] to "+toIndex);

    }
}
