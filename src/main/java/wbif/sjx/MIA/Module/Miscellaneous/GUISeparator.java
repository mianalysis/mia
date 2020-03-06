package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module {
    public static final String VISIBILITY_SEPARATOR = "Separator visibility";
    public static final String SHOW_BASIC = "Show basic";
    public static final String EXPANDED_BASIC = "Expanded basic GUI";
    public static final String EXPANDED_EDITING = "Expanded editing GUI";

    public GUISeparator(ModuleCollection modules) {
        super("GUI separator",modules);
    }


    public ModuleCollection getBasicModules() {
        ModuleCollection basicModules = new ModuleCollection();

        // If this separator isn't visible on the basic GUI it contains no modules
        if (!((boolean) parameters.getValue(SHOW_BASIC))) return basicModules;

        boolean record = false;
        for (Module module:modules.values()) {
            // Start recording until another visible GUI Separator is found
            if (module == this) {
                record = true;
                continue;
            }

            // If not yet recording we can skip to the next round
            if (!record) continue;

            // If this module is a visible GUISeparator, stop recording and return the available modules
            if (module instanceof GUISeparator) {
                if ((boolean) module.getParameterValue(GUISeparator.SHOW_BASIC)) {
                    return basicModules;
                }
            }

            // If currently recording and the module is visible, add it
            if (module.isRunnable() || module.invalidParameterIsVisible()) basicModules.add(module);

        }

        return basicModules;

    }

    public ModuleCollection getEditingModules() {
        ModuleCollection editingModules = new ModuleCollection();

        boolean record = false;
        for (Module module:modules.values()) {
            // Start recording until another GUI Separator is found
            if (module == this) {
                record = true;
                continue;
            }

            // If this module is a visible GUISeparator, stop recording and return the available modules
            if (module instanceof GUISeparator) {
                record = false;
                return editingModules;
            }

            // If currently recording, add the module
            if (record) editingModules.add(module);

        }

        return editingModules;

    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(VISIBILITY_SEPARATOR,this));
        parameters.add(new BooleanP(SHOW_BASIC,this,true));
        parameters.add(new BooleanP(EXPANDED_BASIC,this,true));
        parameters.add(new BooleanP(EXPANDED_EDITING,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    /**
     * This module is always runnable
     * @return
     */
    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
