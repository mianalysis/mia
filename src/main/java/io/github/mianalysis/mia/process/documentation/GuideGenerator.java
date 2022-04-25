package io.github.mianalysis.mia.process.documentation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GuideGenerator extends AbstractGenerator {
    private boolean verbose = true;
    private SitePaths sitePaths = new SitePaths();

    @Override
    public void generate() throws IOException {
        String path = GuideGenerator.class.getResource("/guides").getPath();
        path = path.replace("%20", " ");
        File guideRoot = new File(path);
        
        generateGuideListPages(guideRoot);

    }

    public void generateGuideListPages(File file) throws IOException {
        // Setting the path and ensuring the folder exists
        String pathToRoot = getPathToRoot(file) + "..";
        String categorySaveName = getSaveName(file);
        String categoryPath = getPath(file);
        String path = "docs/html/";
        new File(path + categoryPath).mkdirs();

        // Initialise HTML document
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.GUIDES);

        // Getting file metadata
        File metaFile = new File(file.getPath() + "/_" + file.getName() + ".html");
        HashMap<String, String> metadata = getMetadata(metaFile);
        String title = metadata.get("title");
        title = title == null ? "" : title;
        String description = metadata.get("description");
        description = description == null ? "" : description;

        // Populate module packages content
        String mainContent = getPageTemplate("src/main/resources/templatehtml/categorylisttemplate.html", pathToRoot);
        mainContent = mainContent.replace("${CATEGORY_NAME}", title);
        mainContent = mainContent.replace("${CATEGORY_DESCRIPTION}", description);

        // For now, removing the guide path section since all guides are on a single page
        // mainContent = mainContent.replace("${CATEGORY_PATH}", appendPath(file, pathToRoot));
        mainContent = mainContent.replace("<p class=\"mia-main-text\">${CATEGORY_PATH}</p>", "");
        
        // Adding a card for each child category
        String categoryContent = "";
        for (File childCategory : file.listFiles()) {
            if (childCategory.isFile())
                continue;

            File childMetaFile = new File(childCategory.getPath() + "/_" + childCategory.getName() + ".html");
            HashMap<String, String> childMetadata = getMetadata(childMetaFile);
            String cardContent = getPageTemplate("src/main/resources/templatehtml/categorycardtemplate.html",
                    pathToRoot);
            cardContent = cardContent.replace("${CARD_TITLE}", childMetadata.get("title"));
            cardContent = cardContent.replace("${CARD_TEXT}", childMetadata.get("description"));
            cardContent = cardContent.replace("${CARD_BUTTON}", "See guides");
            cardContent = cardContent.replace("${TARGET_PATH}",
                    getPath(childCategory) + "/" + getSaveName(childCategory));
            categoryContent = categoryContent + cardContent;
        }
        mainContent = mainContent.replace("${CATEGORY_CARDS}", categoryContent);

        // Finding guides in this category and adding them to this page
        String guideContent = "";
        for (File guide : file.listFiles()) {            
            // We won't want to process categories or the metadata files for categories
            if (guide.isDirectory() || guide.getName().subSequence(0,1).equals("_"))
                continue;

            if (verbose)
                System.out.println("Generating list page \"" + guide.getName() + "\"");

            HashMap<String, String> guideMetadata = getMetadata(guide);
            String guideTitle = guideMetadata.get("title");
            guideTitle = guideTitle == null ? "" : guideTitle;
            String guideDescription = guideMetadata.get("description");
            guideDescription = guideDescription == null ? "" : guideDescription;

            String cardContent = getPageTemplate("src/main/resources/templatehtml/modulecardtemplate.html", pathToRoot);

            cardContent = cardContent.replace("${CARD_TITLE}", guideTitle);
            cardContent = cardContent.replace("${CARD_TEXT}", guideDescription);
            cardContent = cardContent.replace("${TARGET_PATH}", getPath(file) + "/" + getSaveName(guide));
            guideContent = guideContent + cardContent;

            generateGuidePage(guide);

        }

        mainContent = mainContent.replace("${MODULE_CARDS}", guideContent);

        // Add packages content to page
        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter(path + categoryPath + "/" + categorySaveName + ".html");
        writer.write(page);
        writer.flush();
        writer.close();

        // For each child guide category, repeating the same process
        for (File childCategory : file.listFiles())
            if (childCategory.isDirectory())
                generateGuideListPages(childCategory);

    }

    public void generateGuidePage(File guide) throws IOException {
        if (verbose)
            System.out.println("Generating guide page \"" + guide.getName() + "\"");
        String pathToRoot = getPathToRoot(guide.getParentFile()) + "..";
        String guidePath = getPath(guide);
        String path = "docs/html/";

        // Initialise HTML document
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.GUIDES);

        // Populate module packages content
        String html = FileUtils.readFileToString(guide, "UTF-8");
        String guideContent = html.substring(html.indexOf("<body>") + 6, html.indexOf("</body>"));

        HashMap<String, String> guideMetadata = getMetadata(guide);
        String guideTitle = guideMetadata.get("title");
        guideTitle = guideTitle == null ? "" : guideTitle;
        String guideDescription = guideMetadata.get("description");
        guideDescription = guideDescription == null ? "" : guideDescription;

        String mainContent = getPageTemplate("src/main/resources/templatehtml/guidetemplate.html", pathToRoot);
        mainContent = mainContent.replace("${PATH_PATH}", appendPath(guide.getParentFile(), pathToRoot));
        mainContent = mainContent.replace("${PATH_NAME}", guideTitle);
        mainContent = mainContent.replace("${PATH_DESCRIPTION}", guideDescription);
        mainContent = mainContent.replace("${PATH_CONTENT}", guideContent);

        // Add module information to page
        page = page.replace("${MAIN_CONTENT}", mainContent);
        page = sitePaths.replacePaths(page);
        page = insertPathToRoot(page, pathToRoot);

        FileWriter writer = new FileWriter(path + guidePath + ".html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    HashMap<String, String> getMetadata(File metaFile) {
        String html;
        try {
            html = FileUtils.readFileToString(metaFile, "UTF-8");
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

        if (html.startsWith("\uFEFF"))
            html = html.substring(1);

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(html.getBytes("UTF-8"))));
            doc.getDocumentElement().normalize();
            NodeList metaNodes = doc.getChildNodes().item(0).getChildNodes().item(1).getChildNodes();

            HashMap<String, String> metadata = new HashMap<>();
            for (int i = 0; i < metaNodes.getLength(); i++) {
                NamedNodeMap attributes = metaNodes.item(i).getAttributes();
                if (attributes == null)
                    continue;

                String name = attributes.getNamedItem("name").getNodeValue();
                String content = attributes.getNamedItem("content").getNodeValue();
                metadata.put(name, content);

            }

            return metadata;

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

    String getPath(File file) {
        if (file.getName().equals("guides"))
            return "/guides";

        return (getPath(file.getParentFile()) + "/" + getSaveName(file));

    }

    String getPathToRoot(File file) {
        if (file.getName().equals("guides"))
            return "../";

        return getPathToRoot(file.getParentFile()) + "../";

    }

    String getSaveName(File file) {
        return file.getName().toLowerCase().replace(" ", "").replace(".html", "");
    }

    String appendPath(File file, String pathToRoot) {
        File metaFile = new File(file.getPath() + "/_" + file.getName() + ".html");
        HashMap<String, String> metadata = getMetadata(metaFile);
        String guidePath = pathToRoot + "/html" + getPath(file) + "/" + getSaveName(file) + ".html";
        String categoryContent = "<a href=\"" + guidePath + "\">" + metadata.get("title") + "</a>";

        if (file.getName().equals("guides"))
            return categoryContent;

        return appendPath(file.getParentFile(), pathToRoot) + " > " + categoryContent;

    }
}
