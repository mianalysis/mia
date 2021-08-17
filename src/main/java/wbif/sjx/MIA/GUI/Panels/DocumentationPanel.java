package wbif.sjx.MIA.GUI.Panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.HyperlinkOpener;
import wbif.sjx.MIA.Process.DocumentationGenerator;

public class DocumentationPanel {
    public static void showAbout() {
        String aboutText = DocumentationGenerator.generateAboutGUI();
        showDocumentation(aboutText);

    }

    public static void showGettingStarted() {
        String gettingStartedText = DocumentationGenerator.generateGettingStartedGUI();
        showDocumentation(gettingStartedText);

    }

    public static void showPony() {
        String pony = "<html><body><div style=\"text-align: center;\">" +
                "<img src=\""+MIA.class.getResource("/Images/Pony.gif").toString()+"\" align=\"middle\">" +
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
            editorPane.setMargin(new Insets(10,10,10,10));
            editorPane.addHyperlinkListener(new HyperlinkOpener());

            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(10);
            scrollPane.getVerticalScrollBar().setValue(0);

            JFrame frame = new JFrame();
            frame.add(scrollPane);
            frame.setIconImage(new ImageIcon(MIA.class.getResource("/Icons/Logo_wide_32.png"),"").getImage());

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setPreferredSize(new Dimension(700,700));
            frame.setMinimumSize(new Dimension(700,200));
            frame.pack();
            frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
            frame.setVisible(true);

            return frame;

    }
}
