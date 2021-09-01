package io.github.mianalysis.MIA.GUI.ParameterControls;

import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.TextType;

import java.awt.event.FocusEvent;

public class SeriesSelector extends TextParameter {
    public SeriesSelector(TextType parameter) {
        super(parameter);
    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();
        super.focusLost(e);

        GUI.updateTestFile(true);
        GUI.updateModuleStates(true);

    }
}
