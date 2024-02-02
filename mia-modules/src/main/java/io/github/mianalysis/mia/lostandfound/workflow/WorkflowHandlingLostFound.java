package io.github.mianalysis.mia.lostandfound.workflow;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.workflow.WorkflowHandling;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class WorkflowHandlingLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new WorkflowHandling(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"ConditionalAnalysisTermination"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Reference image measurement mode", WorkflowHandling.NUMERIC_FILTER_MODE);
        parameterNames.put("Reference value", WorkflowHandling.REFERENCE_NUMERIC_VALUE);
        parameterNames.put("Reference metadata value", WorkflowHandling.METADATA_VALUE);
        parameterNames.put("Reference image measurement", WorkflowHandling.IMAGE_MEASUREMENT);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
