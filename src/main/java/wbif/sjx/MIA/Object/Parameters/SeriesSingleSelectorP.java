package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.SeriesSelector;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class SeriesSingleSelectorP extends IntegerP {
    public SeriesSingleSelectorP(String name, Module module, int value) {
        super(name, module, value);
    }

    public SeriesSingleSelectorP(String name, Module module, int value, String description) {
        super(name, module, value, description);
    }

    @Override
    public ParameterControl getControl() {
        return new SeriesSelector(this);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new SeriesSingleSelectorP(name,module,value,getDescription());
    }
}
