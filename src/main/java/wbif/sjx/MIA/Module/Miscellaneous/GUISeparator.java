package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module {
    public static final String VISIBILITY_SEPARATOR = "Separator visibility";
    public static final String SHOW_BASIC = "Show basic";
    public static final String EXPANDED_BASIC = "Expanded basic GUI";
    public static final String EXPANDED_EDITING = "Expanded editing GUI";

    public GUISeparator(ModuleCollection modules) {
        super("GUI separator", modules);
    }

    public ModuleCollection getBasicModules() {
        ModuleCollection basicModules = new ModuleCollection();

        // If this separator isn't visible on the basic GUI it contains no modules
        if (!((boolean) parameters.getValue(SHOW_BASIC)))
            return basicModules;

        boolean record = false;
        for (Module module : modules.values()) {
            // Start recording until another visible GUI Separator is found
            if (module == this) {
                record = true;
                continue;
            }

            // If not yet recording we can skip to the next round
            if (!record)
                continue;

            // If this module is a visible GUISeparator, stop recording and return the
            // available modules
            if (module instanceof GUISeparator) {
                if ((boolean) module.getParameterValue(GUISeparator.SHOW_BASIC)) {
                    return basicModules;
                }
            }

            // If currently recording and the module is visible, add it
            if (module.canBeDisabled() || module.hasVisibleParameters() || module.invalidParameterIsVisible())
                basicModules.add(module);

        }

        return basicModules;

    }

    public ModuleCollection getEditingModules() {
        ModuleCollection editingModules = new ModuleCollection();

        boolean record = false;
        for (Module module : modules.values()) {
            // Start recording until another GUI Separator is found
            if (module == this) {
                record = true;
                continue;
            }

            // If this module is a visible GUISeparator, stop recording and return the
            // available modules
            if (module instanceof GUISeparator) {
                record = false;
                return editingModules;
            }

            // If currently recording, add the module
            if (record)
                editingModules.add(module);

        }

        return editingModules;

    }


    @Override
    public Category getCategory() {
        return Categories.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "GUI separators break the module list into sections, where all modules are considered to be in the \"section\" corresponding to the separator above them in the module list.  All modules contained within these sections can be enabled/disabled <i>en masse</i> by enabling/disabling their associated separator.  In basic view, the separator can be shown as a blue line, while in editing view it appears as a normal module entry albeit with blue text.  In both views it's possible to hide/show the contents of a separator section using the blue arrows.<br><br>"
        
        +"Separators are intended to break analyses into logical groups, thus making workflow organisation easier and tidier.";
    }

    @Override
    public Status process(Workspace workspace) {
        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(VISIBILITY_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_BASIC, this, true));
        parameters.add(new BooleanP(EXPANDED_BASIC, this, true));
        parameters.add(new BooleanP(EXPANDED_EDITING, this, true));

        addParameterDescriptions();
        
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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(SHOW_BASIC).setDescription(
                "Display this GUI separator in the basic control panel view.  When this parameter is selected, this separator will still only be shown if at least one of the following is true:<br><ul>"

                        + "<li>The separator (and thus all associated modules) can be enabled/disabled from the basic view.  This is done by ticking \"Can be disabled\" at the top of this module in editing view.  Enabling/disabling a GUI separator will automatically apply the same state to all modules contained within (i.e. between this separator and the next separator).  If the separator can be enabled/disabled from basic view the power icon will be shown in green when enabled or in black with a strikethrough when disabled; however, if it can't be enabled/disabled, the power icon will be greyed out.</li>"

                        + "<li>At least one module associated with this separator (a module between this separator and the next) can be disabled from the basic control view.  Modules can be set to allow enabling/disabling from basic view by ticking \"Can be disabled\" at the top of the relevant module parameter control in editing view.</li>"

                        + "<li>At least one parameter from a module associated with this separator (a module between this separator and the next) can be set from the basic control view.  Parameters can be set as visible in basic view by clicking the eyeball icon to the right of that parameter in editing view.  When a module is visible an open eye is shown, while this is a closed eye when it's not visible (default option).</li></ul>");

        parameters.get(EXPANDED_BASIC).setDescription(
                "In basic view, all controls between this separator and the next can be displayed/hidden by clicking the blue arrows either side of the separator bar.  This parameter controls if this parameter is in the expanded (controls displayed) or collapsed (controls hidden) state.  When the expansion state is changed from the basic view, this parameter is automatically updated.");

        parameters.get(EXPANDED_EDITING).setDescription("In editing view, all modules in the module list between this separator and the next can be displayed/hidden by clicking the blue arrows either side of module button.  This parameter controls if this parameter is in the expanded (modules displayed) or collapsed (modules hidden) state.  When the expansion state is changed from the editing view, this parameter is automatically updated.");

    }
}
