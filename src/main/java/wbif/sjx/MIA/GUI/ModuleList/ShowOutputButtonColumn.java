// From https://tips4java.wordpress.com/2009/07/12/table-button-column/ (Accessed 2019-04-03)

package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.MIA.Module.Module;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 *  The ButtonColumn class provides a renderer and an editor that looks like a
 *  JButton. The renderer and editor will then be used for a specified column
 *  in the table. The TableModel will contain the String to be displayed on
 *  the button.
 *
 *  The button can be invoked by a mouse click or by pressing the space bar
 *  when the cell has focus. Optionally a mnemonic can be set to invoke the
 *  button. When the button is invoked the provided Action is invoked. The
 *  source of the Action will be the table. The action command will contain
 *  the model row number of the button that was clicked.
 *
 */
public class ShowOutputButtonColumn extends ButtonColumn
        implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {
    private static final ImageIcon blackClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_black_12px.png"), "");
    private static final ImageIcon blackOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_black_12px.png"), "");
    private static final ImageIcon redClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_red_12px.png"), "");
    private static final ImageIcon redOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_red_12px.png"), "");
    private static final ImageIcon greyClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_grey_12px.png"), "");
    private static final ImageIcon greyOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_grey_12px.png"), "");


    /**
     *  Create the ButtonColumn to be used as a renderer and editor. The
     *  renderer and editor will automatically be installed on the TableColumn
     *  of the specified column.
     *
     *  @param table the table containing the button renderer/editor
     *  @param action the Action to be invoked when the button is invoked
     *  @param column the column to which the button renderer/editor is added
     */
    public ShowOutputButtonColumn(JTable table, Action action, int column)     {
        super(table,action,column);

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Module module = (Module) value;

        if ((module.canShowOutput() && module.isEnabled()) && module.isRunnable()) editButton.setIcon(blackOpenIcon);
        else if ((module.canShowOutput() && module.isEnabled()) &! module.isRunnable()) editButton.setIcon(redOpenIcon);
        else if (module.canShowOutput() &! module.isEnabled()) editButton.setIcon(greyOpenIcon);
        else if ((!module.canShowOutput() && module.isEnabled()) && module.isRunnable()) editButton.setIcon(blackClosedIcon);
        else if ((!module.canShowOutput() && module.isEnabled()) && module.isRunnable()) editButton.setIcon(blackClosedIcon);
        else if ((!module.canShowOutput() && module.isEnabled()) &! module.isRunnable()) editButton.setIcon(redClosedIcon);
        else if (!module.canShowOutput() &! module.isEnabled()) editButton.setIcon(greyClosedIcon);

        this.editorValue = value;
        return editButton;

    }

    //
//  Implement TableCellRenderer interface
//
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Module module = (Module) value;

        if ((module.canShowOutput() && module.isEnabled()) && module.isRunnable()) renderButton.setIcon(blackOpenIcon);
        else if ((module.canShowOutput() && module.isEnabled()) &! module.isRunnable()) renderButton.setIcon(redOpenIcon);
        else if (module.canShowOutput() &! module.isEnabled()) renderButton.setIcon(greyOpenIcon);
        else if ((!module.canShowOutput() && module.isEnabled()) && module.isRunnable()) renderButton.setIcon(blackClosedIcon);
        else if ((!module.canShowOutput() && module.isEnabled()) && module.isRunnable()) renderButton.setIcon(blackClosedIcon);
        else if ((!module.canShowOutput() && module.isEnabled()) &! module.isRunnable()) renderButton.setIcon(redClosedIcon);
        else if (!module.canShowOutput() &! module.isEnabled()) renderButton.setIcon(greyClosedIcon);

        return renderButton;

    }
}