package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.SeriesSelector;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeriesListSelectorP extends StringP {
    public SeriesListSelectorP(String name, Module module) {
        super(name, module);
    }

    public SeriesListSelectorP(String name, Module module, @Nonnull String value) {
        super(name, module, value);
    }

    public SeriesListSelectorP(String name, Module module, @Nonnull String value, String description) {
        super(name, module, value, description);
    }

    @Override
    public ParameterControl getControl() {
        return new SeriesSelector(this);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new SeriesListSelectorP(name,module,value,getDescription());
    }

    public int[] getSeriesList() {
        // Converting series list to a list of numbers
        String seriesList = value.replace(" ","");
        List<String> list = new ArrayList<String>(Arrays.asList(seriesList.split(",")));

        return list.stream().mapToInt(Integer::valueOf).toArray();

    }
}
