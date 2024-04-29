package io.github.mianalysis.mia.gui.regions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextSwitchableParameter;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;

public class ReferenceEditingMenu extends JPopupMenu implements ActionListener {
    private static final long serialVersionUID = 3459551119073952948L;
    private final Ref reference;

    private static final String RENAME = "Rename";
    private static final String RESET_NAME = "Reset name";
    private static final String CHANGE_TO_TEXT = "Change to text entry";
    private static final String CHANGE_TO_DEFAULT = "Change to default entry";

    public ReferenceEditingMenu(Ref reference) {
        this.reference = reference;

        JMenuItem renameMenuItem = new JMenuItem();
        renameMenuItem.setText(RENAME);
        renameMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        renameMenuItem.addActionListener(this);
        add(renameMenuItem);

        JMenuItem resetMenuItem = new JMenuItem("");
        resetMenuItem.setText(RESET_NAME);
        resetMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        resetMenuItem.addActionListener(this);
        add(resetMenuItem);

        if (reference instanceof TextSwitchableParameter) {
            addSeparator();
            
            JMenuItem textSwitchMenuItem = new JMenuItem();
            if (((TextSwitchableParameter) reference).isShowText())
                textSwitchMenuItem.setText(CHANGE_TO_DEFAULT);
            else
                textSwitchMenuItem.setText(CHANGE_TO_TEXT);
            textSwitchMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            textSwitchMenuItem.addActionListener(this);
            add(textSwitchMenuItem);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();
        setVisible(false);

        switch (e.getActionCommand()) {
            case RENAME:
                String nickname = (String) JOptionPane.showInputDialog(new JFrame(),"Enter new name","",JOptionPane.PLAIN_MESSAGE,null,null,reference.getNickname());
                if (nickname != null) reference.setNickname(nickname);
                break;
            case RESET_NAME:
                reference.setNickname(reference.getName());
                break;
            case CHANGE_TO_TEXT:
                ((TextSwitchableParameter) reference).setShowText(true);
                break;
            case CHANGE_TO_DEFAULT:
                ((TextSwitchableParameter) reference).setShowText(false);
                break;
        }

        GUI.updateModules();
        GUI.updateParameters();

    }
}