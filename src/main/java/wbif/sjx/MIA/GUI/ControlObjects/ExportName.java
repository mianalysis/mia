package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 06/09/2017.
 */
public class ExportName extends JTextField implements FocusListener {
    private ExportableRef measurementReference;

    public ExportName(ExportableRef ref) {
        this.measurementReference = ref;

        setText(ref.getNickname());
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        measurementReference.setNickname(getText());
        GUI.updateModules();
        GUI.populateModuleList();
        GUI.updateModuleStates(true);

    }
}
