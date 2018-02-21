package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import ij.IJ;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.MainGUI;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by steph on 28/07/2017.
 */
public class AnalysisControlButton extends JButton implements ActionListener {
    public static final String LOAD_ANALYSIS = "Load";
    public static final String SAVE_ANALYSIS = "Save";
    public static final String START_ANALYSIS = "Run";
    public static final String STOP_ANALYSIS = "Stop";

    private MainGUI gui;
    private static int buttonSize = 50;

    public AnalysisControlButton(MainGUI gui, String command) {
        this.gui = gui;

        addActionListener(this);
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setText(command);
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    public static int getButtonSize() {
        return buttonSize;
    }

    public static void setButtonSize(int buttonSize) {
        AnalysisControlButton.buttonSize = buttonSize;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case LOAD_ANALYSIS:
                    GUIAnalysis analysis = (GUIAnalysis) new AnalysisHandler().loadAnalysis();
                    if (analysis == null) return;

                    gui.setAnalysis(analysis);

                    if (gui.isBasicGUI()) {
                        gui.populateBasicModules();

                    } else {
                        gui.populateModuleList();
                        gui.populateModuleParameters();

                    }

                    gui.setLastModuleEval(-1);
                    gui.render();

                    break;

                case SAVE_ANALYSIS:
                    new AnalysisHandler().saveAnalysis(gui.getAnalysis());
                    break;

                case START_ANALYSIS:
                    Thread t = new Thread(() -> {
                        try {
                            new AnalysisHandler().startAnalysis(gui.getAnalysis());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (GenericMIAException e1) {
                            IJ.showMessage(e1.getMessage());
                        }
                    });
                    t.start();
                    break;

                case STOP_ANALYSIS:
                    System.out.println("Shutting system down");
                    new AnalysisHandler().stopAnalysis();
                    break;

            }

        } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException
                | IllegalAccessException | InstantiationException | TransformerException e1) {
            e1.printStackTrace();
        }
    }
}
