package io.github.mianalysis.mia.gui.svg;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;

public class SVGIcon implements Icon {
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
