package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton implements ActionListener {
    private Module module;
    private static final ImageIcon blackIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowopen_black_12px.png"), "");
    private static final ImageIcon amberIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/Dual Ring-1s-12px.gif"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowclosed_green_12px.png"), "");
    private static final ImageIcon redOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowopen_red_12px.png"), "");
    private static final ImageIcon redClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/arrowclosed_red_12px.png"), "");
    private static final ImageIcon redStopIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/x-mark-3-12.png"), "");


    // CONSTRUCTOR

    public EvalButton(Module module) {
        this.module = module;

        setMargin(new Insets(0,0,0,0));
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
                setIcon(blackIcon);
                setRolloverIcon(blackIcon);
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

    }

    public void evaluateModule(Module module) {
        ModuleCollection modules = GUI.getAnalysis().getModules();
        Workspace testWorkspace = GUI.getTestWorkspace();

        // Setting the index to the previous module.  This will make the currently-evaluated module go red
        GUI.setLastModuleEval(modules.indexOf(module) - 1);
        GUI.setModuleBeingEval(modules.indexOf(module));
        GUI.updateModuleStates();

        Module.setVerbose(true);
        module.execute(testWorkspace);
        GUI.setLastModuleEval(modules.indexOf(module));
        GUI.setModuleBeingEval(-1);

        GUI.updateModuleStates();

    }
}
