package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

import java.awt.event.FocusEvent;

public class SeriesSelector extends TextParameter {
    public SeriesSelector(TextType parameter) {
        super(parameter);
    }

    @Override
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        GUI.updateTestFile();
    }
}
