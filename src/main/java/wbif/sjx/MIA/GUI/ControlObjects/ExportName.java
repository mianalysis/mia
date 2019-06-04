package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.Object.References.Abstract.Ref;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Stephen on 06/09/2017.
 */
public class ExportName extends JLabel implements MouseListener {
    private Ref reference;
    private RenameListMenu renameListMenu ;

    public ExportName(Ref ref) {
        this.reference = ref;
        renameListMenu = new RenameListMenu(ref);

        setText(ref.getNickname());
        addMouseListener(this);

    }

//    @Override
//    public void focusGained(FocusEvent e) {
//
//    }
//
//    @Override
//    public void focusLost(FocusEvent e) {
//        reference.setNickname(getText());
//
//        GUI.updateModules();
//        GUI.populateModuleList();
//        GUI.updateModuleStates(true);
//
//    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Only display menu if the right mouse button is clicked
        if (e.getButton() != MouseEvent.BUTTON3) return;

        // Populating the list containing all available modules
        renameListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        renameListMenu.setVisible(true);

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
