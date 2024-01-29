package io.github.mianalysis.mia.lostandfound.objects.process;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.process.CreateSkeleton;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class CreateSkeletonLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new CreateSkeleton(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "MeasureSkeleton" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        return new HashMap<String, String>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }

}
