package io.github.mianalysis.MIA.GUI.Regions.WorkflowModules;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Module.Module;

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
    private static final ImageIcon blackClosedIcon = new ImageIcon(ShowOutputButton.class.getResource("/Icons/eyeclosed_black_12px.png"), "");
    private static final ImageIcon blackOpenIcon = new ImageIcon(ShowOutputButton.class.getResource("/Icons/eyeopen_black_12px.png"), "");
    private static final ImageIcon redClosedIcon = new ImageIcon(ShowOutputButton.class.getResource("/Icons/eyeclosed_red_12px.png"), "");
    private static final ImageIcon redOpenIcon = new ImageIcon(ShowOutputButton.class.getResource("/Icons/eyeopen_red_12px.png"), "");
    private static final ImageIcon greyClosedIcon = new ImageIcon(ShowOutputButton.class.getResource("/Icons/eyeclosed_grey_12px.png"), "");
    private static final ImageIcon greyOpenIcon = new ImageIcon(ShowOutputButton.class.getResource("/Icons/eyeopen_grey_12px.png"), "");


    public ShowOutputButton(Module module) {
        this.module = module;

        state = module.canShowOutput();

        addActionListener(this);
        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("Show output");
        setToolTipText("Show output from module");
        updateState();

    }

    public void updateState() {
        if ((state && module.isEnabled()) && module.isRunnable()) setIcon(blackOpenIcon);
        else if ((state && module.isEnabled()) &! module.isRunnable()) setIcon(redOpenIcon);
        else if (state &! module.isEnabled()) setIcon(greyOpenIcon);
        else if ((!state && module.isEnabled()) && module.isRunnable()) setIcon(blackClosedIcon);
        else if ((!state && module.isEnabled()) && module.isRunnable()) setIcon(blackClosedIcon);
        else if ((!state && module.isEnabled()) &! module.isRunnable()) setIcon(redClosedIcon);
        else if (!state &! module.isEnabled()) setIcon(greyClosedIcon);

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
