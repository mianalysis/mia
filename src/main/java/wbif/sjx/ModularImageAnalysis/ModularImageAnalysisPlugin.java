// TODO: Add input/output parameters to .mia file (and condition to avoid problems if this isn't present in files)
// TODO: Set "Run" button to simply start processing.  File path should be set at input options

package wbif.sjx.ModularImageAnalysis;

import ij.IJ;
import ij.ImageJ;
import ij.Prefs;
import ij.plugin.PlugIn;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.MainGUI;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sc13967 on 14/07/2017.
 */
public class ModularImageAnalysisPlugin implements PlugIn {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());


                new ImageJ();
                Prefs.setThreads(1);
                new MainGUI(true);

            } else {
                Prefs.setThreads(1);
                String filepath = args[0];

                AnalysisHandler analysisHandler = new AnalysisHandler();
                InputStream inputStream = new FileInputStream(filepath);

                Analysis analysis = analysisHandler.loadAnalysis(inputStream);
                analysisHandler.startAnalysis(analysis);

            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ParserConfigurationException | IOException | GenericMIAException | InterruptedException | SAXException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void run(String s) {
        try {
            new MainGUI(false);
        } catch (InstantiationException | IllegalAccessException e) {
            IJ.log("Error");
            e.printStackTrace();
        }
    }
}
