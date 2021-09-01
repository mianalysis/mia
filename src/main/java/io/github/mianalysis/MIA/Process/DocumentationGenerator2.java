package io.github.mianalysis.MIA.Process;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.parser.Parser;

import io.github.mianalysis.MIA.MIA;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterGroup;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.MIA.Object.References.Abstract.Ref;

public class DocumentationGenerator2 {
    private Parser parser;
    private HtmlRenderer renderer;

    private String version = "";
    private TreeMap<String, Module> modules;

    private enum Page {
        HOME, GUIDES, MODULES, ABOUT;
    }

    public static void main(String[] args) {
        try {
            DocumentationGenerator2 generator = new DocumentationGenerator2();
            generator.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DocumentationGenerator2() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();

        modules = getModules();
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
        File root = new File("docs2/html");
        deleteFolders(root);
        root.mkdir();

        generateIndexPage();
        generateGuidesPages();

        // Generating module pages
        Category rootCategory = Categories.getRootCategory();
        generateCategoryListPages(rootCategory);
        generateModulePages();

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
        page = setNavbarActive(page, Page.HOME);

        String mainContent = getPageTemplate("src/main/resources/templatehtml/indextemplate.html", pathToRoot);

        String introductionContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md")));
        introductionContent = renderer.render(parser.parse(introductionContent));
        mainContent = mainContent.replace("${INDEX_INTRODUCTION}", introductionContent);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs2/index.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateGuidesPages() throws IOException {
        generateGettingStartedPage();
        
    }

    public void generateGettingStartedPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.HOME);

        String mainContent = getPageTemplate("src/main/resources/templatehtml/gettingstartedtemplate.html", pathToRoot);

        String gsContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/templatemd/installation.md")));
        gsContent = renderer.render(parser.parse(gsContent));
        mainContent = mainContent.replace("${INSTALLATION}", gsContent);

        gsContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/creatingWorkflow.md")));
        gsContent = renderer.render(parser.parse(gsContent));
        mainContent = mainContent.replace("${CREATE_NEW}", gsContent);

        gsContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/usingExistingWorkflow.md")));
        gsContent = renderer.render(parser.parse(gsContent));
        mainContent = mainContent.replace("${USE_EXISTING}", gsContent);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs2/html/gettingstarted.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateCategoryListPages(Category category) throws IOException {
        // Setting the path and ensuring the folder exists
        String pathToRoot = getCatgoryPathToRoot(category) + "..";
        String categorySaveName = getSaveName(category);
        String categoryPath = getCategoryPath(category);
        String path = "docs2/html/";
        new File(path + categoryPath).mkdirs();

        // Initialise HTML document
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.MODULES);

        // Populate module packages content
        String mainContent = getPageTemplate("src/main/resources/templatehtml/categorylisttemplate.html", pathToRoot);
        if (category.getParent() == null) // Just having a single link looks strange
            mainContent = mainContent.replace("${CATEGORY_PATH}", "");
        else
            mainContent = mainContent.replace("${CATEGORY_PATH}", appendCategoryPath(category, pathToRoot));
        mainContent = mainContent.replace("${CATEGORY_NAME}", category.getName());
        mainContent = mainContent.replace("${CATEGORY_DESCRIPTION}", category.getDescription());

        // Adding a card for each child category
        String categoryContent = "";
        for (Category childCategory : category.getChildren()) {
            String cardContent = getPageTemplate("src/main/resources/templatehtml/categorycardtemplate.html",
                    pathToRoot);
            cardContent = cardContent.replace("${CARD_TITLE}", childCategory.getName());
            cardContent = cardContent.replace("${CARD_TEXT}", childCategory.getDescription());
            cardContent = cardContent.replace("${TARGET_PATH}",
                    getCategoryPath(childCategory) + "/" + getSaveName(childCategory));
            categoryContent = categoryContent + cardContent;
        }
        mainContent = mainContent.replace("${CATEGORY_CARDS}", categoryContent);

        // Finding modules in this category and adding them to this page
        String moduleContent = "";
        for (Module module : modules.values()) {
            if (module.getCategory() == category) {
                String cardContent = getPageTemplate("src/main/resources/templatehtml/modulecardtemplate.html",
                        pathToRoot);
                cardContent = cardContent.replace("${CARD_TITLE}", module.getName());
                cardContent = cardContent.replace("${CARD_TEXT}", module.getShortDescription());
                cardContent = cardContent.replace("${TARGET_PATH}",
                        getCategoryPath(category) + "/" + getSaveName(module));
                moduleContent = moduleContent + cardContent;
            }
        }
        mainContent = mainContent.replace("${MODULE_CARDS}", moduleContent);

        // Add packages content to page
        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter(path + categoryPath + "/" + categorySaveName + ".html");
        writer.write(page);
        writer.flush();
        writer.close();

        // For each child category, repeating the same process
        for (Category childCategory : category.getChildren())
            generateCategoryListPages(childCategory);

    }

    public void generateModulePages() throws IOException {
        for (Module module : modules.values()) {
            Category category = module.getCategory();
            String pathToRoot = getCatgoryPathToRoot(category) + "..";
            String categoryPath = getCategoryPath(category);
            String path = "docs2/html/";
            String moduleSaveName = getSaveName(module);

            // Initialise HTML document
            String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
            page = setNavbarActive(page, Page.MODULES);

            // Populate module packages content
            String mainContent = getPageTemplate("src/main/resources/templatehtml/moduletemplate.html", pathToRoot);
            mainContent = mainContent.replace("${MODULE_PATH}", appendCategoryPath(module.getCategory(), pathToRoot));
            mainContent = mainContent.replace("${MODULE_NAME}", module.getName());
            mainContent = mainContent.replace("${MODULE_SHORT_DESCRIPTION}", module.getShortDescription());
            mainContent = mainContent.replace("${MODULE_FULL_DESCRIPTION}", module.getDescription());

            String parameterContent = "";
            for (Parameter parameter : module.getAllParameters().values())
                parameterContent = parameterContent + getParameterSummary(parameter);
            mainContent = mainContent.replace("${MODULE_PARAMETERS}", parameterContent);

            // Add module information to page
            page = page.replace("${MAIN_CONTENT}", mainContent);

            FileWriter writer = new FileWriter(path + categoryPath + "/" + moduleSaveName + ".html");
            writer.write(page);
            writer.flush();
            writer.close();

        }
    }

    String appendCategoryPath(Category category, String pathToRoot) {
        String categoryPath = pathToRoot + "/html" + getCategoryPath(category) + "/" + getSaveName(category) + ".html";
        String categoryContent = "<a href=\"" + categoryPath + "\">" + category.getName() + "</a>";

        if (category.getParent() == null)
            return categoryContent;

        return appendCategoryPath(category.getParent(), pathToRoot) + " ➤ " + categoryContent;

    }

    String getParameterSummary(Parameter parameter) {
        if (!parameter.isExported())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(parameter.getName()).append("</b> (default = \"").append(parameter.getRawStringValue())
                .append("\") ").append(parameter.getDescription()).append("<br>");

        if (parameter instanceof ParameterGroup)
            for (Parameter collectionParam : ((ParameterGroup) parameter).getTemplateParameters().values())
                sb.append(getParameterSummary(collectionParam));

        if (!(parameter instanceof ChoiceP))
            sb.append("<br>");

        return sb.toString();

    }

    public void generateAboutPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.HOME);

        String mainContent = getPageTemplate("src/main/resources/templatehtml/abouttemplate.html", pathToRoot);

        String aboutContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_INTRODUCTION}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/acknowledgements.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_ACKNOWLEDGEMENTS}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/citing.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_CITING}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/note.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_DEVELOPMENT}", aboutContent);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs2/html/about.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    String setNavbarActive(String content, Page page) {
        content = content.replace("${ACTIVE_HOME}", page == Page.HOME ? "active" : "");
        content = content.replace("${ACTIVE_GUIDES}", page == Page.GUIDES ? "active" : "");
        content = content.replace("${ACTIVE_MODULES}", page == Page.MODULES ? "active" : "");
        content = content.replace("${ACTIVE_ABOUT}", page == Page.ABOUT ? "active" : "");

        return content;

    }

    public static void generateReadmeMarkdown() {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/githubBadges.md"))));
            sb.append("\n\n");

            sb.append(
                    "[![Wolfson Bioimaging](./src/main/resources/Images/Logo_text_UoB_128.png)](http://www.bristol.ac.uk/wolfson-bioimaging/)");
            sb.append("\n\n");

            sb.append("About MIA").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md"))));
            sb.append("\n\n");

            sb.append("Installation").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/installation.md"))));
            sb.append("\n\n");

            sb.append("Create new workflow").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/creatingWorkflow.md"))));
            sb.append("\n\n");

            sb.append("Use existing workflow").append("\n").append("------------").append("\n");
            sb.append(new String(
                    Files.readAllBytes(Paths.get("src/main/resources/templatemd/usingExistingWorkflow.md"))));
            sb.append("\n\n");

            sb.append("Acknowledgements").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/acknowledgements.md"))));
            sb.append("\n\n");

            sb.append("Citing MIA").append("\n").append("------------").append("\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/citing.md"))));
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

    static String insertPathToRoot(String str, String pathToRoot) {
        return str.replace("${PTR}", pathToRoot);
    }

    String insertMIAVersion(String str) {
        return str.replace("${MIA_VERSION}", version);
    }

    String getCategoryPath(Category category) {
        if (category == null)
            return "";

        return (getCategoryPath(category.getParent()) + "/" + getSaveName(category));

    }

    String getCatgoryPathToRoot(Category category) {
        if (category == null)
            return "";

        return getCatgoryPathToRoot(category.getParent()) + "../";
    }

    String getSaveName(Ref ref) {
        return ref.getName().toLowerCase().replace(" ", "").replace("/", "");
    }

    private static TreeMap<String, Module> getModules() {
        // Get a list of Modules
        List<String> classNames = ClassHunter.getModules(false);

        // Converting the list of classes to a list of Modules
        TreeMap<String, Module> modules = new TreeMap<>();
        ModuleCollection tempCollection = new ModuleCollection();
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                Module module = (Module) constructor.newInstance(tempCollection);
                modules.put(module.getName(), module);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                MIA.log.writeError(e);
            }
        }

        return modules;

    }

}
