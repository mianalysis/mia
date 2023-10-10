package io.github.mianalysis.mia.object.parameters.text;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;

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
        return ParameterControlFactory.getActiveFactory().getSeriesSelector(this);
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
        return CommaSeparatedStringInterpreter.interpretIntegers(value,true,0);

    }
}
