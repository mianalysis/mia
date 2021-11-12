/*
 * "Concave" hulls by Glenn Hudson and Matt Duckham
 *
 * Source code downloaded from https://archive.md/l3Un5#selection-571.0-587.218 on 3rd November 2021.
 *
 * - This software is Copyright (C) 2008 Glenn Hudson released under Gnu Public License (GPL). Under 
 *   GPL you are free to use, modify, and redistribute the software. Please acknowledge Glenn Hudson 
 *   and Matt Duckham as the source of this software if you do use or adapt the code in further research 
 *   or other work. For full details of GPL see http://www.gnu.org/licenses/gpl-3.0.txt.
 * - This software comes with no warranty of any kind, expressed or implied.
 * 
 * A paper with full details of the characteristic hulls algorithm is published in Pattern Recognition.
 * Duckham, M., Kulik, L., Worboys, M.F., Galton, A. (2008) Efficient generation of simple polygons for
 * characterizing the shape of a set of points in the plane. Pattern Recognition v41, 3224-3236
 *
 * The software was developed by Glenn Hudson while working with me as an RA. The characteristic shapes 
 * algorithm is collaborative work between Matt Duckham, Lars Kulik, Antony Galton, and Mike Worboys.
 * 
 */

package signalprocesser.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class JCollapsiblePanel extends JPanel implements MouseListener {

    private static final int DIST_FROM_LEFT = 30;
    private static final int DIST_FROM_TOP = 2;
    private static final int RECT_WIDTH = 14;
    private static final int RECT_HEIGHT = 10;
    private static final int TRIANGLE_MARGINLEFT = 3;
    private static final int TRIANGLE_MARGINTOP = 2;

    private boolean iscollapsed = false;
    private JPanel innercomponent = null;

    public JCollapsiblePanel() {
        // Add mouse listener
        super.addMouseListener(this);

        // Setup layout
        if (innercomponent == null) {
            innercomponent = new JPanel();
        }
        super.setLayout(new BorderLayout());
        super.add(innercomponent, BorderLayout.CENTER);
    }

    public JCollapsiblePanel(LayoutManager layout) {
        // Add mouse listener
        super.addMouseListener(this);

        // Setup layout
        if (innercomponent == null) {
            innercomponent = new JPanel(layout);
        } else {
            innercomponent.setLayout(layout);
        }
        super.setLayout(new BorderLayout());
        super.add(innercomponent, BorderLayout.CENTER);
    }

    public void setCollapsed(boolean b) {
        iscollapsed = b;
        super.removeAll();
        this.revalidate();
    }

    public void paint(Graphics g) {
        // Paint as normal
        super.paint(g);

        // Get width
        int width = getWidth();

        // Paint up and down boxes
        g.setColor(super.getBackground());
        g.fill3DRect(width - DIST_FROM_LEFT, DIST_FROM_TOP, RECT_WIDTH, RECT_HEIGHT, true);

        // Draw triangle
        int xcoord[] = new int[3];
        int ycoord[] = new int[3];
        xcoord[0] = width - DIST_FROM_LEFT + TRIANGLE_MARGINLEFT - 1;
        xcoord[1] = width - DIST_FROM_LEFT + RECT_WIDTH - TRIANGLE_MARGINLEFT - 1;
        xcoord[2] = width - DIST_FROM_LEFT + (RECT_WIDTH / 2) - 1;
        if (iscollapsed == false) {
            ycoord[0] = DIST_FROM_TOP + TRIANGLE_MARGINTOP;
            ycoord[1] = ycoord[0];
            ycoord[2] = DIST_FROM_TOP + RECT_HEIGHT - TRIANGLE_MARGINTOP;
        } else {
            ycoord[0] = DIST_FROM_TOP + RECT_HEIGHT - TRIANGLE_MARGINTOP - 1;
            ycoord[1] = ycoord[0];
            ycoord[2] = DIST_FROM_TOP + TRIANGLE_MARGINTOP - 1;
        }
        g.setColor(Color.blue);
        g.fillPolygon(xcoord, ycoord, 3);
    }

    public void mouseClicked(MouseEvent e) {
        // Statements ordered as they are for performance reasons
        if ((getWidth() - DIST_FROM_LEFT) <= e.getX() && e.getY() <= (DIST_FROM_TOP + RECT_HEIGHT)
                && e.getX() <= (getWidth() - DIST_FROM_LEFT + RECT_WIDTH) && DIST_FROM_TOP <= e.getY()) {
            if (iscollapsed) {
                iscollapsed = false;
                super.add(innercomponent, BorderLayout.CENTER);
            } else {
                iscollapsed = true;
                super.removeAll();
            }
            this.revalidate();
            // this.repaint();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void setLayout(LayoutManager layout) {
        // Create inner component if none
        if (innercomponent == null) {
            innercomponent = new JPanel();
        }

        // Update layout
        // if ( layout instanceof BoxLayout ) {
        // Reflect.setfield(layout, "target", innercomponent);
        // }

        // Set layout
        innercomponent.setLayout(layout);
    }

    public Component add(Component comp) {
        return innercomponent.add(comp);
    }

    public Component add(Component comp, int index) {
        return innercomponent.add(comp, index);
    }

    public void add(Component comp, Object constraints) {
        innercomponent.add(comp, constraints);
    }

    public void add(Component comp, Object constraints, int index) {
        innercomponent.add(comp, constraints, index);
    }

    public Component add(String name, Component comp) {
        return innercomponent.add(name, comp);
    }
}
