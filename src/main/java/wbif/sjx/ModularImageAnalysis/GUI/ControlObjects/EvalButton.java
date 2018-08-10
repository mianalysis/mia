package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import ij.IJ;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton implements ActionListener {
    private static Thread t;

    private Module module;
    private static final ImageIcon blackIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowopen_black_12px.png"), "");
    private static final ImageIcon amberIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/Dual Ring-1s-12px.gif"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowclosed_green_12px.png"), "");
    private static final ImageIcon redIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/x-mark-3-12.png"), "");


    // CONSTRUCTOR

    public EvalButton(Module module) {
        this.module = module;

        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setSelected(false);
        setName("EvalButton");
        setToolTipText("Evaluate module");
        addActionListener(this);
        updateColour();

        setEnabled(module.isEnabled());

    }

    public void updateColour() {
        int idx = GUI.getModules().indexOf(module);

        // If the module is being currently evaluated
        if (idx == GUI.getModuleBeingEval()) {
            setIcon(amberIcon);
            setRolloverIcon(redIcon);
            return;
        }

        if (idx <= GUI.getLastModuleEval()) {
            setIcon(greenIcon);
            setRolloverIcon(greenIcon);
        } else {
            setIcon(blackIcon);
            setRolloverIcon(blackIcon);
        }
    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!module.isEnabled()) return;

        int idx = GUI.getModules().indexOf(module);

        // If it's currently evaluating, this will kill the thread
        if (idx == GUI.getModuleBeingEval()) {
            System.out.println("Stopping");
            GUI.setModuleBeingEval(-1);
            t.getThreadGroup().interrupt();
            return;
        }

        // Terminating any previously-executing threads
        if (t != null) t.interrupt();

        // If the module is ready to be evaluated
        if (idx <= GUI.getLastModuleEval()) {
            t = new Thread(() -> {
                try {
                    // For some reason it's necessary to have a brief pause here to prevent the module executing twice
                    Thread.sleep(1);
                    GUI.evaluateModule(module);
                } catch (GenericMIAException ex) {
                    IJ.showMessage(ex.getMessage());
                } catch (Exception e1) {
                    GUI.setModuleBeingEval(-1);
                    GUI.updateModules();
                    e1.printStackTrace();
                }
            });
            t.start();

        } else {
            // If multiple modules will need to be evaluated first
            t = new Thread(() -> {
                for (int i = GUI.getLastModuleEval() + 1; i <= idx; i++) {
                    Module module = GUI.getModules().get(i);
                    if (module.isEnabled()) try {
                        GUI.evaluateModule(module);
                    } catch (GenericMIAException ex) {
                        IJ.showMessage(ex.getMessage());
                    } catch (Exception e1) {
                        GUI.setModuleBeingEval(-1);
                        GUI.updateModules();
                        e1.printStackTrace();
                        Thread.currentThread().getThreadGroup().interrupt();
                    }
                }
            });
            t.start();
        }
    }
}
