package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.miscellaneous.GUISeparator;
import io.github.mianalysis.mia.object.parameters.BooleanP;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class SeparatorButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -4790928465048201014L;
    private Module module;
    private boolean left;
    private static final ImageIcon expandedIcon = new ImageIcon(SeparatorButton.class.getResource("/icons/downarrow_darkblue_12px.png"), "");
    private static final ImageIcon collapsedLeftIcon = new ImageIcon(SeparatorButton.class.getResource("/icons/rightarrow_darkblue_12px.png"), "");
    private static final ImageIcon collapsedRightIcon = new ImageIcon(SeparatorButton.class.getResource("/icons/leftarrow_darkblue_12px.png"), "");


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
            setIcon(expandedIcon);
        } else {
            if (left) setIcon(collapsedLeftIcon);
            else setIcon(collapsedRightIcon);
        }
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING)).flipBoolean();

        GUI.updateModules();
        GUI.updateParameters();
        GUI.updateHelpNotes();
        GUI.addUndo();

    }
}
