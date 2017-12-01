// Taken from https://stackoverflow.com/questions/11278209/how-can-i-make-comboboxs-list-wider (Accessed 29-06-2017)

package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import javax.swing.*;
import java.awt.*;

public class WiderDropDownCombo extends JComboBox {

    private String type;
    private boolean layingOut = false;
    private int widestLengh = 0;
    private boolean wide = false;

    public WiderDropDownCombo() {

    }

    public WiderDropDownCombo(Object[] objs) {
        super(objs);
    }

    public boolean isWide() {
        return wide;
    }

    // Setting the JComboBox wide
    public void setWide(boolean wide) {
        this.wide = wide;
        widestLengh = getWidestItemWidth();

    }

    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layingOut && isWide())
            dim.width = Math.max(widestLengh, dim.width);
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