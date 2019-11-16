package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Icons.IconFactory;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.BooleanP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class SeparatorButton extends JButton implements ActionListener {
    private Module module;
    private boolean left;
    private static final ImageIcon downArrow = new IconFactory().getArrowDown();
    private static final ImageIcon leftArrow = new IconFactory().getArrowLeft();
    private static final ImageIcon rightArrow = new IconFactory().getArrowRight();

    public SeparatorButton(Module module, boolean left) {
        this.module = module;
        this.left = left;

        addActionListener(this);
        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("Show output");
        setToolTipText("Show output from module");
        setIcon();

    }

    public void setIcon() {
        BooleanP expandedEditing = (BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING);
        if (expandedEditing.isSelected()) {
            setIcon(downArrow);
        } else {
            if (left) setIcon(rightArrow);
            else setIcon(leftArrow);
        }
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING)).flipBoolean();

        GUI.updateModuleList();
        GUI.updateParameters();
        GUI.updateHelpNotes();
        GUI.addUndo();

    }
}
