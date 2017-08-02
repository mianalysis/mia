package wbif.sjx.ModularImageAnalysis;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;
import wbif.sjx.ModularImageAnalysis.GUI.MainGUI;
import wbif.sjx.common.Object.LUTs;

import javax.swing.*;

/**
 * Created by sc13967 on 14/07/2017.
 */
public class ModularImageAnalysisPlugin implements PlugIn {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());

        new ImageJ();
        new MainGUI();

    }

    @Override
    public void run(String s) {
        try {
            new MainGUI();
        } catch (InstantiationException | IllegalAccessException e) {
            IJ.log("Error");
            e.printStackTrace();
        }
    }
}
