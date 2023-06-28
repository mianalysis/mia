package io.github.mianalysis.mia.process.documentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainGenerator {
    public static String INDEX = "INDEX";
    public static String MODULES = "MODULES";
    public static String GUIDES = "GUIDES";
    public static String PUBLICATIONS = "PUBLICATIONS";
    public static String ABOUT = "ABOUT";

    public static void main(String[] args) {
        try {
            new MainGenerator().run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        // Clearing existing HTML files
        File root = new File("docs/index.html");
        // deleteFolders(root);

        root = new File("docs/guides");
        deleteFolders(root);
        root.mkdir();

        root = new File("docs/modules");
        deleteFolders(root);
        root.mkdir();

        root = new File("docs/about.html");
        deleteFolders(root);

        root = new File("docs/publications.html");
        deleteFolders(root);

        // Creating HTML files
        new IndexGenerator().generate();        
        new GuideGenerator().generate();
        new ModuleGenerator().generate();
        new PublicationsGenerator().generate();
        new AboutGenerator().generate();

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

    public static void generateReadmeMarkdown() {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/githubBadges.md"))));
            sb.append("\n\n");

            sb.append(
                    "[![Wolfson Bioimaging](./src/main/resources/images/Logo_text_UoB_128.png)](https://www.bristol.ac.uk/wolfson-bioimaging/)");
            sb.append("\n\n");

            sb.append("About MIA").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md"))));
            sb.append("\n\n");

            sb.append(
                "![](./docs/img/carousel/carousel1.png)");
            sb.append("\n\n");

            sb.append("Preprint and poster").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/poster.md"))));
            sb.append("\n\n");

            sb.append("Installation").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/installation.md"))));
            sb.append("\n\n");

            sb.append("Using MIA").append("\n").append("------------").append("\n");
            sb.append(
                    "Guides for using MIA can be found [here](https://mianalysis.github.io/mia/guides).  There are also example workflows in the [mia-examples](https://github.com/mianalysis/mia-examples) repository (with more to be added over time).\n");
            sb.append("\n\n");

            sb.append("Acknowledgements").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/acknowledgements.md"))));
            sb.append("\n\n");

            sb.append("Citing MIA").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/citing.md"))));
            sb.append("\n\n");

            sb.append("Publications").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publicationsshort.md"))));
            sb.append("\n\n");

            sb.append("Ongoing development").append("\n").append("------------").append("\n");
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
