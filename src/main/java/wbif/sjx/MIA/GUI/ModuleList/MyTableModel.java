package wbif.sjx.MIA.GUI.ModuleList;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel implements Reorderable {
    public MyTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public void reorder(int[] fromIndex, int toIndex) {
        moveRow(fromIndex[0],fromIndex[fromIndex.length-1],toIndex);
    }
}
