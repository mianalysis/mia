package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 5495286052011521092L;

    private static Thread t;

    private Module module;
    private static final ImageIcon blackIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowopen_black_12px.png"), "");
    private static final ImageIcon greyIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowopen_lightgrey_12px.png"), "");
    private static final ImageIcon amberIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/Dual Ring-1s-12px.gif"), "");
    private static final ImageIcon greenIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowclosed_green_12px.png"), "");
    private static final ImageIcon redOpenIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowopen_red_12px.png"), "");
    private static final ImageIcon redClosedIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowclosed_red_12px.png"), "");
    private static final ImageIcon redStopIcon = new ImageIcon(EvalButton.class.getResource("/icons/x-mark-3-12.png"),
            "");

    // CONSTRUCTOR

    public EvalButton(Module module) {
        this.module = module;

        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        setSelected(false);
        setName("EvalButton");
        setToolTipText("Evaluate module");
        addActionListener(this);
        updateState();

    }

    public void updateState() {
        int idx = GUI.getModules().indexOf(module);

        // If the module is being currently evaluated
        if (idx == GUI.getModuleBeingEval()) {
            setIcon(amberIcon);
            setRolloverIcon(redStopIcon);
            return;
        }

        if (idx <= GUI.getLastModuleEval()) {
            if (module.isRunnable()) {
                setIcon(greenIcon);
                setRolloverIcon(greenIcon);
            } else {
                setIcon(redClosedIcon);
                setRolloverIcon(redClosedIcon);
            }
        } else {
            if (module.isRunnable()) {
                if (MIA.preferences.darkThemeEnabled()) {
                    setIcon(greyIcon);
                    setRolloverIcon(greyIcon);
                } else {
                    setIcon(blackIcon);
                    setRolloverIcon(blackIcon);
                }
            } else {
                setIcon(redOpenIcon);
                setRolloverIcon(redOpenIcon);
            }
        }

        setEnabled(module.isEnabled() && module.isRunnable());

    }

    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Modules modules = GUI.getModules();
        int idx = modules.indexOf(module);

        // If this is the first (non-GUI separator) module, reset the workspace
        int firstIdx = 0;
        for (Module module : modules.values()) {
            if (!(module instanceof GUISeparator))
                break;
            firstIdx++;
        }

        if (idx == firstIdx) {
            GUI.getTestWorkspace().clearAllImages(false);
            GUI.getTestWorkspace().clearAllObjects(false);
            GUI.getTestWorkspace().clearMetadata();

        }

        // If it's currently evaluating, this will kill the thread
        if (idx == GUI.getModuleBeingEval()) {
            MIA.log.writeStatus("Stopping");
            GUI.setModuleBeingEval(-1);
            GUI.updateModuleStates();
            t.stop();
            return;
        }

        // If the module is ready to be evaluated
        if (idx <= GUI.getLastModuleEval()) {
            t = new Thread(() -> {
                try {
                    // For some reason it's necessary to have a brief pause here to prevent the
                    // module executing twice
                    Thread.sleep(1);
                    evaluateModule(module);
                } catch (Exception e1) {
                    GUI.setModuleBeingEval(-1);
                    e1.printStackTrace();
                }
            });
            t.start();

        } else {
            // If multiple modules will need to be evaluated first
            t = new Thread(() -> {
                while (idx > GUI.getLastModuleEval()) {
                    int i = GUI.getLastModuleEval() + 1;
                    Module module = GUI.getModules().get(i);
                    if (module.isEnabled() && module.isRunnable()) {
                        try {
                            if (!evaluateModule(module)) {
                                GUI.updateModuleStates();
                                break;
                            }
                        } catch (Exception e1) {
                            GUI.setModuleBeingEval(-1);
                            GUI.updateModuleStates();
                            e1.printStackTrace();
                            break;
                        }
                    } else {
                        GUI.setLastModuleEval(GUI.getLastModuleEval() + 1);
                    }
                }
            });
            t.start();
        }
    }

    public boolean evaluateModule(Module module) {
        Modules modules = GUI.getAnalysis().getModules();
        Workspace testWorkspace = GUI.getTestWorkspace();

        // Setting the index to the previous module. This will make the
        // currently-evaluated module go red
        GUI.setLastModuleEval(modules.indexOf(module) - 1);
        GUI.setModuleBeingEval(modules.indexOf(module));
        GUI.updateModuleStates();

        Module.setVerbose(true);
        boolean status = true;
        Status success = module.execute(testWorkspace);
        switch (success) {
            case PASS:
                GUI.setLastModuleEval(modules.indexOf(module));
                status = true;
                break;
            case REDIRECT:
                // Getting index of module before one to move to
                Module redirectModule = module.getRedirectModule();
                if (redirectModule == null)
                    status = true;

                GUI.setLastModuleEval(modules.indexOf(redirectModule) - 1);
                status = true;
                break;
            case FAIL:
            case TERMINATE:
            case TERMINATE_SILENT:
                status = false;
                break;
        }

        GUI.setModuleBeingEval(-1);
        GUI.updateModuleStates();

        return status;

    }
}
