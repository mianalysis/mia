package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatClientProperties;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class ModuleName extends JLabel {
    private Module module;
    private JTable table;
    private boolean isSelected;
    private Color defaultColour;

    private static final ImageIcon skipIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/skiparrow_orange_12px.png"), "");
    private static final ImageIcon skipIconDM = new ImageIcon(
            ModuleName.class.getResource("/icons/skiparrow_orangeDM_12px.png"), "");
    private static final ImageIcon warningIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/warning_red_12px.png"), "");
    private static final ImageIcon warningIconDM = new ImageIcon(
            ModuleName.class.getResource("/icons/warning_redDM_12px.png"), "");

    public ModuleName(Module module, JTable table, boolean isSelected) {
        this.module = module;
        this.table = table;
        this.isSelected = isSelected;

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        try {
            putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        } catch (Exception e) {
        }
        setBorder(new EmptyBorder(2, 5, 0, 0));
        setPreferredSize(new Dimension(200, 30));
        setBackground(new Color(0, 0, 0, 0));
        setOpaque(false);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        if (module.isDeprecated()) {
            Map attributes = font.getAttributes();
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            font = new Font(attributes);
        }

        defaultColour = getForeground();

        setFont(font);
        setText(module.getNickname());
        updateState();

        // if (isSelected)
        //     setBackground(Colours.getLightBlue(isDark));
        // else
        //     setBackground(table.getBackground());

    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (isSelected)
            setBackground(Colours.getLightBlue(isDark));
        else
            setBackground(table.getBackground());
    }

    public void updateState() {
        setIcon(null);

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        String deprecationMessage = "";
        if (module.isDeprecated())
            deprecationMessage = " (deprecated)";

        String status = "";
        if (module instanceof GUISeparator) {
            setForeground(Colours.getDarkBlue(isDark));
            setToolTipText("Module separator");
            setHorizontalAlignment(CENTER);
        } else if (module.isEnabled() && module.isReachable() && module.isRunnable()) {
            setForeground(defaultColour);
            status = "OK";
        } else if (module.isEnabled() & !module.isReachable()) {
            setForeground(Colours.getOrange(isDark));
            if (isDark)
                setIcon(skipIconDM);
            else
                setIcon(skipIcon);
            status = "Skipped";
        } else if (module.isEnabled() & !module.isRunnable()) {
            setForeground(Colours.getRed(isDark));
            if (isDark)
                setIcon(warningIconDM);
            else
                setIcon(warningIcon);
            status = "Error";
        } else {
            setForeground(Color.GRAY);
            status = "Disabled";
        }

        setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                + "<br>ID: " + module.getModuleID() + "<br>Status: " + status + deprecationMessage + "</html>");

        if (module.isDeprecated()) {
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            Map attributes = font.getAttributes();
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            font = new Font(attributes);
        }

        revalidate();
        repaint();

    }
}
