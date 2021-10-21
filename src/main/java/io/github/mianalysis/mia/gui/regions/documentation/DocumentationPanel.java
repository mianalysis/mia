package io.github.mianalysis.mia.gui.regions.documentation;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.HyperlinkOpener;

public class DocumentationPanel {
    public static void showAbout() {
        String aboutText = generateAboutGUI();
        showDocumentation(aboutText);

    }

    public static void showGettingStarted() {
        String gettingStartedText = generateGettingStartedGUI();
        showDocumentation(gettingStartedText);

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
    
    public static String generateAboutGUI() {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        StringBuilder sb = new StringBuilder();

        // The following is required to get the version number and release date from the
        // pom.xml
        String version = "";
        // try {
        //     FileReader reader = new FileReader("pom.xml");
        //     Model model = new MavenXpp3Reader().read(reader);
        //     reader.close();
        //     version = new MavenProject(model).getVersion();
        // } catch (XmlPullParserException | IOException e) {
            version = MIA.class.getPackage().getImplementationVersion();
        // }

        try {
            sb.append("<html><body><div align=\"justify\">");

            sb.append("<img src=\"");
            sb.append(MIA.class.getResource("/images/Logo_text_UoB_64.png").toString());
            sb.append("\" align=\"middle\">");
            sb.append("<br><br>");

            URL url = Resources.getResource("templatemd/introduction.md");
            String string = Resources.toString(url, Charsets.UTF_8);
            if (version != null)
                string = string.replace("${version}", version);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            url = Resources.getResource("templatemd/acknowledgements.md");
            string = Resources.toString(url, Charsets.UTF_8);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            url = Resources.getResource("templatemd/citing.md");
            string = Resources.toString(url, Charsets.UTF_8);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            url = Resources.getResource("templatemd/note.md");
            string = Resources.toString(url, Charsets.UTF_8);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            sb.append("</div></body></html>");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }

    public static String generateGettingStartedGUI() {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("<html><body><div align=\"justify\">");

            sb.append("<img src=\"");
            sb.append(MIA.class.getResource("/images/Logo_text_UoB_64.png").toString());
            sb.append("\" align=\"middle\">");
            sb.append("<br><br>");

            URL url = Resources.getResource("templatemd/installation.md");
            String string = Resources.toString(url, Charsets.UTF_8);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            url = Resources.getResource("templatemd/creatingWorkflow.md");
            string = Resources.toString(url, Charsets.UTF_8);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            url = Resources.getResource("templatemd/usingExistingWorkflow.md");
            string = Resources.toString(url, Charsets.UTF_8);
            sb.append(renderer.render(parser.parse(string)));
            sb.append("<br><br>");

            sb.append("</div></body></html>");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }
}
