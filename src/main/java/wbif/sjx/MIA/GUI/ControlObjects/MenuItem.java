package wbif.sjx.MIA.GUI.ControlObjects;

import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.DocumentationPanel;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
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
import java.lang.reflect.InvocationTargetException;

/**
 * Created by stephen on 28/07/2017.
 */
public class MenuItem extends JMenuItem implements ActionListener {
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
    public static final String SHOW_GLOBAL_VARIABLES = "Show global variables";
    public static final String SHOW_ABOUT = "About";
    public static final String SHOW_GETTING_STARTED = "Getting started";

    public MenuItem(String command) {
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
                    ModuleCollection modules = analysis.getModules();
                    modules.add(new ImageLoader<>(modules));

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
                    setText(MenuItem.EDITING_VIEW);
                    break;

                case EDITING_VIEW:
                    GUI.enableEditingMode();
                    GUI.setActiveModule(null);
                    setText(MenuItem.BASIC_VIEW);
                    break;

                case SHOW_GLOBAL_VARIABLES:
                    GUI.setActiveModule(MIA.getGlobalVariables());
                    GUI.updateParameters();
                    break;

                case SHOW_ABOUT:
                    DocumentationPanel.showAbout();
                    break;

                case SHOW_GETTING_STARTED:
                    DocumentationPanel.showGettingStarted();
                    break;

            }

        } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException
                | IllegalAccessException | InstantiationException | TransformerException | NoSuchMethodException
                | InvocationTargetException e1) {
            e1.printStackTrace();
        }
    }
}
