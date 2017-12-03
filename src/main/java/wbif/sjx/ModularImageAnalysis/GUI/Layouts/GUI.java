package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by sc13967 on 21/11/2017.
 */
public abstract class GUI {
    protected GUIAnalysis analysis = new GUIAnalysis();
    HCModule activeModule = null;
    private int lastModuleEval = -1;
    private Workspace testWorkspace = new Workspace(1, null);

    public ModuleCollection getModules() {
        return analysis.getModules();
    }

    public abstract void updateModules();

    public int getLastModuleEval(){
           return lastModuleEval;
    }

    public void setLastModuleEval(int lastModuleEval) {
        this.lastModuleEval = lastModuleEval;
    }

    public HCModule getActiveModule() {
        return activeModule;
    }

    public void setActiveModule(HCModule activeModule) {
        this.activeModule = activeModule;
    }

    public void evaluateModule(HCModule module) throws GenericMIAException {
        module.execute(testWorkspace, true);
        lastModuleEval = getModules().indexOf(module);

        updateModules();
    }

    public void setTestWorkspace(Workspace testWorkspace) {
        this.testWorkspace = testWorkspace;
    }

}
