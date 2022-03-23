// From https://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable (Accessed 2019-04-03)

package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 * Handles drag and drop row reordering
 */
public class DraggableTransferHandler extends TransferHandler {
    /**
     *
     */
    private static final long serialVersionUID = 2203844820269362427L;
    private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
            "application/x-java-Integer;class=java.lang.Integer", "Integer Row Index");
    private JTable table = null;

    public DraggableTransferHandler(JTable table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        assert (c == table);

        return new DataHandler(table.getSelectedRow(), localObjectFlavor.getMimeType());

    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);

        return b;

    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        JTable target = (JTable) info.getComponent();
        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();

        int toIndex = dl.getRow();
        int max = table.getModel().getRowCount();
        if (toIndex < 0 || toIndex > max) toIndex = max;

        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        int[] fromIndices = table.getSelectedRows();
        if (fromIndices.length == 0) return false;

        ((DraggableTableModel) table.getModel()).reorder(fromIndices, toIndex);
        target.getSelectionModel().setSelectionInterval(-1,-1);

        return true;

    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

}
