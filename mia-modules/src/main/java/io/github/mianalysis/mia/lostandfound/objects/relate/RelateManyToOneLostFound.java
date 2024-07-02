package io.github.mianalysis.mia.lostandfound.objects.relate;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.relate.RelateManyToOne;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class RelateManyToOneLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new RelateManyToOne(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String, String> parameterNames = new HashMap<>();
        parameterNames.put("Reference point", RelateManyToOne.REFERENCE_MODE);
        parameterNames.put("Minimum percentage overlap", RelateManyToOne.MINIMUM_OVERLAP);
        parameterNames.put("Limit linking by distance", RelateManyToOne.LINKING_DISTANCE_LIMIT);

        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, HashMap<String, String>> parameterValues = new HashMap<>();

        HashMap<String, String> parameterValue = new HashMap<>();
        parameterValue.put("false", RelateManyToOne.LinkingDistanceLimits.NO_LIMIT);
        parameterValue.put("true", RelateManyToOne.LinkingDistanceLimits.LIMIT_BOTH);

        parameterValues.put(RelateManyToOne.LINKING_DISTANCE_LIMIT, parameterValue);

        return parameterValues;

    }

}
