package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SavePipelineButton extends JButton implements ActionListener {
    MainGUI gui;

    SavePipelineButton(MainGUI gui) {
        this.gui = gui;

        setText("Save");
        addActionListener(this);
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));

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
