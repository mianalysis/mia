package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ShowOutputButton extends JButton implements ActionListener {
    private GUI gui;
    private Module module;
    private boolean state = true;
    private static final ImageIcon redClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_red_12px.png"), "");
    private static final ImageIcon greenOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_green_12px.png"), "");
    private static final ImageIcon greyClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_grey_12px.png"), "");
    private static final ImageIcon greyOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_grey_12px.png"), "");


    public ShowOutputButton(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        state = module.canShowOutput();

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("Show output");
        setIcon();

        addActionListener(this);

    }

    public void setIcon() {
        if (state && module.isEnabled()) setIcon(greenOpenIcon) ;
        else if (!state && module.isEnabled()) setIcon(redClosedIcon);
        else if (state &! module.isEnabled()) setIcon(greyOpenIcon);
        else if (!state &! module.isEnabled()) setIcon(greyClosedIcon);
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Invert state
        state = !state;

        setIcon();
        module.setShowOutput(state);

    }
}
