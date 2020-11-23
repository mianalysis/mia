package wbif.sjx.MIA.Process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DocumentationGenerator2 {
    public static void main(String[] args) {

        try {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFolders(File root) {
        if (root.isFile()) {
            root.delete();

        } else if (root.isDirectory()) {
            for (File file : root.listFiles())
                deleteFolders(file);

            root.delete();
        }
    }

    private static String getPageTemplate(String pathToTemplate, String pathToRoot) {
        try {
            String page = new String(Files.readAllBytes(Paths.get(pathToTemplate)));
            return page.replace("${PTR}", pathToRoot);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void generateIndexPage() throws IOException {
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

    public static void generateGettingStartedPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "GETTING STARTED");

        FileWriter writer = new FileWriter("docs/html/gettingstarted.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public static void generateGuidesPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "GUIDES");

        FileWriter writer = new FileWriter("docs/html/guides.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public static void generateModulesPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "MODULES");

        FileWriter writer = new FileWriter("docs/html/modules.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public static void generateAboutPage() throws IOException {
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
}
