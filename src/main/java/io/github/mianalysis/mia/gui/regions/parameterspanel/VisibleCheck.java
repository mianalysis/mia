package io.github.mianalysis.mia.gui.regions.parameterspanel;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class VisibleCheck extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 3462766918524878171L;
    private Parameter parameter;

    private static final ImageIcon closedIcon = new ImageIcon(VisibleCheck.class.getResource("/icons/eyeclosed_black_12px.png"), "");
    private static final ImageIcon openIcon = new ImageIcon(VisibleCheck.class.getResource("/icons/eyeopen_black_12px.png"), "");

    public VisibleCheck(Parameter parameter) {
        this.parameter = parameter;

        addActionListener(this);
        setFocusPainted(false);
        setSelected(parameter.isVisible());
        setMargin(new Insets(0,0,0,0));
        setName("Show parameter");
        setToolTipText("Show parameter in processing view");
        updateIcon();

    }

    public void updateIcon() {
        if (parameter.isVisible()) setIcon(openIcon);
        else setIcon(closedIcon);
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        parameter.setVisible(!parameter.isVisible());
        updateIcon();

    }
}
