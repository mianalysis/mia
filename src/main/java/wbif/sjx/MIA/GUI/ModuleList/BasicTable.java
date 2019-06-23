package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.ControlObjects.EvalButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledButton;
import wbif.sjx.MIA.GUI.ControlObjects.ShowOutputButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.InputOutput.ImageSaver;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;

public class BasicTable {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        new BasicTable().test();

    }

//    Gemma = new function = perfect
//    when, Gemma = good, Stephen need not code
//    so i+1 = playtime)
//    foreloop until i+1 = playtime
//    don't stop, for no one)
//
//    CLOSED LOOP';

    public void test() {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;

        // Creating the module name table
        ModuleCollection modules = getModules();
        JTable moduleNameTable = getModuleNameTable(modules);

        // Creating control buttons for modules
        for (Module module:modules) {
            c.gridx = 0;

            ModuleEnabledButton enabledButton = new ModuleEnabledButton(module);
            enabledButton.setPreferredSize(new Dimension(26,26));
            panel.add(enabledButton,c);
            c.gridx++;

            ShowOutputButton showOutputButton = new ShowOutputButton(module);
            showOutputButton.setPreferredSize(new Dimension(26,26));
            panel.add(showOutputButton,c);
            c.gridx++;
            c.gridx++;

            EvalButton evalButton = new EvalButton(module);
            evalButton.setPreferredSize(new Dimension(26,26));
            panel.add(evalButton,c);

            c.gridy++;

        }

        // Adding the draggable module list to the third column
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = modules.size();
        panel.add(moduleNameTable,c);

        frame.add(panel);
        frame.setPreferredSize(new Dimension(300, 200));
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * Creates an example ModuleCollection with a few modules
     * @return
     */
    private ModuleCollection getModules() {
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
        imageLoader.updateParameterValue(ImageLoader.FILE_PATH, pathToImage);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE, "Test_Output_Image");

        // Setting parameters for FilterImage
        FilterImage filterImage = (FilterImage) mod2;
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE, "Test_Output_Image");
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT, false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE, "Test_output");
        filterImage.updateParameterValue(FilterImage.FILTER_MODE, FilterImage.FilterModes.GAUSSIAN2D);
        filterImage.updateParameterValue(FilterImage.CALIBRATED_UNITS, false);
        filterImage.updateParameterValue(FilterImage.FILTER_RADIUS, 2d);

        return modules;

    }

    private JTable getModuleNameTable(ModuleCollection modules) {
        String[] columnNames = {"Title"};
        Object[][] data = new Object[modules.size()][1];
        for (int i=0;i<modules.size();i++) data[i][0] = modules.get(i);

        MyTableModel tableModel = new MyTableModel(data, columnNames,modules);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setTableHeader(null);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new MyTransferHandler(table));
        table.getColumn("Title").setPreferredWidth(200);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);

        return table;

    }
}
