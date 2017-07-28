package wbif.sjx.ModularImageAnalysis.GUI;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class FileParameter extends JButton implements ActionListener {
    private MainGUI gui;
    private HCModule module;
    private Parameter parameter;

    FileParameter(MainGUI gui, HCModule module, Parameter parameter) {
        this.gui = gui;
        this.module = module;
        this.parameter = parameter;

        setText(FilenameUtils.getName(parameter.getValue()));
        addActionListener(this);
        setFocusPainted(false);

    }

    public HCModule getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileDialog fileDialog = new FileDialog(new Frame(), "Select image to load", FileDialog.LOAD);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);

        parameter.setValue(fileDialog.getFiles()[0].getAbsolutePath());
        setText(FilenameUtils.getName(parameter.getValue()));

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) gui.setLastModuleEval(idx-1);

        if (gui.isBasicGUI()) {
            gui.populateBasicModules();
        } else {
            gui.populateModuleList();
        }
    }
}
