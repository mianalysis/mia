package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.svg.CustomColorsProcessor;
import io.github.mianalysis.mia.gui.svg.CustomParserProvider;
import io.github.mianalysis.mia.gui.svg.DynamicAWTSvgPaint;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ShowOutputButton extends JButton implements ActionListener, MouseListener {
    /**
     *
     */
    private static final int size = 18;
    private Module module;
    private boolean showOutput = true;

    private SVGIcon eyeOpenIcon;
    private SVGIcon eyeClosedIcon;
    private DynamicAWTSvgPaint dynamicEyeColor;
    private DynamicAWTSvgPaint dynamicGlowColor;

    public ShowOutputButton(Module module) {
        this.module = module;

        CustomColorsProcessor processor = new CustomColorsProcessor(Arrays.asList("glow", "foreground"));
        CustomParserProvider provider = new CustomParserProvider(processor);
        SVGDocument eyeOpenSvgDoc = new SVGLoader()
                .load(ShowOutputButton.class.getResource("/icons/eyeopen.svg"), provider);
        SVGDocument eyeClosedSvgDoc = new SVGLoader()
                .load(ShowOutputButton.class.getResource("/icons/eyeclosed.svg"), provider);

        eyeOpenIcon = new SVGIcon(eyeOpenSvgDoc, size, size);
        eyeClosedIcon = new SVGIcon(eyeClosedSvgDoc, size, size);
        dynamicEyeColor = processor.customColorForId("foreground");
        dynamicGlowColor = processor.customColorForId("glow");

        dynamicGlowColor.setColor(new Color(0, 0, 0, 0));

        showOutput = module.canShowOutput();

        putClientProperty(FlatClientProperties.STYLE, "arc: 0");
        setBorderPainted(false);
        addActionListener(this);
        addMouseListener(this);
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

        if (showOutput)
            setIcon(eyeOpenIcon);
        else
            setIcon(eyeClosedIcon);

        if ((module.isEnabled()) && module.isReachable() &&
                module.isRunnable()) {
            dynamicEyeColor.setColor(Colours.getDarkGrey(isDark));
        } else if ((module.isEnabled()) & !module.isReachable()) {
            dynamicEyeColor.setColor(Colours.getOrange(isDark));
        } else if ((module.isEnabled()) & !module.isRunnable()) {
            dynamicEyeColor.setColor(Colours.getRed(isDark));
        } else if (!module.isEnabled()) {
            dynamicEyeColor.setColor(Color.GRAY);
        }
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        // Invert state
        showOutput = !showOutput;

        updateState();

        module.setShowOutput(showOutput);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dynamicGlowColor.setColor(Colours.getDarkBlue(false));
        repaint();

    }

    @Override
    public void mouseExited(MouseEvent e) {
        dynamicGlowColor.setColor(new Color(0, 0, 0, 0));
        repaint();
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
