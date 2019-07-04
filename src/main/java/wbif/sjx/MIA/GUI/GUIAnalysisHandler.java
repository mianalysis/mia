package wbif.sjx.MIA.GUI;

import org.xml.sax.SAXException;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
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
        GUI.updateModuleList();
        GUI.updateParameters();
        GUI.updateHelpNotes();
        GUI.setLastModuleEval(-1);
        GUI.getUndoRedoStore().reset();

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
        GUI.updateModuleList();
        GUI.updateParameters();
        GUI.updateHelpNotes();
        GUI.setLastModuleEval(-1);
        GUI.updateTestFile();
        GUI.updateModules();
        GUI.updateModuleStates(true);
        GUI.getUndoRedoStore().reset();

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
        GUI.addUndo();
        for (Module module : GUI.getModules()) module.setEnabled(true);
        GUI.updateModuleList();

    }

    public static void disableAllModules() {
        for (Module module:GUI.getModules()) module.setEnabled(false);
        GUI.updateModuleList();
    }

    public static void enableAllModulesOutput() {
        GUI.addUndo();
        for (Module module:GUI.getModules()) module.setShowOutput(true);
        GUI.updateModuleList();
    }

    public static void disableAllModulesOutput() {
        GUI.addUndo();
        for (Module module:GUI.getModules()) module.setShowOutput(false);
        GUI.updateModuleList();
    }

    public static void removeModules() {
        GUI.addUndo();

        Module[] activeModules = GUI.getSelectedModules();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModules == null) return;

        // Getting lowest index
        ModuleCollection modules = GUI.getAnalysis().getModules();
        int lowestIdx = modules.indexOf(activeModules[0]);
        if (lowestIdx <= lastModuleEval) GUI.setLastModuleEval(lowestIdx - 1);

        // Removing modules
        for (Module activeModule:activeModules) {
            modules.remove(activeModule);
        }

        GUI.setSelectedModules(null);
        GUI.updateModules();
        GUI.updateModuleStates(true);
        GUI.updateParameters();
        GUI.updateHelpNotes();

    }

    public static void moveModuleUp() {
        GUI.addUndo();

        ModuleCollection modules = GUI.getAnalysis().getModules();
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules== null) return;

        int[] fromIndices = GUI.getSelectedModuleIndices();
        int toIndex = fromIndices[0]-1;
        if (toIndex < 0) return;

        modules.reorder(fromIndices,toIndex);

        int lastModuleEval = GUI.getLastModuleEval();
        if (toIndex <= lastModuleEval) GUI.setLastModuleEval(toIndex - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public static void moveModuleDown() {
        GUI.addUndo();

        ModuleCollection modules = GUI.getAnalysis().getModules();
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules== null) return;

        int[] fromIndices = GUI.getSelectedModuleIndices();
        int toIndex = fromIndices[fromIndices.length-1]+2;
        if (toIndex > modules.size()) return;

        modules.reorder(fromIndices,toIndex);

        int lastModuleEval = GUI.getLastModuleEval();
        if (fromIndices[0] <= lastModuleEval) GUI.setLastModuleEval(fromIndices[0] - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }
}
