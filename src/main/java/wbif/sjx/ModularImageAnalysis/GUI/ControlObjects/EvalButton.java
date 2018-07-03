package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import ij.IJ;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton implements ActionListener {
    private GUI gui;
    private Module module;
    private static final ImageIcon redIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowopen_black_12px.png"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowclosed_green_12px.png"), "");


    // CONSTRUCTOR

    public EvalButton(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("EvalButton");
        setToolTipText("Evaluate module");
        addActionListener(this);
        updateColour();

        setEnabled(module.isEnabled());

    }

    public void updateColour() {
        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) {
            setIcon(greenIcon);
        } else {
            setIcon(redIcon);
        }
    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!module.isEnabled()) return;

        int idx = gui.getModules().indexOf(module);

        // If the module is ready to be evaluated
        if (idx <= gui.getLastModuleEval()) new Thread(() -> {
            try {
                // For some reason it's necessary to have a brief pause here to prevent the module executing twice
                Thread.sleep(1);
                gui.evaluateModule(module);
            } catch (GenericMIAException ex) {
                IJ.showMessage(ex.getMessage());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }).start();

        // If multiple modules will need to be evaluated first
        new Thread(() -> {
            for (int i = gui.getLastModuleEval()+1;i<=idx;i++) {
                Module module = gui.getModules().get(i);
                if (module.isEnabled()) try {
                    gui.evaluateModule(module);
                } catch (GenericMIAException ex) {
                    IJ.showMessage(ex.getMessage());
                }

            }
        }).start();
    }
}
