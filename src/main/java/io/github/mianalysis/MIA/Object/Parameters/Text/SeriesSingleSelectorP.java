package io.github.mianalysis.MIA.Object.Parameters.Text;

import io.github.mianalysis.MIA.GUI.ParameterControls.ParameterControl;
import io.github.mianalysis.MIA.GUI.ParameterControls.SeriesSelector;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

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
        return new SeriesSelector(this);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        SeriesSingleSelectorP newParameter = new SeriesSingleSelectorP(name,newModule,value,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
