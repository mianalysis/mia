package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;

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
    private static final ImageIcon expandedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/downarrow_blue_12px.png"), "");
    private static final ImageIcon collapsedLeftIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/rightarrow_blue_12px.png"), "");
    private static final ImageIcon collapsedRightIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/leftarrow_blue_12px.png"), "");


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
        if (module.getParameterValue(GUISeparator.EXPANDED_EDITING)) {
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
        boolean expanded = module.getParameterValue(GUISeparator.EXPANDED_EDITING);
        module.updateParameterValue(GUISeparator.EXPANDED_EDITING,!expanded);
        GUI.populateModuleList();
        GUI.populateModuleParameters();
    }
}
