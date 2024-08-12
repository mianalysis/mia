package io.github.mianalysis.mia.gui.regions.progressandstatus;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.system.Colours;

public class StatusPanel extends JPanel {

    private static final long serialVersionUID = -5685268881319325735L;
    private double value = 0;

    public StatusPanel() {
        int statusHeight = GUI.getStatusHeight();

        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(1, statusHeight + 15));
        setMaximumSize(new Dimension(1, statusHeight + 15));
        setPreferredSize(new Dimension(1, statusHeight + 15));

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (value == 100)
            g.setColor(Colours.getGreen(false));
        else
            g.setColor(Colours.getBlue(false));
        g.fillRect(0, 0, (int) Math.round(getWidth() * value / 100), getHeight());

    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;

        repaint();

    }
}
