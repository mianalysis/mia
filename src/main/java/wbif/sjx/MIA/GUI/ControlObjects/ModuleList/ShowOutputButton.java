package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Icons.IconFactory;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ShowOutputButton extends JButton implements ActionListener {
    private Module module;
    private boolean state = true;
    private static final ImageIcon blackOpenIcon = new IconFactory(14,14).getEyeOpen(Colours.BLACK_HEX);
    private static final ImageIcon redOpenIcon = new IconFactory(14,14).getEyeOpen(Colours.RED_HEX);
    private static final ImageIcon greyOpenIcon = new IconFactory(14,14).getEyeOpen(Colours.GREY_HEX);
    private static final ImageIcon blackClosedIcon = new IconFactory(14,14).getEyeClosed(Colours.BLACK_HEX);
    private static final ImageIcon redClosedIcon = new IconFactory(14,14).getEyeClosed(Colours.RED_HEX);
    private static final ImageIcon greyClosedIcon = new IconFactory(14,14).getEyeClosed(Colours.GREY_HEX);


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
