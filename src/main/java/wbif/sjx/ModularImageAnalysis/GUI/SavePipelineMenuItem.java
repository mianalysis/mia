package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SavePipelineMenuItem extends JMenuItem implements ActionListener {
    MainGUI gui;

    SavePipelineMenuItem(MainGUI gui) {
        this.gui = gui;

        setText("Save pipeline");
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            new AnalysisHandler().saveAnalysis(gui.getAnalysis());
        } catch (IOException | ParserConfigurationException | TransformerException e1) {
            e1.printStackTrace();
        }
    }
}
