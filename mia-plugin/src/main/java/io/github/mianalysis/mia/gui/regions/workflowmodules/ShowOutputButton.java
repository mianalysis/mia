package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.svg.SVGButton;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ShowOutputButton extends SVGButton implements ActionListener {
    private static final int size = 18;
    private Module module;
    private boolean showOutput = true;

    public ShowOutputButton(Module module) {
        super(new String[] { "/icons/eyeopen.svg", "/icons/eyeclosed.svg" }, size, module.canShowOutput() ? 0 : 1);

        this.module = module;
        this.showOutput = module.canShowOutput();

        addActionListener(this);
        setName("Show output");
        setToolTipText("Show output from module");

        updateState();

    }

    @Override
    public void updateState() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (showOutput)
            selectIconByIndex(0);
        else
            selectIconByIndex(1);

        if ((module.isEnabled()) && module.isReachable() && module.isRunnable())
            dynamicForegroundColor.setColor(Colours.getDarkGrey(isDark));
        else if ((module.isEnabled()) & !module.isReachable())
            dynamicForegroundColor.setColor(Colours.getOrange(isDark));
        else if ((module.isEnabled()) & !module.isRunnable())
            dynamicForegroundColor.setColor(Colours.getRed(isDark));
        else if (!module.isEnabled())
            dynamicForegroundColor.setColor(Color.GRAY);

    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        // Invert state
        showOutput = !showOutput;

        updateState();

        module.setShowOutput(showOutput);

    }
}
