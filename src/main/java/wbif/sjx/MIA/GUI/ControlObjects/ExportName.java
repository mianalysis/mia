package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 06/09/2017.
 */
public class ExportName extends JTextField implements FocusListener {
    private Ref reference;

    public ExportName(Ref ref) {
        this.reference = ref;

        setText(ref.getNickname());
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        reference.setNickname(getText());

        GUI.updateModules();
        GUI.populateModuleList();
        GUI.updateModuleStates(true);

    }
}
