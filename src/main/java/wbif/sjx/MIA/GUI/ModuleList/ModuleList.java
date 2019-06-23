package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.InputOutput.ImageSaver;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ModuleList {
    private static Thread t;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        try {
            new ModuleList().test();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void test() throws UnsupportedEncodingException {
        JFrame frame = new JFrame();

        ModuleCollection modules = GUI.getModules();

        Module mod1 = new ImageLoader<>(modules);
        Module mod2 = new FilterImage(modules);
        Module mod3 = new ImageSaver(modules);

        modules.add(mod1);
        modules.add(mod2);
        modules.add(mod3);

        // Setting parameters for ImageLoader
        ImageLoader imageLoader = (ImageLoader) mod1;
        imageLoader.updateParameterValue(ImageLoader.IMPORT_MODE, ImageLoader.ImportModes.SPECIFIC_FILE);
        String pathToImage = "C:\\Users\\steph\\Documents\\Java Projects\\ModularImageAnalysis\\src\\test\\resources\\images\\ImageFilter\\LabelledObjects5D_8bit_2pxVariance2D.tif";
        imageLoader.updateParameterValue(ImageLoader.FILE_PATH,pathToImage);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,"Test_Output_Image");

        // Setting parameters for FilterImage
        FilterImage filterImage = (FilterImage) mod2;
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,"Test_Output_Image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,"Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE,FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS,false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS,2d);

        String[] columnNames = {"Enable", "ShowOutput", "Title", "Evaluate"};
        Object[][] data = {{mod1, mod1, mod1, mod1},
                {mod2,mod2,mod2,mod2},
                {mod3,mod3,mod3,mod3},
        };

        MyTableModel tableModel = new MyTableModel(data,columnNames,modules);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setTableHeader(null);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new MyTransferHandler(table));
        table.getColumn("Enable").setPreferredWidth(20);
        table.getColumn("ShowOutput").setPreferredWidth(20);
        table.getColumn("Title").setPreferredWidth(200);
        table.getColumn("Evaluate").setPreferredWidth(20);
        table.setRowHeight(30);
        table.setShowGrid(false);

        Action enable = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                Module module = (Module) table.getModel().getValueAt(modelRow,0);
                module.setEnabled(!module.isEnabled());

            }
        };
        EnableButtonColumn enableButtonColumn = new EnableButtonColumn(table, enable, 0);

        Action showOutput = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                Module module = (Module) table.getModel().getValueAt(modelRow,0);
                module.setShowOutput(!module.canShowOutput());

            }
        };
        ShowOutputButtonColumn showOutputButtonColumn = new ShowOutputButtonColumn(table, showOutput, 1);

        Action evaluate = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                Module module = (Module) table.getModel().getValueAt(modelRow,0);

                Module.setVerbose(true);
                evaluateModules(module,table);

            }
        };
        EvaluateButtonColumn evaluateButtonColumn = new EvaluateButtonColumn(table, evaluate, 3);

        table.setFillsViewportHeight(true);
        frame.add(scrollPane);
        frame.setPreferredSize(new Dimension(300,200));
        frame.pack();
        frame.setVisible(true);

        table.repaint();

    }

    private void evaluateModules(Module module,JTable table) {
        if (!module.isEnabled()) return;

        int idx = GUI.getModules().indexOf(module);

        // If it's currently evaluating, this will kill the thread
        if (idx == GUI.getModuleBeingEval()) {
            System.out.println("Stopping");
            GUI.setModuleBeingEval(-1);
            GUI.updateModuleStates(true);
            t.stop();
            return;
        }

        // If the module is ready to be evaluated
        if (idx <= GUI.getLastModuleEval()) {
            t = new Thread(() -> {
                try {
                    // For some reason it's necessary to have a brief pause here to prevent the module executing twice
                    Thread.sleep(1);
                    table.repaint();
                    evaluateModule(module);
                    table.repaint();
                } catch (Exception e1) {
                    GUI.setModuleBeingEval(-1);
                    GUI.updateModuleStates(true);
                    e1.printStackTrace();
                    table.repaint();
                }
            });
            t.start();

        } else {
            // If multiple modules will need to be evaluated first
            t = new Thread(() -> {
                for (int i = GUI.getLastModuleEval() + 1; i <= idx; i++) {
                    Module currModule = GUI.getModules().get(i);
                    if (currModule.isEnabled() && currModule.isRunnable()) try {
                        table.repaint();
                        evaluateModule(currModule);
                        table.repaint();
                    } catch (Exception e1) {
                        GUI.setModuleBeingEval(-1);
                        e1.printStackTrace();
                        Thread.currentThread().getThreadGroup().interrupt();
                        table.repaint();
                    }
                }
            });
            t.start();
        }
    }

    private void evaluateModule(Module module) {
        ModuleCollection modules = GUI.getAnalysis().getModules();
        Workspace testWorkspace = GUI.getTestWorkspace();

        // Setting the index to the previous module.  This will make the currently-evaluated module go red
        GUI.setLastModuleEval(modules.indexOf(module) - 1);
        GUI.setModuleBeingEval(modules.indexOf(module));
//        GUI.updateModuleStates();

        Module.setVerbose(true);
        module.execute(testWorkspace);
//        GUI.setLastModuleEval(modules.indexOf(module));
//        GUI.setModuleBeingEval(-1);

//        GUI.updateModuleStates();

    }
}
