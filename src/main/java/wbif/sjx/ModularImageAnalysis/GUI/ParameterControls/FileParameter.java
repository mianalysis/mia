package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by Stephen on 20/05/2017.
 */
public class FileParameter extends JButton implements ActionListener {
    private GUI gui;
    private Module module;
    private Parameter parameter;
    private String fileType;

    public interface FileTypes {
        String FILE_TYPE = "File";
        String FOLDER_TYPE = "Folder";
        String EITHER_TYPE = "Either";

    }

    public FileParameter(GUI gui, Module module, Parameter parameter, String fileType) {
        this.gui = gui;
        this.module = module;
        this.parameter = parameter;
        this.fileType = fileType;

        setToolTipText(parameter.getValue());
        setText(FilenameUtils.getName(parameter.getValue()));
        addActionListener(this);
        setFocusPainted(false);

    }

    public Module getModule() {
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

        if (parameter.getValue() != null) {
            fileChooser.setCurrentDirectory(new File((String) parameter.getValue()));
        }
        fileChooser.showDialog(null,"Open");

        if (fileChooser.getSelectedFile() == null) return;

        parameter.setValue(fileChooser.getSelectedFile().getAbsolutePath());
        setText(FilenameUtils.getName(parameter.getValue()));
        setToolTipText(parameter.getValue());

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) gui.setLastModuleEval(idx-1);

        gui.updateTestFile();
        gui.updateModules();

    }
}
