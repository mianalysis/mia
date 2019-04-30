package wbif.sjx.MIA.GUI.ControlObjects;

import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisReader;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisRunner;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisWriter;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by stephen on 28/07/2017.
 */
public class AnalysisMenuItem extends JMenuItem implements ActionListener {
    public static final String NEW_PIPELINE = "New pipeline";
    public static final String LOAD_PIPELINE = "Load pipeline";
    public static final String SAVE_PIPELINE = "Save pipeline";
    public static final String RUN_ANALYSIS = "Run analysis";
    public static final String STOP_ANALYSIS = "Stop analysis";
    public static final String RESET_ANALYSIS = "Reset analysis";
    public static final String ENABLE_ALL = "Enable all modules";
    public static final String DISABLE_ALL = "Disable all modules";
    public static final String OUTPUT_ALL = "Show output for all modules";
    public static final String SILENCE_ALL = "Hide output for all modules";
    public static final String BASIC_VIEW = "Switch to basic view";
    public static final String EDITING_VIEW = "Switch to editing view";
    public static final String SHOW_HELP_NOTES = "Show help and notes panel";
    public static final String HIDE_HELP_NOTES = "Hide help and notes panel";
    public static final String TOGGLE_HELP_NOTES = "Toggle help and notes panel";

    public AnalysisMenuItem(String command) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case NEW_PIPELINE:
                    int savePipeline = JOptionPane.showConfirmDialog(new Frame(),"Save existing pipeline?", "Create new pipeline", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

                    switch (savePipeline) {
                        case -1: // Cancel (don't create new pipeline
                            return;
                        case 0: // Save
                            AnalysisWriter.saveAnalysis(GUI.getAnalysis());
                            break;
                    }

                    Analysis analysis = new Analysis();
                    analysis.getModules().add(new ImageLoader<>());

                    GUI.setAnalysis(analysis);
                    GUI.populateModuleList();
                    GUI.populateModuleParameters();
                    GUI.populateHelpNotes();
                    GUI.setLastModuleEval(-1);

                    break;

                case LOAD_PIPELINE:
                    analysis = AnalysisReader.loadAnalysis();
                    if (analysis == null) return;

                    GUI.setAnalysis(analysis);
                    GUI.populateModuleList();
                    GUI.populateModuleParameters();
                    GUI.populateHelpNotes();
                    GUI.setLastModuleEval(-1);

                    break;

                case SAVE_PIPELINE:
                    AnalysisWriter.saveAnalysis(GUI.getAnalysis());
                    break;

                case RUN_ANALYSIS:
                    Thread t = new Thread(() -> {
                        try {
                            AnalysisRunner.startAnalysis(GUI.getAnalysis());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    });
                    t.start();
                    break;

                case STOP_ANALYSIS:
                    System.out.println("Shutting system down");
                    AnalysisRunner.stopAnalysis();
                    break;

                case ENABLE_ALL:
                    for (Module module:GUI.getModules()) module.setEnabled(true);
                    GUI.populateModuleList();
                    break;

                case DISABLE_ALL:
                    for (Module module:GUI.getModules()) module.setEnabled(false);
                    GUI.populateModuleList();
                    break;

                case OUTPUT_ALL:
                    for (Module module:GUI.getModules()) module.setShowOutput(true);
                    GUI.populateModuleList();
                    break;

                case SILENCE_ALL:
                    for (Module module:GUI.getModules()) module.setShowOutput(false);
                    GUI.populateModuleList();
                    break;

                case BASIC_VIEW:
                    GUI.enableBasicMode();
                    GUI.setActiveModule(null);
                    setText(AnalysisMenuItem.EDITING_VIEW);
                    break;

                case EDITING_VIEW:
                    GUI.enableEditingMode();
                    GUI.setActiveModule(null);
                    setText(AnalysisMenuItem.BASIC_VIEW);
                    break;

                case TOGGLE_HELP_NOTES:
                    GUI.setShowHelpNotes(!GUI.showHelpNotes());
                    GUI.updatePanel();
                    GUI.populateHelpNotes();
                    break;

            }

        } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException
                | IllegalAccessException | InstantiationException | TransformerException e1) {
            e1.printStackTrace();
        }
    }
}
