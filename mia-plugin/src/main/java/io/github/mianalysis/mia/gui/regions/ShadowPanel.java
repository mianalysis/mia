package io.github.mianalysis.mia.gui.regions;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatDropShadowBorder;

public class ShadowPanel extends JPanel {
    public ShadowPanel(JComponent component) {
        component.setBackground(new Color(255,255,255,220));
        component.setOpaque(true);
        component.putClientProperty( FlatClientProperties.STYLE, "arc: 16" );

        setBorder(new FlatDropShadowBorder(Color.GRAY,5,1));
        setOpaque(false);

        setLayout(new BorderLayout());
        add(component,BorderLayout.CENTER);

    }
}
