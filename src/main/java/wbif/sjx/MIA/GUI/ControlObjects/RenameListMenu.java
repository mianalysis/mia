package wbif.sjx.MIA.GUI.ControlObjects;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.References.Abstract.Ref;

public class RenameListMenu extends JPopupMenu implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 3459551119073952948L;
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
        GUI.addUndo();
        setVisible(false);

        switch (e.getActionCommand()) {
            case "Rename":
                String nickname = (String) JOptionPane.showInputDialog(new JFrame(),"Enter new name","",JOptionPane.PLAIN_MESSAGE,null,null,reference.getNickname());
                if (nickname != null) reference.setNickname(nickname);
                break;
            case "Reset name":
                reference.setNickname(reference.getName());
                break;
        }

        GUI.updateModuleStates(false);
        GUI.updateModules();
        GUI.updateParameters();

    }
}
