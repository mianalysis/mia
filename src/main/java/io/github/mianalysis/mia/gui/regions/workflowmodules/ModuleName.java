package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.RenameListMenu;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.Colours;

public class ModuleName extends JLabel {
    private Module module;
    private JTable table;
    private boolean isSelected;

    private static final ImageIcon skipIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/skiparrow_orange_12px.png"), "");
    private static final ImageIcon warningIcon = new ImageIcon(
            ModuleName.class.getResource("/icons/warning_red_12px.png"), "");

    public ModuleName(Module module, JTable table, boolean isSelected) {
        this.module = module;
        this.table = table;
        this.isSelected = isSelected;

        setBorder(new EmptyBorder(2, 5, 0, 0));
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        setOpaque(true);
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

        if (module instanceof GUISeparator) {
            setForeground(Colours.DARK_BLUE);
            setToolTipText("Module separator");
        } else if (module.isEnabled() && module.isReachable() && module.isRunnable()) {
            setForeground(Color.BLACK);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: OK</html>");
        } else if (module.isEnabled() & !module.isReachable()) {
            setForeground(Colours.ORANGE);
            setIcon(skipIcon);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Skipped</html>");
        } else if (module.isEnabled() & !module.isRunnable()) {
            setForeground(Colours.RED);
            setIcon(warningIcon);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Error</html>");
        } else {
            setForeground(Color.GRAY);
            setToolTipText("<html>Module: " + module.getName() + "<br>Nickname: " + module.getNickname()
                    + "<br>Status: Disabled</html>");
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
