package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.Process.DocumentationGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class DocumentationPanel {
    public static void showAbout() {
        try {
            String aboutText = DocumentationGenerator.generateAboutGUI();
            aboutText = "<html><body>"+aboutText+"</body></html>";

            JFrame frame = new JFrame();

            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            editorPane.setContentType("text/html");
            editorPane.setText(aboutText);
            editorPane.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(10);
            scrollPane.getVerticalScrollBar().setValue(0);

            frame.add(scrollPane);
            frame.setPreferredSize(new Dimension(500,500));

            frame.pack();
            frame.setVisible(true);

            System.err.println(aboutText);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
