package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ReferenceEditingMenu;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;

/**
 * Created by Stephen on 06/09/2017.
 */
public class ExportName extends JLabel implements MouseListener {
    /**
     *
     */
    private static final long serialVersionUID = 5017487558283084852L;
    private Ref reference;
    private ReferenceEditingMenu refEditingMenu ;

    public ExportName(Ref ref) {
        this.reference = ref;
        refEditingMenu = new ReferenceEditingMenu(ref);

        setText(ref.getNickname());
        addMouseListener(this);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Only display menu if the right mouse button is clicked
        if (e.getButton() != MouseEvent.BUTTON3) return;

        // Populating the list containing all available modules
        refEditingMenu.show(GUI.getFrame(), 0, 0);
        refEditingMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        refEditingMenu.setVisible(true);

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
