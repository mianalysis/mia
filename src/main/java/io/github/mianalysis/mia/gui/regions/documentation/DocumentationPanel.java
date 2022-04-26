package io.github.mianalysis.mia.gui.regions.documentation;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.HyperlinkOpener;
import io.github.mianalysis.mia.process.documentation.DocumentationGenerator;

public class DocumentationPanel {
    public static void showAbout() {
        try {
            String aboutText = new String(Files.readAllBytes(Paths.get(DocumentationGenerator.ABOUT_PATH)));
            showDocumentation(aboutText);
        } catch (IOException e) {
            MIA.log.writeError(e);
        }
    }

    public static void showGettingStarted() {
        try {
            String gettingStartedText = new String(Files.readAllBytes(Paths.get(DocumentationGenerator.GETTING_STARTED_PATH)));
            showDocumentation(gettingStartedText);
        } catch (IOException e) {
            MIA.log.writeError(e);
        }
    }

    public static void showPony() {
        String pony = "<html><body><div style=\"text-align: center;\">" +
                "<img src=\""+MIA.class.getResource("/images/Pony.gif").toString()+"\" align=\"middle\">" +
                "</div></body></html>";

        JFrame frame = showDocumentation(pony);
        frame.setPreferredSize(new Dimension(400,465));
        frame.setMinimumSize(new Dimension(400,465));
        ((JScrollPane) frame.getContentPane().getComponent(0)).setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        frame.pack();

    }

    static JFrame showDocumentation(String textToDisplay) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        editorPane.setContentType("text/html");
        editorPane.setText(textToDisplay);
        editorPane.setCaretPosition(0);
        editorPane.setMargin(new Insets(10, 10, 10, 10));
        editorPane.addHyperlinkListener(new HyperlinkOpener());

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        JFrame frame = new JFrame();
        frame.add(scrollPane);
        frame.setIconImage(new ImageIcon(MIA.class.getResource("/icons/Logo_wide_32.png"), "").getImage());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setPreferredSize(new Dimension(700, 700));
        frame.setMinimumSize(new Dimension(700, 200));
        frame.pack();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

        return frame;

    }
}
