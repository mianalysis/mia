package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.svg.SVGButton;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class VisibleCheck extends SVGButton implements ActionListener {
    private static final int size = 18;
    private Parameter parameter;

    public VisibleCheck(Parameter parameter) {
        super(new String[] { "/icons/eyeopen.svg", "/icons/eyeclosed.svg" }, size, parameter.isVisible() ? 0 : 1);
        
        this.parameter = parameter;
        
        addActionListener(this);
        setName("Show parameter");
        setToolTipText("Show parameter in processing view");

        updateState();

    }

    @Override
    public void updateState() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (parameter.isVisible())
            selectIconByIndex(0);
        else
            selectIconByIndex(1);

        dynamicForegroundColor.setColor(Colours.getBlack(isDark));

    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        parameter.setVisible(!parameter.isVisible());
        updateState();

    }
}
