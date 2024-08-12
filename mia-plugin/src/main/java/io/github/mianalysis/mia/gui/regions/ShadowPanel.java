package io.github.mianalysis.mia.gui.regions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatDropShadowBorderMod;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class ShadowPanel extends JPanel {
    public ShadowPanel(JComponent component) {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (isDark)
            component.setBackground(new Color(255, 255, 255, 32));
        else
            component.setBackground(new Color(255, 255, 255, 220));

        component.setOpaque(false);
        component.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        if (isDark)
            setBorder(new FlatDropShadowBorderMod(new Color(48, 48, 48), 8, 1, 16));
        else
            setBorder(new FlatDropShadowBorderMod(Color.GRAY, 8, 1, 16));
        setOpaque(false);

        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);

    }
}
