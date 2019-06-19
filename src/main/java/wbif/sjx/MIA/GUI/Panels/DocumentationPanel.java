package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.DocumentationGenerator;
import wbif.sjx.MIA.Process.Logging.Log;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.IOException;

public class DocumentationPanel {
    public static void showAbout() {
        String aboutText = DocumentationGenerator.generateAboutGUI();
        showDocumentation(aboutText);

    }

    public static void showGettingStarted() {
        String gettingStartedText = DocumentationGenerator.generateGettingStartedGUI();
        showDocumentation(gettingStartedText);

    }

    static void showDocumentation(String textToDisplay) {
            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            editorPane.setContentType("text/html");
            editorPane.setText(textToDisplay);
            editorPane.setCaretPosition(0);
            editorPane.setMargin(new Insets(10,10,10,10));

            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(10);
            scrollPane.getVerticalScrollBar().setValue(0);

            JFrame frame = new JFrame();
            frame.add(scrollPane);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setPreferredSize(new Dimension(700,700));
            frame.setMinimumSize(new Dimension(700,200));
            frame.pack();
            frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
            frame.setVisible(true);

    }
}
