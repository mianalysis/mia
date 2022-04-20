package io.github.mianalysis.mia.gui.regions.filelist;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class FileListTable extends JTable {
    private static boolean updated = false;

    public FileListTable(TableModel dm) {
            super(dm);        
    }

    @Override
    public void updateUI() {
        // For Java 8 we need a custom updateUI method to prevent a StackOverflowError when updating the LookAndFeel.
        if (!updated) {
            updated = true;
            super.updateUI();                    
            updated = false;
        }
    }
}
