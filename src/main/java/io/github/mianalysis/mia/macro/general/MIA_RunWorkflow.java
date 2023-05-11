package io.github.mianalysis.mia.macro.general;

import java.awt.GraphicsEnvironment;
import java.io.File;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_RunWorkflow extends MacroOperation {

    public MIA_RunWorkflow(MacroExtension theHandler) {
        super(theHandler);
    }
    
    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        File workflowPath = new File((String) objects[0]);
        String inputPath = (String) objects[1];

        try {
            Analysis analysis = AnalysisReader.loadAnalysis(workflowPath);
            if (inputPath != null)
                analysis.getModules().getInputControl().updateParameterValue(InputControl.INPUT_PATH, inputPath);
                    
            // Running analysis
            AnalysisRunner runner = new AnalysisRunner();
            runner.run(analysis,false);
            MacroHandler.setWorkspace(runner.getWorkspaces().iterator().next());

            if (GraphicsEnvironment.isHeadless())
                java.lang.System.exit(0);
            
        } catch (Exception e) {
            MIA.log.writeError(e);
        }
        
        return null;
        
    }
    
    @Override
    public String getArgumentsDescription() {
        return "String workflowPath, String inputPath";
    }
    
    @Override
    public String getDescription() {
        return "Removes all images and objects from the workspace.  This should be generateModuleList at the beginning of a macro.";
    }
}
