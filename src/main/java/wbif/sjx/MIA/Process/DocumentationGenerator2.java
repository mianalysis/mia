package wbif.sjx.MIA.Process;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;

public class DocumentationGenerator2 {
    private String version = "";

    public static void main(String[] args) {
        try {   
            DocumentationGenerator2 generator = new DocumentationGenerator2();
            generator.run();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DocumentationGenerator2() {
        try {
            FileReader reader = new FileReader("pom.xml");
            Model model = new MavenXpp3Reader().read(reader);
            reader.close();
            version = new MavenProject(model).getVersion();
        } catch (XmlPullParserException | IOException | org.codehaus.plexus.util.xml.pull.XmlPullParserException e) {
            version = getClass().getPackage().getImplementationVersion();
        }
    }

    public void run() throws IOException {
        // Clearing existing HTML files
        File root = new File("docs/html");
        deleteFolders(root);
        root.mkdir();

        // Creating website content
        generateIndexPage();
        generateGettingStartedPage();
        generateGuidesPage();
        generateModulesPage();
        generateAboutPage();

        // Creating README.md
        generateReadmeMarkdown();
    }

    private void deleteFolders(File root) {
        if (root.isFile()) {
            root.delete();

        } else if (root.isDirectory()) {
            for (File file : root.listFiles())
                deleteFolders(file);

            root.delete();
        }
    }

    private String getPageTemplate(String pathToTemplate, String pathToRoot) {
        try {
            String page = new String(Files.readAllBytes(Paths.get(pathToTemplate)));
            page = insertPathToRoot(page, pathToRoot);
            page = insertMIAVersion(page);
            return page;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void generateIndexPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = ".";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        String mainContent = getPageTemplate("src/main/resources/templatehtml/indextemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs/index.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateGettingStartedPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "GETTING STARTED");

        FileWriter writer = new FileWriter("docs/html/gettingstarted.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateGuidesPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "GUIDES");

        FileWriter writer = new FileWriter("docs/html/guides.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateModulesPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "MODULES");

        FileWriter writer = new FileWriter("docs/html/modules.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateAboutPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "ABOUT");

        FileWriter writer = new FileWriter("docs/html/about.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public static void generateReadmeMarkdown() {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/githubBadges.md"))));
            sb.append("\n\n");

            sb.append(
                    "[![Wolfson Bioimaging](./src/main/resources/Images/Logo_text_UoB_128.png)](http://www.bristol.ac.uk/wolfson-bioimaging/)");
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/installation.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/creatingWorkflow.md"))));
            sb.append("\n\n");
            sb.append(new String(
                    Files.readAllBytes(Paths.get("src/main/resources/templatemd/usingExistingWorkflow.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/acknowledgements.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/citing.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/note.md"))));
            sb.append("\n\n");

            FileWriter writer = new FileWriter("README.md");
            writer.write(sb.toString());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String insertPathToRoot(String str, String pathToRoot) {
        return str.replace("${PTR}", pathToRoot);
    }

    String insertMIAVersion(String str) {
        return str.replace("${MIA_VERSION}", version);
    }
}
