package io.github.mianalysis.mia.gui.regions.availablemodulelist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.moduledependencies.Dependency;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class ListUnavailableModules extends JMenuItem implements ActionListener {
    private int count = 0;
    
    public ListUnavailableModules() {
        List<String> detectedModuleNames = AvailableModules.getModuleNames(false);
        for (String detectedModuleName : detectedModuleNames) {
            String shortName = detectedModuleName.substring(detectedModuleName.lastIndexOf(".") + 1);
            if (!MIA.getDependencies().compatible(shortName, false))    
                count++;
        }

        if (count == 1)
        setText(count+" unavailable module");
        else
        setText(count+" unavailable modules");

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
        if (isDark)
            setIcon(new ImageIcon(SearchForModuleItem.class.getResource("/icons/alert_darkgreyDM_12px.png"), ""));
        else
            setIcon(new ImageIcon(SearchForModuleItem.class.getResource("/icons/alert_black_12px.png"), ""));
        setFont(GUI.getDefaultFont().deriveFont(14f));
        addActionListener(this);
    }

    public int getUnavailableCount() {
        return count;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MIA.log.writeMessage("The following modules could not be loaded due to missing/incompatible dependencies:");
        
        List<String> detectedModuleNames = AvailableModules.getModuleNames(false);
        for (String detectedModuleName : detectedModuleNames) {
            String shortName = detectedModuleName.substring(detectedModuleName.lastIndexOf(".") + 1);
            // Checking dependencies have been met
            if (!MIA.getDependencies().compatible(shortName, false)) {
                MIA.log.writeMessage("Module \"" + shortName + "\":");
                for (Dependency dependency : MIA.getDependencies().getDependencies(shortName, false))
                    if (!dependency.test()) {
                        MIA.log.writeMessage("    Requirement: " + dependency.toString());
                        MIA.log.writeMessage("    Message: " + dependency.getMessage());
                    }
                count++;
            }
        }

        

    }
}
