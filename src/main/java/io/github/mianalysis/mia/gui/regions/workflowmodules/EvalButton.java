package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
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
    private static final ImageIcon blackIconDM = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowopen_blackDM_12px.png"), "");
    private static final ImageIcon amberIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/Dual Ring-1s-12px.gif"), "");
    private static final ImageIcon amberIconDM = new ImageIcon(
            EvalButton.class.getResource("/icons/Dual Ring-1s_DM-12px.gif"), "");
    private static final ImageIcon greenIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowclosed_green_12px.png"), "");
    private static final ImageIcon greenIconDM = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowclosed_greenDM_12px.png"), "");
    private static final ImageIcon redOpenIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowopen_red_12px.png"), "");
    private static final ImageIcon redOpenIconDM = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowopen_redDM_12px.png"), "");
    private static final ImageIcon redClosedIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowclosed_red_12px.png"), "");
    private static final ImageIcon redClosedIconDM = new ImageIcon(
            EvalButton.class.getResource("/icons/arrowclosed_redDM_12px.png"), "");
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
            if (MIA.getPreferences().darkThemeEnabled())
                setIcon(amberIconDM);
            else
                setIcon(amberIcon);

            setRolloverIcon(redStopIcon);

            return;
        }

        if (idx <= GUI.getLastModuleEval()) {
            if (module.isRunnable()) {
                if (MIA.getPreferences().darkThemeEnabled()) {
                    setIcon(greenIconDM);
                    setRolloverIcon(greenIconDM);
                } else {
                    setIcon(greenIcon);
                    setRolloverIcon(greenIcon);
                }
            } else {
                if (MIA.getPreferences().darkThemeEnabled()) {
                    setIcon(redClosedIconDM);
                    setRolloverIcon(redClosedIconDM);
                } else {
                    setIcon(redClosedIcon);
                    setRolloverIcon(redClosedIcon);
                }
            }
        } else {
            if (module.isRunnable()) {
                if (MIA.getPreferences().darkThemeEnabled()) {
                    setIcon(blackIconDM);
                    setRolloverIcon(blackIconDM);
                } else {
                    setIcon(blackIcon);
                    setRolloverIcon(blackIcon);
                }
            } else {
                if (MIA.getPreferences().darkThemeEnabled()) {
                    setIcon(redOpenIconDM);
                    setRolloverIcon(redOpenIconDM);
                } else {
                    setIcon(redOpenIcon);
                    setRolloverIcon(redOpenIcon);
                }
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

        // Checking a test file is specified, but not loaded or if it's different to
        // that loaded.
        String inputControlPath = modules.getInputControl().getParameterValue(InputControl.INPUT_PATH, null);
        File testWorkspaceFile = GUI.getTestWorkspace().getMetadata().getFile();
        String testWorkspacePath = testWorkspaceFile == null ? "" : testWorkspaceFile.getAbsolutePath();

        boolean reload = true;
        if (new File(inputControlPath).isFile())
            reload = !inputControlPath.equals(testWorkspacePath);
        else 
            if (testWorkspaceFile != null)
                reload = !testWorkspacePath.contains(inputControlPath);

        // || new File(inputControlPath).isDirectory() &!
        // inputControlPath.equals(testWorkspacePath)) {
        if (reload) {
            // Make first module look like it's being evaluated while the test file updates
            GUI.setLastModuleEval(-1);
            if (modules.size() > 2)
                GUI.setModuleBeingEval(0);
            GUI.updateModuleStates();
            GUI.updateTestFile(true);
        }

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
                Module redirectModule = module.getRedirectModule(testWorkspace);
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
