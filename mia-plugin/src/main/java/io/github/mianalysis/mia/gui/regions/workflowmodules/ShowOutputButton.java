package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.SwingPreferences;

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

    private static SVGLoader loader = new SVGLoader();
    private static SVGDocument svgDoc = loader.load(ShowOutputButton.class.getResource("/icons/eyeopen.svg"));           

    public ShowOutputButton(Module module) {
        this.module = module;

        state = module.canShowOutput();

        putClientProperty(FlatClientProperties.STYLE, "arc: 0");
        setBorderPainted(false);
        addActionListener(this);
        setFocusPainted(false);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setSelected(false);
        setMargin(new Insets(0, 0, 0, 0));
        setName("Show output");
        setToolTipText("Show output from module");
        updateState();

    }

    public void updateState() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        // if ((state && module.isEnabled()) && module.isReachable() && module.isRunnable()) {
        //     if (isDark)
        //         setIcon(blackOpenIconDM);
        //     else
        //         setIcon(blackOpenIcon);
        // } else if ((state && module.isEnabled()) & !module.isReachable()) {
        //     if (isDark)
        //         setIcon(orangeOpenIconDM);
        //     else
        //         setIcon(orangeOpenIcon);
        // } else if ((state && module.isEnabled()) & !module.isRunnable()) {
        //     if (isDark)
        //         setIcon(redOpenIconDM);
        //     else
        //         setIcon(redOpenIcon);
        // } else if (state & !module.isEnabled()) {
        //     setIcon(greyOpenIcon);
        // } else if ((!state && module.isEnabled()) && module.isReachable() && module.isRunnable()) {
        //     if (isDark)
        //         setIcon(blackClosedIconDM);
        //     else
        //         setIcon(blackClosedIcon);
        // } else if ((!state && module.isEnabled()) & !module.isReachable()) {
        //     if (isDark)
        //         setIcon(orangeClosedIconDM);
        //     else
        //         setIcon(orangeClosedIcon);
        // } else if ((!state && module.isEnabled()) & !module.isRunnable()) {
        //     if (isDark)
        //         setIcon(redClosedIconDM);
        //     else
        //         setIcon(redClosedIcon);
        // } else if (!state & !module.isEnabled()) {
        //     setIcon(greyClosedIcon);
        // }
        // setIcon(generateIcon());
        setIcon(new SVGIcon(svgDoc, 16, 16));

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

class SVGIcon implements Icon {
    private final SVGDocument document;
    private final int width;
    private final int height;

    public SVGIcon(SVGDocument document, int width, int height) {
        this.document = document;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        document.render(c, g2d, new ViewBox(x, y, width, height));
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
