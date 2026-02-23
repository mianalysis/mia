package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.formdev.flatlaf.FlatClientProperties;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.svg.SVGButton;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;
import java.awt.Color;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledButton extends SVGButton implements ActionListener {
    private static final int size = 18;

    private Module module;
    // private static final ImageIcon blackIcon = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_black_strike_12px.png"),
    // "");
    // private static final ImageIcon blackIconDM = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_blackDM_strike_12px.png"),
    // "");
    // private static final ImageIcon redIcon = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_red_12px.png"), "");
    // private static final ImageIcon redIconDM = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_redDM_12px.png"), "");
    // private static final ImageIcon orangeIcon = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_orange_12px.png"), "");
    // private static final ImageIcon orangeIconDM = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_orangeDM_12px.png"), "");
    // private static final ImageIcon greenIcon = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_green_12px.png"), "");
    // private static final ImageIcon greenIconDM = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_greenDM_12px.png"), "");
    // private static final ImageIcon darkBlueIcon = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_darkblue_12px.png"), "");
    // private static final ImageIcon darkBlueIconDM = new ImageIcon(
    // ModuleEnabledButton.class.getResource("/icons/power_darkblueDM_12px.png"),
    // "");

    public ModuleEnabledButton(Module module) {
        super(new String[] { "/icons/poweron.svg", "/icons/poweroff.svg" }, size, module.isEnabled() ? 0 : 1);

        this.module = module;

        addActionListener(this);
        setName("ModuleEnabled");
        setToolTipText("Enable/disable module");

        updateState();

    }

    public void updateState() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (module instanceof GUISeparator)
            dynamicForegroundColor.setColor(Colours.getDarkBlue(isDark));
        else if (module.isEnabled() && module.isReachable() && module.isRunnable())
            dynamicForegroundColor.setColor(Colours.getGreen(isDark));
        else if (module.isEnabled() && !module.isReachable())
            dynamicForegroundColor.setColor(Colours.getOrange(isDark));
        else if (module.isEnabled() && !module.isRunnable())
            dynamicForegroundColor.setColor(Colours.getRed(isDark));
        else
            dynamicForegroundColor.setColor(Color.GRAY);

    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Invert state
        module.setEnabled(!module.isEnabled());

        updateState();

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval())
            GUI.setLastModuleEval(idx - 1);

        // If this is a GUISeparator module, disable all modules after it, until the
        // next separator
        ModulesI modules = GUI.getModules();
        if (module.getClass().isInstance(new GUISeparator(modules))) {
            for (int i = idx + 1; i < modules.size(); i++) {
                Module currentModule = modules.getAtIndex(i);
                if (currentModule.getClass().isInstance(new GUISeparator(modules))) {
                    break;
                } else {
                    currentModule.setEnabled(module.isEnabled());
                }
            }
        }

        GUI.updateModules(true, module);
        GUI.updateParameters(false, null);

        updateState();

    }
}
