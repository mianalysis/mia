package io.github.mianalysis.mia.object.parameters.text;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public class SeriesSingleSelectorP extends IntegerP {
    public SeriesSingleSelectorP(String name, Module module, int value) {
        super(name, module, value);
    }

    public SeriesSingleSelectorP(String name, Module module, String value) {
        super(name, module, value);
    }

    public SeriesSingleSelectorP(String name, Module module, int value, String description) {
        super(name, module, value, description);
    }

    public SeriesSingleSelectorP(String name, Module module, String value, String description) {
        super(name, module, value, description);
    }

    @Override
    public ParameterControl getControl() {
        return ParameterControlFactory.getActiveFactory().getSeriesSelector(this);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        SeriesSingleSelectorP newParameter = new SeriesSingleSelectorP(name, newModule, value, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
