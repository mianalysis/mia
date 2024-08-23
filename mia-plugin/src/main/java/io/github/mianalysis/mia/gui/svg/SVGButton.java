package io.github.mianalysis.mia.gui.svg;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JButton;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;

import io.github.mianalysis.mia.gui.regions.workflowmodules.ShowOutputButton;
import io.github.mianalysis.mia.object.system.Colours;

public abstract class SVGButton extends JButton implements MouseListener {
    protected DynamicAWTSvgPaint dynamicForegroundColor;
    protected DynamicAWTSvgPaint dynamicGlowColor;
    protected CustomParserProvider provider;

    private SVGIcon[] icons;

    public SVGButton(String[] svgPaths, int size, int defaultIndex) {
        CustomColorsProcessor processor = new CustomColorsProcessor(Arrays.asList("glow", "foreground"));
        provider = new CustomParserProvider(processor);

        icons = new SVGIcon[svgPaths.length];
        for (int i = 0; i < svgPaths.length; i++) {
            URL url = ShowOutputButton.class.getResource(svgPaths[i]);
            SVGDocument svgDocument = new SVGLoader().load(url, provider);
            icons[i] = new SVGIcon(svgDocument, size, size);
        }

        setIcon(icons[defaultIndex]);

        dynamicForegroundColor = processor.customColorForId("foreground");
        dynamicGlowColor = processor.customColorForId("glow");
        dynamicGlowColor.setColor(new Color(0, 0, 0, 0));

        putClientProperty(FlatClientProperties.STYLE, "arc: 0");
        setBorderPainted(false);        
        addMouseListener(this);
        setFocusPainted(false);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setSelected(false);
        setMargin(new Insets(0, 0, 0, 0));

    }

    public void selectIconByIndex(int index) {
        setIcon(icons[index]);
    }

    public abstract void updateState();

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
        dynamicGlowColor.setColor(Colours.getDarkGrey(false));
        repaint();

    }

    @Override
    public void mouseExited(MouseEvent e) {
        dynamicGlowColor.setColor(new Color(0, 0, 0, 0));
        repaint();
    }
}
