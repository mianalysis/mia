package wbif.sjx.ModularImageAnalysis.GUI;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class FileParameter extends JButton implements ActionListener {
    private GUI gui;
    private HCModule module;
    private Parameter parameter;
    private String fileType = FileTypes.EITHER_TYPE;

    public interface FileTypes {
        String FILE_TYPE = "Global";
        String FOLDER_TYPE = "Local";
        String EITHER_TYPE = "Either";

    }

    FileParameter(GUI gui, HCModule module, Parameter parameter, String fileType) {
        this.gui = gui;
        this.module = module;
        this.parameter = parameter;
        this.fileType = fileType;

        setText(FilenameUtils.getName(parameter.getValue()));
        addActionListener(this);
        setFocusPainted(false);

    }

    public HCModule getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select file");
        fileChooser.setMultiSelectionEnabled(false);

        switch (fileType) {
            case FileTypes.EITHER_TYPE:
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                break;

            case FileTypes.FILE_TYPE:
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;

            case FileTypes.FOLDER_TYPE:
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                break;
        }

        fileChooser.showDialog(null,"Open");

        parameter.setValue(fileChooser.getSelectedFile().getAbsolutePath());
        setText(FilenameUtils.getName(parameter.getValue()));

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) gui.setLastModuleEval(idx-1);

        gui.updateModules();

    }
}
