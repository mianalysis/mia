package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ShowOutputButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -5640712162795245225L;
    private Module module;
    private boolean state = true;
    private static final ImageIcon blackClosedIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_black_12px.png"), "");
    private static final ImageIcon blackClosedIconDM = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_blackDM_12px.png"), "");
    private static final ImageIcon blackOpenIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_black_12px.png"), "");
    private static final ImageIcon blackOpenIconDM = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_blackDM_12px.png"), "");
    private static final ImageIcon redClosedIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_red_12px.png"), "");
    private static final ImageIcon redClosedIconDM = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_redDM_12px.png"), "");
    private static final ImageIcon redOpenIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_red_12px.png"), "");
    private static final ImageIcon redOpenIconDM = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_redDM_12px.png"), "");
    private static final ImageIcon orangeClosedIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_orange_12px.png"), "");
    private static final ImageIcon orangeClosedIconDM = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_orangeDM_12px.png"), "");
    private static final ImageIcon orangeOpenIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_orange_12px.png"), "");
    private static final ImageIcon orangeOpenIconDM = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_orangeDM_12px.png"), "");
    private static final ImageIcon greyClosedIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeclosed_grey_12px.png"), "");
    private static final ImageIcon greyOpenIcon = new ImageIcon(
            ShowOutputButton.class.getResource("/icons/eyeopen_grey_12px.png"), "");

    public ShowOutputButton(Module module) {
        this.module = module;

        state = module.canShowOutput();

        addActionListener(this);
        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0, 0, 0, 0));
        setName("Show output");
        setToolTipText("Show output from module");
        updateState();

    }

    public void updateState() {
        boolean darkMode = MIA.preferences.darkThemeEnabled();

        if ((state && module.isEnabled()) && module.isReachable() && module.isRunnable()) {
            if (darkMode)
                setIcon(blackOpenIconDM);
            else
                setIcon(blackOpenIcon);
        } else if ((state && module.isEnabled()) & !module.isReachable()) {
            if (darkMode)
                setIcon(orangeOpenIconDM);
            else
                setIcon(orangeOpenIcon);
        } else if ((state && module.isEnabled()) & !module.isRunnable()) {
            if (darkMode)
                setIcon(redOpenIconDM);
            else
                setIcon(redOpenIcon);
        } else if (state & !module.isEnabled()) {
            setIcon(greyOpenIcon);
        } else if ((!state && module.isEnabled()) && module.isReachable() && module.isRunnable()) {
            if (darkMode)
                setIcon(blackClosedIconDM);
            else
                setIcon(blackClosedIcon);
        } else if ((!state && module.isEnabled()) & !module.isReachable()) {
            if (darkMode)
                setIcon(orangeClosedIconDM);
            else
                setIcon(orangeClosedIcon);
        } else if ((!state && module.isEnabled()) & !module.isRunnable()) {
            if (darkMode)
                setIcon(redClosedIconDM);
            else
                setIcon(redClosedIcon);
        } else if (!state & !module.isEnabled()) {
            setIcon(greyClosedIcon);
        }
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        // Invert state
        state = !state;

        updateState();
        module.setShowOutput(state);

    }
}
