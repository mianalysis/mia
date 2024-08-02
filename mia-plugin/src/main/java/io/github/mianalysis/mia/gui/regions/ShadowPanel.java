package io.github.mianalysis.mia.gui.regions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatDropShadowBorderMod;

public class ShadowPanel extends JPanel {
    public ShadowPanel(JComponent component) {
        component.setBackground(new Color(255,255,255,220));
        // component.setBackground(new Color(255,255,255,0));
        component.setOpaque(false);
        component.putClientProperty( FlatClientProperties.STYLE, "arc: 16" );

        setBorder(new FlatDropShadowBorderMod(Color.GRAY,8,1,8));
        setOpaque(false);

        setLayout(new BorderLayout());
        add(component,BorderLayout.CENTER);

    }
}
