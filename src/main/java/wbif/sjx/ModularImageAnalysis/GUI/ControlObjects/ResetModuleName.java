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

    public ResetModuleName(Module module) {
        this.module = module;

        setText("Reset name");
        setFocusPainted(false);
        setPreferredSize(new Dimension(100, 25));
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        module.setNickname(module.getTitle());
        GUI.populateModuleList();
        GUI.populateModuleParameters();
    }
}
