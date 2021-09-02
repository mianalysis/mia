package io.github.mianalysis.mia.gui.parametercontrols;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.Object.Parameters.Abstract.TextType;

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
