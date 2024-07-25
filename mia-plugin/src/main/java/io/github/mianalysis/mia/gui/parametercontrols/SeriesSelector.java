package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.event.FocusEvent;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

public class SeriesSelector extends TextParameter {
    public SeriesSelector(TextType parameter) {
        super(parameter);
    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();
        super.focusLost(e);

        new Thread(() -> {
            // GUI.updateTestFile(true);
            GUI.updateModules(true, parameter.getModule());
            GUI.updateParameters(true, parameter.getModule());
        }).start();
    }
}
