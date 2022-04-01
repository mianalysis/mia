package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;

public class ModuleName extends JLabel {
    private Module module;
    private JTable table;
    private boolean isSelected;
    private Color defaultColour;

    private static final ImageIcon skipIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/skiparrow_orange_12px.png"), "");
            private static final ImageIcon alertIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/alert_orange_12px.png"), "");
    private static final ImageIcon warningIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/warning_red_12px.png"), "");

    public ModuleName(Module module, JTable table, boolean isSelected) {
        this.module = module;
        this.table = table;
        this.isSelected = isSelected;

        setBorder(new EmptyBorder(2, 5, 0, 0));
        setOpaque(true);
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

        if (isSelected)
            setBackground(Colours.LIGHT_BLUE);
        else
            setBackground(table.getBackground());

    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if (isSelected)
            setBackground(Colours.LIGHT_BLUE);
        else
            setBackground(table.getBackground());
    }

    public void updateState() {
        setIcon(null);

        if (isSelected)
            setBackground(Colours.LIGHT_BLUE);
        else
            setBackground(table.getBackground());

        String deprecationMessage = "";
        if (module.isDeprecated())
            deprecationMessage = " (deprecated)";

        if (module instanceof GUISeparator) {
            setForeground(Colours.DARK_BLUE);
            setToolTipText("Module separator");
        } else if (module.isEnabled() & !module.isReachable()) {
            setForeground(Colours.ORANGE);
            setIcon(skipIcon);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Skipped"+deprecationMessage+"</html>");
        } else if (module.isEnabled() & !module.isRunnable()) {
            setForeground(Colours.RED);
            setIcon(warningIcon);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Error"+deprecationMessage+"</html>");
        } else if (module.isEnabled()
                & MIA.preferences.getDataStorageMode().equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE)
                && module.getIL2Support().equals(IL2Support.PARTIAL)) {
            setForeground(defaultColour);
            setIcon(alertIcon);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Partial image streaming support"+deprecationMessage+"</html>");
        } else if (module.isEnabled() && module.isReachable() && module.isRunnable()) {
            setForeground(defaultColour);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: OK"+deprecationMessage+"</html>");
        } else {
            setForeground(Color.BLACK);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Disabled"+deprecationMessage+"</html>");
        }

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
