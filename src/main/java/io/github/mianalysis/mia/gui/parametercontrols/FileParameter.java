package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;

import ij.Prefs;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;

/**
 * Created by Stephen on 20/05/2017.
 */
public class FileParameter extends ParameterControl implements ActionListener {
    private String fileType;
    private JButton control;

    public interface FileTypes {
        String FILE_TYPE = "File";
        String FOLDER_TYPE = "Folder";
        String EITHER_TYPE = "Either";

    }

    public FileParameter(FileFolderType parameter, String fileType) {
        super(parameter);
        this.fileType = fileType;

        control = new JButton();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setToolTipText(parameter.getPath());
        control.setText(FilenameUtils.getName(parameter.getPath()));
        control.addActionListener(this);
        control.setFocusPainted(false);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setText(FilenameUtils.getName(((FileFolderType) parameter).getPath()));
        control.setToolTipText(((FileFolderType) parameter).getPath());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        String previousPath = Prefs.get("MIA.PreviousPath", "");
        JFileChooser fileChooser = new JFileChooser(previousPath);
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

        if (((FileFolderType) parameter).getPath() != null) {
            String path = (String) ((FileFolderType) parameter).getPath();
            path = checkPath(path);
            fileChooser.setCurrentDirectory(new File(path));
        }
        fileChooser.showDialog(null, "Open");

        if (fileChooser.getSelectedFile() == null)
            return;

        ((FileFolderType) parameter).setPath(fileChooser.getSelectedFile().getAbsolutePath());
        Prefs.set("MIA.PreviousPath", fileChooser.getSelectedFile().getAbsolutePath());

        Module module = parameter.getModule();
        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval() & !(module instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        if (module.getClass().isInstance(new InputControl(GUI.getModules()))) {
            new Thread(() -> {
                GUI.updateTestFile(true);
                updateControl();
                GUI.updateModules();
                GUI.updateParameters();
            }).start();
        } else {
            GUI.updateModules();
            GUI.updateParameters();
        }
    }

    private String checkPath(String path) {
        File file = new File(path);

        // Check if the full path exists
        if (file.exists())
            return path;

        // If this file doesn't exist, test to see if its parent does
        String parentPath = file.getParent();

        // If there's no parent (i.e. we're at the path root) return an empty string
        if (parentPath == null)
            return "";

        return checkPath(parentPath);

    }
}
