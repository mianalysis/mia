package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import ij.IJ;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.ComponentFactory;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.OutputStreamTextField;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

/**
 * Created by sc13967 on 21/11/2017.
 */
public class DeployedGUI extends GUI implements ActionListener {
    private int frameWidth = 350;
    private int frameHeight = 600;
    private int elementHeight = 25;

    private JFrame frame = new JFrame();
    private JPanel basicControlPanel = new JPanel();
    private JPanel basicModulesPanel = new JPanel();
    private JScrollPane basicModulesScrollPane = new JScrollPane(basicModulesPanel);
    private JPanel statusPanel = new JPanel();

    private Analysis analysis;
    private String name;
    private String version;
    private ComponentFactory componentFactory;

    public static void main(String[] args) throws IllegalAccessException, ParserConfigurationException, IOException, InstantiationException, URISyntaxException, SAXException, ClassNotFoundException {
        new DeployedGUI("/2017-11-20 Cilia analysis.mia","t","1");
    }

    public DeployedGUI(String analysisResourcePath, String name, String version) throws URISyntaxException, SAXException, IllegalAccessException, IOException, InstantiationException, ParserConfigurationException, ClassNotFoundException {
        InputStream analysisResourceStream = DeployedGUI.class.getResourceAsStream(analysisResourcePath);
        Analysis analysis = new AnalysisHandler().loadAnalysis(analysisResourceStream);

        new DeployedGUI(analysis,name,version);

    }

    public DeployedGUI(Analysis analysis, String name, String version) throws IllegalAccessException, InstantiationException {
        this.analysis = analysis;
        this.name = name;
        this.version = version;

        componentFactory = new ComponentFactory(this, elementHeight);

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameWidth) / 2, (screenSize.height - frameHeight) / 2);
        frame.setResizable(false);

        frame.setLayout(new GridBagLayout());
        frame.setTitle(name+" (version " + version + ")");

        render();

        frame.setVisible(true);

    }

    void render() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;

        // Initialising the control panel
        initialiseControlPanel();
        frame.add(basicControlPanel,c);

        // Initialising the parameters panel
        initialiseModulesPanel();
        c.gridy++;
        frame.add(basicModulesScrollPane, c);

        // Initialising the status panel
        initialiseStatusPanel(frameWidth);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.insets = new Insets(5,5,5,5);
        frame.add(statusPanel,c);

        frame.pack();
        frame.revalidate();
        frame.repaint();

    }

    private void initialiseControlPanel() {
        basicControlPanel = new JPanel();
        int buttonSize = 50;

        basicControlPanel = new JPanel();
        basicControlPanel.setPreferredSize(new Dimension(frameWidth, buttonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        JButton startAnalysisButton = new JButton("Start");
        startAnalysisButton.addActionListener(this);
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        basicControlPanel.add(startAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();

    }

    private void initialiseModulesPanel() {
        int elementWidth = frameWidth;

        // Initialising the scroll panel
        basicModulesScrollPane.setPreferredSize(new Dimension(elementWidth, frameHeight - 165));
        basicModulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicModulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        basicModulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Initialising the panel for module buttons
        basicModulesPanel.setLayout(new GridBagLayout());

        updateModules();

    }

    public void updateModules() {
        basicModulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        // Adding input control options
        c.gridy++;
        JPanel inputPanel = componentFactory.createBasicModuleControl(analysis.getInputControl(),frameWidth-40);
        if (inputPanel != null) basicModulesPanel.add(inputPanel,c);

        // Adding module buttons
        ModuleCollection modules = analysis.getModules();
        for (HCModule module : modules) {
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;
            c.gridy++;

            JPanel modulePanel = componentFactory.createBasicModuleControl(module,frameWidth-40);
            if (modulePanel!=null) basicModulesPanel.add(modulePanel,c);

        }

        c.gridy++;
        c.weighty = 100;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0, 15));
        basicModulesPanel.add(separator, c);

        basicModulesPanel.validate();
        basicModulesPanel.repaint();
        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    private void initialiseStatusPanel(int width) {
        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(width, 40));
        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        JTextField textField = new JTextField();
        textField.setBackground(null);
        textField.setPreferredSize(new Dimension(width - 20, 25));
        textField.setBorder(null);
        textField.setText(name+" (version " + version + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statusPanel.add(textField, c);

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream printStream = new PrintStream(outputStreamTextField);
        System.setOut(printStream);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Start")) {
            Thread t = new Thread(() -> {
                try {
                    new AnalysisHandler().startAnalysis(analysis);
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                } catch (GenericMIAException e1) {
                    IJ.showMessage(e1.getMessage());
                }
            });
            t.start();
        }
    }
}
