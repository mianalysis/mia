package io.github.mianalysis.MIA.Object;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ij.Prefs;
import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.GenericButtonP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;

/**
 * Created by Stephen on 24/08/2021.
 */
public class Preferences extends Module {
    public static final String GUI_SEPARATOR = "GUI parameters";
    public static final String SHOW_DEPRECATED = "Show deprecated modules (editing mode)";

    public static final String UPDATE_SEPARATOR = "Update";
    public static final String UPDATE_PARAMETERS = "Update parameters";

    public boolean showDeprecated() {
        return parameters.getValue(SHOW_DEPRECATED);
    }

    public void setShowDeprecated(boolean showDeprecated) {
        Prefs.set("MIA.GUI.showDeprecated",showDeprecated);
        parameters.getParameter(SHOW_DEPRECATED).setValue(showDeprecated);
        GUI.updateAvailableModules();
    }

    public Preferences(Modules modules) {
        super("Preferences", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.CORE;
    }

    @Override
    public String getDescription() {
        return "General MIA parameters.";
    }

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(GUI_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_DEPRECATED, this, Prefs.get("MIA.GUI.showDeprecated", false)));
        
        parameters.add(new SeparatorP(UPDATE_SEPARATOR, this));
        parameters.add(new GenericButtonP(UPDATE_PARAMETERS, this, UPDATE_PARAMETERS, new Update()));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(SHOW_DEPRECATED).setDescription(
                "When selected, deprecated modules will appear in the editing view available modules list.  These modules will be marked with a strikethrough their name, but otherwise act as normal.  Note: Modules marked as deprecated will be removed from future versions of MIA.");

        parameters.get(UPDATE_PARAMETERS).setDescription("When clicked, the preferences within MIA will be updated.");
    }
    
    class Update implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setShowDeprecated(parameters.getValue(SHOW_DEPRECATED));               
        }        
    }
}
