package wbif.sjx.MIA.Object.Parameters.Text;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.SeriesSelector;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

import com.drew.lang.annotations.NotNull;

public class SeriesListSelectorP extends StringP {
    public SeriesListSelectorP(String name, Module module) {
        super(name, module);
    }

    public SeriesListSelectorP(String name, Module module, @NotNull String value) {
        super(name, module, value);
    }

    public SeriesListSelectorP(String name, Module module, @NotNull String value, String description) {
        super(name, module, value, description);
    }

    @Override
    public ParameterControl getControl() {
        return new SeriesSelector(this);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        SeriesListSelectorP newParameter = new SeriesListSelectorP(name,newModule,value,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    public int[] getSeriesList() {
        return CommaSeparatedStringInterpreter.interpretIntegers(value,true);

    }
}
