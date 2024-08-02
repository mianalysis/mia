// Taken from https://stackoverflow.com/questions/11278209/how-can-i-make-comboboxs-list-wider (Accessed 29-06-2017)

package io.github.mianalysis.mia.gui.parametercontrols;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import java.awt.*;

public class WiderDropDownCombo extends JComboBox {
    /**
     *
     */
    private static final long serialVersionUID = 6268185303269647377L;
    private boolean layingOut = false;
    private int widestLength = 0;
    private boolean wide = false;

    public WiderDropDownCombo() {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setMaximumRowCount(20);
    }

    public WiderDropDownCombo(Object[] objs) {
        super(objs);

        putClientProperty( FlatClientProperties.STYLE, "arc: 16" );
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setMaximumRowCount(20);
    }

    public boolean isWide() {
        return wide;
    }

    // Setting the JComboBox wide
    public void setWide(boolean wide) {
        this.wide = wide;
        widestLength = getWidestItemWidth();
    }

    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layingOut && isWide()) dim.width = Math.max(widestLength, dim.width);

        return dim;
    }

    public int getWidestItemWidth() {
        int numOfItems = this.getItemCount();
        Font font = this.getFont();
        FontMetrics metrics = this.getFontMetrics(font);
        int widest = 0;
        for (int i = 0; i < numOfItems; i++) {
            Object item = this.getItemAt(i);
            if (item == null) continue;
            int lineWidth = metrics.stringWidth(item.toString());
            widest = Math.max(widest, lineWidth);
        }

        return widest + 5;

    }

    public void doLayout() {
        try {
            layingOut = true;
            super.doLayout();
        } finally {
            layingOut = false;
        }
    }
}