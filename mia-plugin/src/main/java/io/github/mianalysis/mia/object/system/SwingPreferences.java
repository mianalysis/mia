package io.github.mianalysis.mia.object.system;

import javax.swing.JComboBox;

import ij.IJ;
import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class SwingPreferences extends Preferences {
    public static final String GUI_SEPARATOR = "GUI parameters";
    public static final String THEME = "Theme";
    public static final String SHOW_DEPRECATED = "Show deprecated modules (editing mode)";

    public interface Themes extends io.github.mianalysis.mia.gui.Themes {
    };

    public SwingPreferences(ModulesI modules) {
        super(modules);
    }

    public void setTheme() {
        String theme = parameters.getValue(THEME, null);

        Prefs.set("MIA.GUI.theme", theme);
        Prefs.savePreferences();
        IJ.showMessage("Theme will be applied after restarting Fiji");

    }

    public boolean darkThemeEnabled() {
        return io.github.mianalysis.mia.gui.Themes.isDarkTheme(parameters.getValue(THEME, null));
    }

    public boolean showDeprecated() {
        return parameters.getValue(SHOW_DEPRECATED, null);
    }

    public void setShowDeprecated(boolean showDeprecated) {
        Prefs.set("MIA.GUI.showDeprecated", showDeprecated);
        Prefs.savePreferences();
        parameters.get(SHOW_DEPRECATED).setValue(showDeprecated);
        GUI.updateAvailableModules();
    }

    public void setShowDeprecated() {
        boolean showDeprecated = parameters.getValue(SHOW_DEPRECATED, null);
        Prefs.set("MIA.GUI.showDeprecated", showDeprecated);
        Prefs.savePreferences();
        GUI.updateAvailableModules();
    }

    public void initialiseParameters() {    
        super.initialiseParameters();

        // GUI parameters
        parameters.add(new SeparatorP(GUI_SEPARATOR, this));

        Parameter parameter = new ChoiceP(THEME, this, Prefs.get("MIA.GUI.theme", Themes.FLAT_LAF_LIGHT), Themes.ALL);
        // parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText", evt -> {
        //     if (evt.getOldValue() != null)
        //         setTheme();
        // });
        parameters.add(parameter);

        parameter = new BooleanP(SHOW_DEPRECATED, this, Prefs.get("MIA.GUI.showDeprecated", false));
        // parameter.getControl().getComponent().addPropertyChangeListener("ToolTipText", evt -> {
        //     if (evt.getOldValue() != null)
        //         setShowDeprecated();
        // });
        parameters.add(parameter);

        addSwingParameterDescriptions();

    }

        @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(GUI_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THEME));
        returnedParameters.add(parameters.getParameter(SHOW_DEPRECATED));

        returnedParameters.addAll(super.updateAndGetParameters());

        return returnedParameters;

    }

    void addSwingParameterDescriptions() {
        super.addParameterDescriptions();

    parameters.get(SHOW_DEPRECATED).setDescription(
                "When selected, deprecated modules will appear in the editing view available modules list.  These modules will be marked with a strikethrough their name, but otherwise act as normal.  Note: Modules marked as deprecated will be removed from future versions of MIA.");
                
    }
        
}
