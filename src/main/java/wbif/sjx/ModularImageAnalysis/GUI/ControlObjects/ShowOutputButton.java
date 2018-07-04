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
    private static final ImageIcon redClosedIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeclosed_black_12px.png"), "");
    private static final ImageIcon greenOpenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/eyeopen_black_12px.png"), "");


    public ShowOutputButton(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        state = module.canShowOutput();

        addActionListener(this);
        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("Show output");
        setToolTipText("Show output from module");
        setIcon();

        setEnabled(module.isEnabled());

    }

    public void setIcon() {
//        if (state && module.isEnabled()) setIcon(greenOpenIcon) ;
//        else if (!state && module.isEnabled()) setIcon(redClosedIcon);
//        else if (state &! module.isEnabled()) setIcon(greyOpenIcon);
//        else if (!state &! module.isEnabled()) setIcon(greyClosedIcon);
        if (state) setIcon(greenOpenIcon);
        else setIcon(redClosedIcon);
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
