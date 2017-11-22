package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by sc13967 on 21/11/2017.
 */
public abstract class GUI {
    protected GUIAnalysis analysis = new GUIAnalysis();
    private int lastModuleEval = -1;
    private HCModule activeModule = null;
    private Workspace testWorkspace = new Workspace(1, null);

    ModuleCollection getModules() {
        return analysis.getModules();
    }

    abstract void updateModules();

    int getLastModuleEval(){
           return lastModuleEval;
    }

    void setLastModuleEval(int lastModuleEval) {
        this.lastModuleEval = lastModuleEval;
    }

    HCModule getActiveModule() {
        return activeModule;
    }

    void setActiveModule(HCModule activeModule) {
        this.activeModule = activeModule;
    }

    void evaluateModule(HCModule module) throws GenericMIAException {
        module.execute(testWorkspace, true);
        lastModuleEval = getModules().indexOf(module);

        updateModules();
    }

    void setTestWorkspace(Workspace testWorkspace) {
        this.testWorkspace = testWorkspace;
    }
}
