package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import java.awt.event.FocusEvent;

public class SeriesSelector extends TextParameter {
    public SeriesSelector(TextType parameter) {
        super(parameter);
    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();
        super.focusLost(e);

        GUI.updateTestFile();
        GUI.updateModuleStates(true);

    }
}
