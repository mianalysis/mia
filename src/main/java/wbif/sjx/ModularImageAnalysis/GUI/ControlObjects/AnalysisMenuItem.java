package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import ij.IJ;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.MainGUI;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by steph on 28/07/2017.
 */
public class AnalysisMenuItem extends JMenuItem implements ActionListener {
    public static final String LOAD_ANALYSIS = "Load pipeline";
    public static final String SAVE_ANALYSIS = "Save pipeline";
    public static final String START_ANALYSIS = "Run analysis";
    public static final String STOP_ANALYSIS = "Stop analysis";
    public static final String CLEAR_PIPELINE = "Remove all modules";

    private MainGUI gui;

    public AnalysisMenuItem(MainGUI gui, String command) {
        this.gui = gui;

        setText(command);
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case LOAD_ANALYSIS:
                    GUIAnalysis analysis = (GUIAnalysis) new AnalysisHandler().loadAnalysis();

                    if (analysis==null) return;

                    gui.setAnalysis(analysis);

                    if (gui.isBasicGUI()) {
                        gui.populateBasicModules();

                    } else {
                        gui.populateModuleList();
                        gui.populateModuleParameters();

                    }

                    gui.setLastModuleEval(-1);

                    break;

                case SAVE_ANALYSIS:
                    new AnalysisHandler().saveAnalysis(gui.getAnalysis());
                    break;

                case START_ANALYSIS:
                    Thread t = new Thread(() -> {
                        try {
                            new AnalysisHandler().startAnalysis(gui.getAnalysis());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (GenericMIAException e1) {
                            IJ.showMessage(e1.getMessage());
                        }
                    });
                    t.start();
                    break;

                case STOP_ANALYSIS:
                    System.out.println("Shutting system down");
                    new AnalysisHandler().stopAnalysis();
                    break;

                case CLEAR_PIPELINE:
                    gui.getAnalysis().removeAllModules();

                    if (gui.isBasicGUI()) {
                        gui.populateBasicModules();

                    } else {
                        gui.populateModuleList();
                        gui.populateModuleParameters();

                    }

                    gui.setLastModuleEval(-1);

                    break;

            }

        } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException
                | IllegalAccessException | InstantiationException | TransformerException e1) {
            e1.printStackTrace();
        }
    }
}
