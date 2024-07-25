package io.github.mianalysis.mia.gui;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.inputoutput.ImageLoader;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.analysishandling.AnalysisWriter;

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
        int saveWorkflow = JOptionPane.showConfirmDialog(new Frame(), "Save existing workflow?", "Create new workflow",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

        switch (saveWorkflow) {
            case -1: // Cancel (don't create new workflow)
                return;
            case 0: // Save
                saveModules();
                break;
        }

        Modules modules = new Modules();
        modules.add(new ImageLoader<>(modules));

        GUI.setModules(modules);
        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);
        GUI.updateHelpNotes();
        GUI.setLastModuleEval(-1);
        GUI.getUndoRedoStore().reset();

    }

    public static void loadModules() {
        Modules newModules = null;
        try {
            newModules = AnalysisReader.loadModules();
        } catch (SAXException | IllegalAccessException | IOException | InstantiationException
                | ParserConfigurationException | ClassNotFoundException | NoSuchMethodException
                | InvocationTargetException e) {
            MIA.log.writeError(e);
        }
        if (newModules == null)
            return;

        GUI.setModules(newModules);
        GUI.updateHelpNotes();
        GUI.setLastModuleEval(-1);
        // new Thread(() -> {
        // GUI.updateTestFile(true);
        GUI.updateModules(true, null);
        GUI.updateParameters(false, null);
        GUI.getUndoRedoStore().reset();
        // }).start();
    }

    public static void saveModules() {
        try {
            AnalysisWriter.saveModulesAs(GUI.getModules(), GUI.getModules().getAnalysisFilename());
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            MIA.log.writeError(e);
        }
    }

    public static void saveModulesAs() {
        try {
            AnalysisWriter.saveModules(GUI.getModules());
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            MIA.log.writeError(e);
        }
    }

    public static void runAnalysis() {
        MIA.log.writeStatus("Running");
        Thread t = new Thread(() -> {
            try {
                GUI.updateProgressBar(0);
                GUI.resetJobNumbers();
                GUI.getAnalysisRunner().run(GUI.getModules());
            } catch (IOException e) {
                MIA.log.writeError(e);
            } catch (InterruptedException e) {
                // Do nothing as the user has terminated this
            }
        });
        t.start();
    }

    public static void stopAnalysis() {
        MIA.log.writeStatus("Shutting system down");
        AnalysisRunner.stopAnalysis();
        GUI.setModuleBeingEval(-1);
        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);
    }

    public static void enableAllModules() {
        GUI.addUndo();
        for (Module module : GUI.getModules())
            module.setEnabled(true);
        GUI.updateModules(true, null);
        GUI.updateParameters(false, null);
    }

    public static void disableAllModules() {
        for (Module module : GUI.getModules())
            module.setEnabled(false);
        GUI.updateModules(true, null);
        GUI.updateParameters(false, null);
    }

    public static void enableAllModulesOutput() {
        GUI.addUndo();
        for (Module module : GUI.getModules())
            module.setShowOutput(true);
        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);
    }

    public static void disableAllModulesOutput() {
        GUI.addUndo();
        for (Module module : GUI.getModules())
            module.setShowOutput(false);
        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);
    }

    public static void removeModules() {
        GUI.addUndo();

        Module[] activeModules = GUI.getSelectedModules();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModules == null)
            return;

        // Getting lowest index
        Modules modules = GUI.getModules();
        int lowestIdx = modules.indexOf(activeModules[0]);
        if (lowestIdx <= lastModuleEval)
            GUI.setLastModuleEval(lowestIdx - 1);

        // Removing modules
        for (Module activeModule : activeModules)
            modules.remove(activeModule);
        
        GUI.setSelectedModules(null);
        GUI.updateModules(true,null);
        GUI.updateParameters(false, null);
        GUI.updateHelpNotes();

    }

    public static void moveModuleUp() {
        GUI.addUndo();

        Modules modules = GUI.getModules();
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

        int startIdx = Math.min(fromIndices[0],toIndex);
        GUI.updateModules(true,modules.get(startIdx));
        GUI.updateParameters(false,null);

    }

    public static void moveModuleDown() {
        GUI.addUndo();

        Modules modules = GUI.getModules();
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

        int startIdx = Math.min(fromIndices[0],toIndex);
        GUI.updateModules(true,modules.get(startIdx));
        GUI.updateParameters(false,null);

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
            Document doc = AnalysisWriter.prepareAnalysisDocument(copyModules);
            StringWriter stringWriter = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
            stringWriter.close();

            StringSelection stringSelection = new StringSelection(stringWriter.getBuffer().toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        } catch (ParserConfigurationException | TransformerException | IOException e) {
            MIA.log.writeError(e);
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
            Modules pasteModules = AnalysisReader.loadModules(copyString);

            // Ensuring the copied modules are linked to the present Modules and
            // have a unique ID
            for (Module module : pasteModules.values()) {
                module.setModules(modules);
                module.setModuleID(String.valueOf(module.hashCode()));
            }

            // Adding the new modules
            modules.insert(pasteModules, toIdx);

            // Updating the evaluation indicator
            int lastModuleEval = GUI.getLastModuleEval();
            GUI.setLastModuleEval(Math.min(toIdx, lastModuleEval));
            GUI.updateModules(true,toModule);
            GUI.updateParameters(false,null);

        } catch (IOException | UnsupportedFlavorException | IllegalAccessException | InstantiationException
                | InvocationTargetException | ClassNotFoundException | ParserConfigurationException | SAXException
                | NoSuchMethodException e) {
            MIA.log.writeError(e);
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

        GUI.updateModules(false,null);
        GUI.updateParameters(false,null);

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
        int firstIdx = GUI.getModules().indexOf(selectedModules[0]);
        if (firstIdx <= lastModuleEval)
            GUI.setLastModuleEval(firstIdx - 1);

        GUI.updateModules(false,null);
        GUI.updateParameters(false,null);

    }
}
