package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.Process.DocumentationGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class DocumentationPanel {
    public static void showAbout() {
        try {
            String aboutText = DocumentationGenerator.generateAboutGUI();

            JFrame frame = new JFrame();

            JEditorPane textArea = new JEditorPane();
            textArea.setEditable(false);
            textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            textArea.setText(aboutText);
            textArea.setCaretPosition(0);

            JScrollPane objectsScrollPane = new JScrollPane(textArea);
            objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
            objectsScrollPane.getVerticalScrollBar().setValue(0);

            frame.add(objectsScrollPane);

            frame.pack();
            frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
