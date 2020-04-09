package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.ParameterControls.ModuleChoiceParameter;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class ModuleP extends Parameter {
    protected String selectedModuleID = "";

    public ModuleP(String name, Module module) {
        super(name, module);
    }

    public ModuleP(String name, Module module, String selectedModuleID) {
        super(name, module);
        this.selectedModuleID = selectedModuleID;
    }

    public ModuleP(String name, Module module, String selectedModuleID, String description) {
        super(name, module, description);
        this.selectedModuleID = selectedModuleID;
    }

    public Module getSelectedModule() {
        return module.getModules().getModuleByID(selectedModuleID);
    }

    public void setSelectedModule(Module selectedModule) {
        this.selectedModuleID = selectedModule.getModuleID();
    }

    public Module[] getModules() {
        ModuleCollection modules = module.getModules();
        int nAvailable = 0;
        for (Module module : modules) {
            if (module.isEnabled() && module.isRunnable())
                nAvailable++;
        }

        Module[] moduleArray = new Module[nAvailable];
        int count = 0;
        for (Module module : modules) {
            if (module.isEnabled() && module.isRunnable())
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
        Module selectedModule = getSelectedModule();
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
        return new ModuleChoiceParameter(this);
    }

    @Override
    public <T> T getValue() {
        return (T) getSelectedModule();
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

        // Checking if the selected module is in the module list and if it's enabled and
        // runnable
        for (Module testModule : module.getModules()) {
            if (testModule.getModuleID().equals(selectedModuleID) && testModule.isEnabled()
                    && testModule.isRunnable()) {
                return true;
            }
        }

        return false;

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ModuleP newParameter = new ModuleP(name, newModule, selectedModuleID, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
