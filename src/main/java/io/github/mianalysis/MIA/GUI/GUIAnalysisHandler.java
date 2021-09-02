package io.github.mianalysis.mia.gui;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.InputOutput.ImageLoader;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Process.AnalysisHandling.Analysis;
import io.github.mianalysis.mia.Process.AnalysisHandling.AnalysisReader;
import io.github.mianalysis.mia.Process.AnalysisHandling.AnalysisRunner;
import io.github.mianalysis.mia.Process.AnalysisHandling.AnalysisWriter;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class GUIAnalysisHandler {
    public static void newAnalysis() {
        int savePipeline = JOptionPane.showConfirmDialog(new Frame(), "Save existing pipeline?", "Create new pipeline",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

        switch (savePipeline) {
            case -1: // Cancel (don't create new pipeline
                return;
            case 0: // Save
                saveAnalysis();
                break;
        }

        Analysis analysis = new Analysis();
        Modules modules = analysis.getModules();
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
        } catch (SAXException | IllegalAccessException | IOException | InstantiationException
                | ParserConfigurationException | ClassNotFoundException | NoSuchMethodException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (newAnalysis == null)
            return;

        GUI.setAnalysis(newAnalysis);
        GUI.updateModuleList();
        GUI.updateParameters();
        GUI.updateHelpNotes();
        GUI.setLastModuleEval(-1);
        GUI.updateTestFile(true);
        GUI.updateModules();
        GUI.updateModuleStates(true);
        GUI.getUndoRedoStore().reset();

    }

    public static void saveAnalysis() {
        try {
            AnalysisWriter.saveAnalysisAs(GUI.getAnalysis(), GUI.getAnalysis().getAnalysisFilename());
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
                GUI.getAnalysisRunner().run(GUI.getAnalysis());
            } catch (IOException | InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        t.start();
    }

    public static void stopAnalysis() {
        MIA.log.writeStatus("Shutting system down");
        AnalysisRunner.stopAnalysis();
    }

    public static void enableAllModules() {
        GUI.addUndo();
        for (Module module : GUI.getModules())
            module.setEnabled(true);
        GUI.updateModuleList();
    }

    public static void disableAllModules() {
        for (Module module : GUI.getModules())
            module.setEnabled(false);
        GUI.updateModuleList();
    }

    public static void enableAllModulesOutput() {
        GUI.addUndo();
        for (Module module : GUI.getModules())
            module.setShowOutput(true);
        GUI.updateModuleList();
    }

    public static void disableAllModulesOutput() {
        GUI.addUndo();
        for (Module module : GUI.getModules())
            module.setShowOutput(false);
        GUI.updateModuleList();
    }

    public static void removeModules() {
        GUI.addUndo();

        Module[] activeModules = GUI.getSelectedModules();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModules == null)
            return;

        // Getting lowest index
        Modules modules = GUI.getAnalysis().getModules();
        int lowestIdx = modules.indexOf(activeModules[0]);
        if (lowestIdx <= lastModuleEval)
            GUI.setLastModuleEval(lowestIdx - 1);

        // Removing modules
        for (Module activeModule : activeModules) {
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

        Modules modules = GUI.getAnalysis().getModules();
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules == null)
            return;

        int[] fromIndices = GUI.getSelectedModuleIndices();
        int toIndex = fromIndices[0] - 1;
        if (toIndex < 0)
            return;

        modules.reorder(fromIndices, toIndex);

        int lastModuleEval = GUI.getLastModuleEval();
        if (toIndex <= lastModuleEval)
            GUI.setLastModuleEval(toIndex - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public static void moveModuleDown() {
        GUI.addUndo();

        Modules modules = GUI.getAnalysis().getModules();
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules == null)
            return;

        int[] fromIndices = GUI.getSelectedModuleIndices();
        int toIndex = fromIndices[fromIndices.length - 1] + 2;
        if (toIndex > modules.size())
            return;

        modules.reorder(fromIndices, toIndex);

        int lastModuleEval = GUI.getLastModuleEval();
        if (fromIndices[0] <= lastModuleEval)
            GUI.setLastModuleEval(fromIndices[0] - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public static void copyModules() {
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules == null)
            return;
        if (selectedModules.length == 0)
            return;

        Modules copyModules = new Modules();
        copyModules.addAll(Arrays.asList(selectedModules));

        try {
            Analysis analysis = new Analysis();
            analysis.setModules(copyModules);
            Document doc = AnalysisWriter.prepareAnalysisDocument(analysis);
            StringWriter stringWriter = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            stringWriter.close();

            StringSelection stringSelection = new StringSelection(stringWriter.getBuffer().toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        } catch (ParserConfigurationException | TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void pasteModules() {
        try {
            Module[] selectedModules = GUI.getSelectedModules();
            if (selectedModules == null)
                return;
            if (selectedModules.length == 0)
                return;

            GUI.addUndo();
            Module toModule = selectedModules[selectedModules.length - 1];
            Modules modules = GUI.getModules();
            int toIdx = modules.indexOf(toModule);

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // DataFlavor dataFlavor = new ModulesDataFlavor();
            Transferable contents = clipboard.getContents(null);
            String copyString = (String) contents.getTransferData(DataFlavor.stringFlavor);
            Modules pasteModules = AnalysisReader.loadAnalysis(copyString).getModules();

            // Ensuring the copied modules are linked to the present Modules and
            // have a unique ID
            for (Module module : pasteModules.values()) {
                module.setModules(modules);
                module.setModuleID(String.valueOf(System.currentTimeMillis()));
            }

            // Adding the new modules
            modules.insert(pasteModules, toIdx);

            // Updating the evaluation indicator
            int lastModuleEval = GUI.getLastModuleEval();
            GUI.setLastModuleEval(Math.min(toIdx, lastModuleEval));
            GUI.updateModules();
            GUI.updateModuleStates(true);

        } catch (IOException | UnsupportedFlavorException | IllegalAccessException | InstantiationException
                | InvocationTargetException | ClassNotFoundException | ParserConfigurationException | SAXException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void toggleOutput() {
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules == null)
            return;
        if (selectedModules.length == 0)
            return;

        for (Module selectedModule : selectedModules)
            selectedModule.setShowOutput(!selectedModule.canShowOutput());

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public static void toggleEnableDisable() {
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules == null)
            return;
        if (selectedModules.length == 0)
            return;

        for (Module selectedModule : selectedModules)
            selectedModule.setEnabled(!selectedModule.isEnabled());

        int lastModuleEval = GUI.getLastModuleEval();
        int firstIdx = GUI.getAnalysis().getModules().indexOf(selectedModules[0]);
        if (firstIdx <= lastModuleEval)
            GUI.setLastModuleEval(firstIdx - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }
}
