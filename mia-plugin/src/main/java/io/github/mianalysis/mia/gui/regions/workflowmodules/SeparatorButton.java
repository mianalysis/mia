package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.system.SwingPreferences;

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
    private static final ImageIcon expandedIcon = new ImageIcon(
            SeparatorButton.class.getResource("/icons/downarrow_darkblue_12px.png"), "");
    private static final ImageIcon expandedIconDM = new ImageIcon(
            SeparatorButton.class.getResource("/icons/downarrow_darkblueDM_12px.png"), "");
    private static final ImageIcon collapsedLeftIcon = new ImageIcon(
            SeparatorButton.class.getResource("/icons/rightarrow_darkblue_12px.png"), "");
    private static final ImageIcon collapsedLeftIconDM = new ImageIcon(
            SeparatorButton.class.getResource("/icons/rightarrow_darkblueDM_12px.png"), "");
    private static final ImageIcon collapsedRightIcon = new ImageIcon(
            SeparatorButton.class.getResource("/icons/leftarrow_darkblue_12px.png"), "");
    private static final ImageIcon collapsedRightIconDM = new ImageIcon(
            SeparatorButton.class.getResource("/icons/leftarrow_darkblueDM_12px.png"), "");

    public SeparatorButton(Module module, boolean left) {
        this.module = module;
        this.left = left;

        addActionListener(this);
        setFocusPainted(false);
        setSelected(false);
        setBorderPainted(false);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setMargin(new Insets(0, 0, 0, 0));
        setName("Show output");
        setToolTipText("Show output from module");
        setIcon();

    }

    public void setIcon() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        BooleanP expandedEditing = (BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING);
        if (expandedEditing.isSelected()) {
            if (isDark)
                setIcon(expandedIconDM);
            else
                setIcon(expandedIcon);
        } else {
            if (left) {
                if (isDark)
                    setIcon(collapsedLeftIconDM);
                else
                    setIcon(collapsedLeftIcon);
            } else {
                if (isDark)
                    setIcon(collapsedRightIconDM);
                else
                    setIcon(collapsedRightIcon);
            }
        }
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING)).flipBoolean();

        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);
        GUI.addUndo();

    }
}
