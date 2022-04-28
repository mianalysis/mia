package io.github.mianalysis.mia.process.documentation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import io.github.mianalysis.mia.MIA;

public class DocumentationGenerator {
    public static final String ABOUT_PATH = "docs/gui/about.html";
    public static final String GETTING_STARTED_PATH = "docs/gui/gettingstarted.html";
    public static void main(String[] args) throws IOException {
        // Creating "About" file
        String aboutText = generateAboutGUI();
        File file = new File(ABOUT_PATH);
        file.getParentFile().mkdirs();
        Files.write(file.toPath(),aboutText.getBytes(StandardCharsets.UTF_8));
 
        // Creating "Getting Started" file
        String gettingStartedText = generateGettingStartedGUI();
        file = new File(GETTING_STARTED_PATH);
        Files.write(file.toPath(),gettingStartedText.getBytes(StandardCharsets.UTF_8));
 
    }

    public static String generateAboutGUI() {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        StringBuilder sb = new StringBuilder();

        // The following is required to get the version number and release date from the
        // pom.xml
        String version = "";
        // try {
        // FileReader reader = new FileReader("pom.xml");
        // Model model = new MavenXpp3Reader().read(reader);
        // reader.close();
        // version = new MavenProject(model).getVersion();
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
