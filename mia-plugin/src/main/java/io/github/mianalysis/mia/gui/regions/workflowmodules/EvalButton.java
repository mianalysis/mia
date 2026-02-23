package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.svg.SVGButton;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.script.AbstractMacroRunner;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends SVGButton implements ActionListener {
    private static final int size = 18;

    private static Parameters previousParameters = null;

    private static Thread t;

    private Module module;
    // private static final ImageIcon blackIcon = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowopen_black_12px.png"), "");
    // private static final ImageIcon blackIconDM = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowopen_blackDM_12px.png"), "");
    private static final ImageIcon amberIcon = new ImageIcon(
            EvalButton.class.getResource("/icons/Dual Ring-1s-12px.gif"), "");
    private static final ImageIcon amberIconDM = new ImageIcon(
            EvalButton.class.getResource("/icons/Dual Ring-1s_DM-12px.gif"), "");
    // private static final ImageIcon greenIcon = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowclosed_green_12px.png"), "");
    // private static final ImageIcon greenIconDM = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowclosed_greenDM_12px.png"), "");
    // private static final ImageIcon redOpenIcon = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowopen_red_12px.png"), "");
    // private static final ImageIcon redOpenIconDM = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowopen_redDM_12px.png"), "");
    // private static final ImageIcon redClosedIcon = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowclosed_red_12px.png"), "");
    // private static final ImageIcon redClosedIconDM = new ImageIcon(
    //         EvalButton.class.getResource("/icons/arrowclosed_redDM_12px.png"), "");
    // private static final ImageIcon redStopIcon = new ImageIcon(EvalButton.class.getResource("/icons/x-mark-3-12.png"),
    //         "");

    // CONSTRUCTOR

    public EvalButton(Module module) {
        super(new String[] { "/icons/arrowdownopen.svg", "/icons/arrowdownclosed.svg", "/icons/stop.svg"}, size, 0);

        this.module = module;

        addActionListener(this);
        setName("EvalButton");
        setToolTipText("Evaluate module");        

        updateState();

    }

    public void updateState() {
        int idx = GUI.getModules().indexOf(module);

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        // If the module is being currently evaluated
        if (idx == GUI.getModuleBeingEval()) {
            // selectIconByIndex(0);
            // dynamicForegroundColor.setColor(Colours.getOrange(isDark));
            if (isDark)
                setIcon(amberIconDM);
            else
                setIcon(amberIcon);

            selectRolloverIconByIndex(2);

            return;
        }

        if (idx <= GUI.getLastModuleEval()) {
            if (module.isRunnable()) {
                selectIconByIndex(1);
                dynamicForegroundColor.setColor(Colours.getGreen(isDark));
                // if (isDark) {
                //     setIcon(greenIconDM);
                //     setRolloverIcon(greenIconDM);
                // } else {
                //     setIcon(greenIcon);
                //     setRolloverIcon(greenIcon);
                // }
            } else {
                selectIconByIndex(1);
                dynamicForegroundColor.setColor(Colours.getRed(isDark));
                // if (isDark) {
                //     setIcon(redClosedIconDM);
                //     setRolloverIcon(redClosedIconDM);
                // } else {
                //     setIcon(redClosedIcon);
                //     setRolloverIcon(redClosedIcon);
                // }
            }
        } else {
            if (module.isRunnable()) {
                selectIconByIndex(0);
                dynamicForegroundColor.setColor(Colours.getBlack(isDark));
                // if (isDark) {
                //     setIcon(blackIconDM);
                //     setRolloverIcon(blackIconDM);
                // } else {
                //     setIcon(blackIcon);
                //     setRolloverIcon(blackIcon);
                // }
            } else {
                selectIconByIndex(0);
                dynamicForegroundColor.setColor(Colours.getRed(isDark));
                // if (isDark) {
                //     setIcon(redOpenIconDM);
                //     setRolloverIcon(redOpenIconDM);
                // } else {
                //     setIcon(redOpenIcon);
                //     setRolloverIcon(redOpenIcon);
                // }
            }
        }

        setEnabled(module.isEnabled() && module.isRunnable());
        if (!isEnabled())
            dynamicForegroundColor.setColor(Colours.getGrey(isDark));            
    
    }

    // GETTERS

    public Module getModule() {
        return module;
    }

    private boolean checkForTestFileUpdate(Parameters currentParameters, Parameters previousParameters) {
        // This is the first load, so check for new file
        if (previousParameters == null)
            return true;
        
        // Checking each parameter to see if it's the same as before
        for (String currentParameterName : currentParameters.keySet()) {
            Parameter currentParameter = currentParameters.get(currentParameterName);

            if (!previousParameters.containsKey(currentParameterName))
                return true;
            
            Parameter previousParameter = previousParameters.get(currentParameterName);

            // Comparing values
            if (previousParameter instanceof ParameterGroup) {
                ParameterGroup previousGroup = (ParameterGroup) previousParameter;
                ParameterGroup currentGroup = (ParameterGroup) currentParameter;

                LinkedHashMap<Integer, Parameters> previousCollection = previousGroup.getCollections(true);
                LinkedHashMap<Integer, Parameters> currentCollection = currentGroup.getCollections(true);

                if (previousCollection.size() != currentCollection.size())
                    return true;
                
                // Comparing collection (assumes they're in the same order)
                for (int collectionIndex:currentCollection.keySet()) {
                    Parameters currentCollectionParameters = currentCollection.get(collectionIndex);

                    if (!previousCollection.containsKey(collectionIndex))
                        return true;

                    Parameters previousCollectionParameters = previousCollection.get(collectionIndex);

                    if (checkForTestFileUpdate(currentCollectionParameters, previousCollectionParameters))
                        return true;
                                       
                }

            } else {
                if (!previousParameter.getRawStringValue().equals(currentParameter.getRawStringValue()))
                    return true;
                
            }
        }

        return false;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ModulesI modules = GUI.getModules();

        // Checking if InputControl has changed since last eval
        Parameters currentParameters = modules.getInputControl().getAllParameters();
        boolean reload = checkForTestFileUpdate(currentParameters, previousParameters);

        if (reload) {
            previousParameters = currentParameters.duplicate();

            // Make first module look like it's being evaluated while the test file updates
            GUI.setLastModuleEval(-1);
            if (modules.size() > 2)
                GUI.setModuleBeingEval(0);
            GUI.updateModuleStates(false, null);
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
        if (idx == GUI.getModuleBeingEval() & !reload) {
            MIA.log.writeStatus("Stopping");
            GUI.setModuleBeingEval(-1);
            GUI.updateModuleStates(false, null);
            t.stop();
            return;
        }

        // Transferring MacroHandler back to test workspace
        if (modules.hasModuleMatchingType(AbstractMacroRunner.class)) {
            MacroHandler.setWorkspace(GUI.getTestWorkspace());
            MacroHandler.setModules(GUI.getModules());
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
                    Module module = GUI.getModules().getAtIndex(i);
                    if (module.isEnabled() && module.isRunnable()) {
                        try {
                            if (!evaluateModule(module)) {
                                GUI.updateModuleStates(false, null);
                                break;
                            }
                        } catch (Exception e1) {
                            GUI.setModuleBeingEval(-1);
                            GUI.updateModuleStates(false, null);
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
        ModulesI modules = GUI.getModules();
        WorkspaceI testWorkspace = GUI.getTestWorkspace();

        // Setting the index to the previous module. This will make the
        // currently-evaluated module go red
        GUI.setLastModuleEval(modules.indexOf(module) - 1);
        GUI.setModuleBeingEval(modules.indexOf(module));
        GUI.updateModuleStates(false, null);

        // Module.setVerbose(true);
        boolean status = true;
        Status success = module.execute(testWorkspace);
        switch (success) {
            case PASS:
                GUI.setLastModuleEval(modules.indexOf(module));
                status = true;
                break;
            case REDIRECT:
                // Getting index of module before one to move to
                Module redirectModule = modules.getModuleByID(module.getRedirectModuleID(testWorkspace));
                
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
        GUI.updateModuleStates(false, null);

        return status;

    }
}
