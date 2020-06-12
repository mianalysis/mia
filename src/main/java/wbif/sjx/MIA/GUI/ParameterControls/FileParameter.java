package wbif.sjx.MIA.GUI.ParameterControls;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Parameters.Abstract.FileFolderType;

/**
 * Created by Stephen on 20/05/2017.
 */
public class FileParameter extends ParameterControl implements ActionListener {
    private FileFolderType parameter;
    private String fileType;
    private JButton control;

    public interface FileTypes {
        String FILE_TYPE = "File";
        String FOLDER_TYPE = "Folder";
        String EITHER_TYPE = "Either";

    }

    public FileParameter(FileFolderType parameter, String fileType) {
        this.parameter = parameter;
        this.fileType = fileType;

        control = new JButton();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setToolTipText(parameter.getPath());
        control.setText(FilenameUtils.getName(parameter.getPath()));
        control.addActionListener(this);
        control.setFocusPainted(false);

    }

    public FileFolderType getParameter() {
        return parameter;
    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setText(FilenameUtils.getName(parameter.getPath()));
        control.setToolTipText(parameter.getPath());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

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

        if (parameter.getPath() != null) {
            fileChooser.setCurrentDirectory(new File((String) parameter.getPath()));
        }
        fileChooser.showDialog(null,"Open");

        if (fileChooser.getSelectedFile() == null) return;

        parameter.setPath(fileChooser.getSelectedFile().getAbsolutePath());

        Module module = parameter.getModule();
        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval() & !(module instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        if (module.getClass().isInstance(new InputControl(GUI.getModules()))) GUI.updateTestFile(true);

        GUI.updateModuleStates(true);
        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

    }
}
