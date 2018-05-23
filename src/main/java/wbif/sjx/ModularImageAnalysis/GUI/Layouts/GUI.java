package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;

/**
 * Created by sc13967 on 21/11/2017.
 */
public abstract class GUI {
    protected Analysis analysis = new Analysis();
    Module activeModule = null;
    protected int lastModuleEval = -1;
    private Workspace testWorkspace = new Workspace(1, null,1);

    public ModuleCollection getModules() {
        return analysis.getModules();
    }

    public abstract void populateModuleList();

    public abstract void populateModuleParameters();

    public abstract void updateModules();

    public abstract void updateTestFile();

    public int getLastModuleEval(){
           return lastModuleEval;
    }

    public void setLastModuleEval(int lastModuleEval) {
        this.lastModuleEval = Math.max(lastModuleEval,-1);
    }

    public Module getActiveModule() {
        return activeModule;
    }

    public void setActiveModule(Module activeModule) {
        this.activeModule = activeModule;
    }

    public void evaluateModule(Module module) throws GenericMIAException {
        Module.setVerbose(true);
        module.execute(testWorkspace);
        lastModuleEval = getModules().indexOf(module);

        updateModules();
    }

    public void setTestWorkspace(Workspace testWorkspace) {
        this.testWorkspace = testWorkspace;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public Workspace getTestWorkspace() {
        return testWorkspace;
    }

}
