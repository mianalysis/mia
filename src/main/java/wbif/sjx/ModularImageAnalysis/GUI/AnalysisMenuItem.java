package wbif.sjx.ModularImageAnalysis.GUI;

import ij.IJ;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
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
    static final String LOAD_ANALYSIS = "Load pipeline";
    static final String SAVE_ANALYSIS = "Save pipeline";
    static final String SET_FILE_TO_ANALYSE = "Set file to analyse";
    static final String START_ANALYSIS = "Run analysis";
    static final String STOP_ANALYSIS = "Stop analysis";
    static final String CLEAR_PIPELINE = "Remove all modules";

    private MainGUI gui;

    AnalysisMenuItem(MainGUI gui, String command) {
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

                case SET_FILE_TO_ANALYSE:
                    FileDialog fileDialog = new FileDialog(new Frame(), "Select file to run", FileDialog.LOAD);
                    fileDialog.setMultipleMode(false);
                    fileDialog.setVisible(true);

                    gui.setTestWorkspace(new Workspace(1, fileDialog.getFiles()[0]));

                    // Updating currently-processed modules to none
                    gui.setLastModuleEval(-1);
                    gui.populateModuleList();

                    System.out.println("Set current file to \"" + fileDialog.getFiles()[0].getName() + "\"");

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
