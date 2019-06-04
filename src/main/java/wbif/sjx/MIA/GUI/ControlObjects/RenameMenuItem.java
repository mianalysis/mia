package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.Object.References.Abstract.Ref;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RenameMenuItem extends JMenuItem implements ActionListener {
    private final Ref reference;

    public RenameMenuItem(Ref reference) {
        this.reference = reference;

        setText("Rename");
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame frame = new JFrame();


    }
}
