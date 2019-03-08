package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 06/09/2017.
 */
public class ResetModuleName extends JButton implements ActionListener {
    private Module module;

    private static final ImageIcon refreshIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/refresh_black_12px.png"), "");

    public ResetModuleName(Module module) {
        this.module = module;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("Refresh module name");
        setToolTipText("Refresh module name");
        addActionListener(this);
        setIcon(refreshIcon);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        module.setNickname(module.getTitle());
        GUI.populateModuleList();
        GUI.populateModuleParameters();
        GUI.populateHelpNotes();
        GUI.populateBasicHelpNotes();
    }
}
