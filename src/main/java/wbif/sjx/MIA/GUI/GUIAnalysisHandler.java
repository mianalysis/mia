package wbif.sjx.MIA.GUI;

import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class GUIAnalysisHandler {
    public static void newAnalysis() {
        int savePipeline = JOptionPane.showConfirmDialog(new Frame(),"Save existing pipeline?", "Create new pipeline", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

        switch (savePipeline) {
            case -1: // Cancel (don't create new pipeline
                return;
            case 0: // Save
                saveAnalysis();
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

    }

    public static void loadAnalysis() {
        Analysis newAnalysis = null;
        try {
            newAnalysis = AnalysisReader.loadAnalysis();
        } catch (SAXException | IllegalAccessException | IOException | InstantiationException |
                ParserConfigurationException | ClassNotFoundException | NoSuchMethodException |
                InvocationTargetException e) {
            e.printStackTrace();
        }
        if (newAnalysis == null) return;

        GUI.setAnalysis(newAnalysis);
        GUI.populateModuleList();
        GUI.populateModuleParameters();
        GUI.populateHelpNotes();

        GUI.setLastModuleEval(-1);
        GUI.updateTestFile();
        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public static void saveAnalysis() {
        try {
            AnalysisWriter.saveAnalysisAs(GUI.getAnalysis(),GUI.getAnalysis().getAnalysisFilename());
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void saveAnalysisAs() {
        try {
            AnalysisWriter.saveAnalysis(GUI.getAnalysis());
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void runAnalysis() {
        Thread t = new Thread(() -> {
            try {
                AnalysisRunner.startAnalysis(GUI.getAnalysis());
            } catch (IOException | InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        t.start();
    }

    public static void stopAnalysis() {
        System.out.println("Shutting system down");
        AnalysisRunner.stopAnalysis();
    }

    public static void enableAllModules() {
        for (
                Module module : GUI.getModules())
            module.setEnabled(true);
        GUI.populateModuleList();

    }

    public static void disableAllModules() {
        for (Module module:GUI.getModules()) module.setEnabled(false);
        GUI.populateModuleList();
    }

    public static void enableAllModulesOutput() {
        for (Module module:GUI.getModules()) module.setShowOutput(true);
        GUI.populateModuleList();
    }

    public static void disableAllModulesOutput() {
        for (Module module:GUI.getModules()) module.setShowOutput(false);
        GUI.populateModuleList();
    }
}
