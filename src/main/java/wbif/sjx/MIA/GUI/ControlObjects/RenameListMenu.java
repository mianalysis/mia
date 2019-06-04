package wbif.sjx.MIA.GUI.ControlObjects;

import jdk.nashorn.internal.scripts.JO;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class RenameListMenu extends JPopupMenu implements ActionListener {
    private final Ref reference;
    private final JMenuItem renameMenuItem;
    private final JMenuItem resetMenuItem;

    public RenameListMenu(Ref reference) {
        this.reference = reference;

        renameMenuItem = new JMenuItem();
        renameMenuItem.setText("Rename");
        renameMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        renameMenuItem.addActionListener(this);

        resetMenuItem = new JMenuItem("");
        resetMenuItem.setText("Reset name");
        resetMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        resetMenuItem.addActionListener(this);

        add(renameMenuItem);
        add(resetMenuItem);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Rename":
                String nickname = (String) JOptionPane.showInputDialog(new JFrame(),"Enter new name","",JOptionPane.PLAIN_MESSAGE,null,null,reference.getNickname());
                if (nickname != null) reference.setNickname(nickname);
                break;
            case "Reset name":
                reference.setNickname(reference.getName());
                break;
        }

        GUI.updateParameters();

        setVisible(false);

    }
}
