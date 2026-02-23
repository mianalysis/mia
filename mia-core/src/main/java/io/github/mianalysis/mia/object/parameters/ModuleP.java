package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public class ModuleP extends Parameter {
    protected String selectedModuleID = "";
    protected boolean showNonRunnable = true;

    public ModuleP(String name, ModuleI module, boolean showNonRunnable) {
        super(name, module);
        this.showNonRunnable = showNonRunnable;
    }

    public ModuleP(String name, ModuleI module, boolean showNonRunnable, String selectedModuleID) {
        super(name, module);
        this.selectedModuleID = selectedModuleID;
        this.showNonRunnable = showNonRunnable;
    }

    public ModuleP(String name, ModuleI module, boolean showNonRunnable, String selectedModuleID, String description) {
        super(name, module, description);
        this.selectedModuleID = selectedModuleID;
        this.showNonRunnable = showNonRunnable;
    }

    public String getSelectedModuleID() {
        return selectedModuleID;
    }

    public void setSelectedModuleID(String selectedModuleID) {
        this.selectedModuleID = selectedModuleID;
    }

    public ModuleI[] getModules() {
        ModulesI modules = module.getModules();
        int nAvailable = 0;
        for (ModuleI module : modules) {
            if (module.isEnabled() && (module.isRunnable() || showNonRunnable))
                nAvailable++;
        }

        ModuleI[] moduleArray = new Module[nAvailable];
        int count = 0;
        for (ModuleI module : modules) {
            if (module.isEnabled() && (module.isRunnable() || showNonRunnable))
                moduleArray[count++] = module;
        }

        return moduleArray;

    }

    @Override
    public String getRawStringValue() {
        return selectedModuleID;

    }

    @Override
    public String getAlternativeString() {
        ModuleI selectedModule = module.getModules().getModuleByID(getSelectedModuleID());
        if (selectedModule == null)
            return "";

        return selectedModule.getNickname();

    }

    @Override
    public void setValueFromString(String string) {
        this.selectedModuleID = string;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getModuleChoiceControl(this);
    }

    public boolean getShowNonRunnable() {
        return showNonRunnable;
    }

    public void setShowNonRunnable(boolean showNonRunnable) {
        this.showNonRunnable = showNonRunnable;
    }

    @Override
    public <T> T getValue(WorkspaceI workspace) {
        return (T) getSelectedModuleID();
    }

    @Override
    public <T> void setValue(T value) {
        selectedModuleID = ((Module) value).getModuleID();
    }

    @Override
    public boolean verify() {
        // Checking the selected module has been assigned and is present
        if (selectedModuleID == "")
            return false;

        // Checking if the selected module is in the module list. It doesn't need to be
        // enabled or runnable
        for (ModuleI testModule : module.getModules()) {
            if (testModule.getModuleID().equals(selectedModuleID)) {
                return true;
            }
        }

        return false;

    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        ModuleP newParameter = new ModuleP(getName(), newModule, showNonRunnable, selectedModuleID, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
