package io.github.mianalysis.mia.process.documentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.TreeMap;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.abstrakt.Ref;
import io.github.mianalysis.mia.process.ClassHunter;

public class ModuleGenerator extends AbstractGenerator {
    private TreeMap<String, Module> modules;

    public ModuleGenerator() {
        modules = getModules();
    }

    @Override
    public void generate() throws IOException {
        Category rootCategory = Categories.getRootCategory();
        generateModuleListPages(rootCategory);
        generateModulePages();

    }

    public void generateModuleListPages(Category category) throws IOException {
        // Setting the path and ensuring the folder exists
        String pathToRoot = getCatgoryPathToRoot(category) + "..";
        String categorySaveName = getSaveName(category);
        String categoryPath = getCategoryPath(category);
        String path = "docs/html/";
        new File(path + categoryPath).mkdirs();

        // Initialise HTML document
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.MODULES);

        // Populate module packages content
        String mainContent = getPageTemplate("src/main/resources/templatehtml/categorylisttemplate.html", pathToRoot);
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
            generateModuleListPages(childCategory);

    }

    public void generateModulePages() throws IOException {
        for (Module module : modules.values()) {
            Category category = module.getCategory();
            String pathToRoot = getCatgoryPathToRoot(category) + "..";
            String categoryPath = getCategoryPath(category);
            String path = "docs/html/";
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

        return appendCategoryPath(category.getParent(), pathToRoot) + " > " + categoryContent;

    }

    String getParameterSummary(Parameter parameter) {
        if (!parameter.isExported())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td class=\"mia-table-text-bold\">");
        sb.append(parameter.getName());
        sb.append("</td>");
        sb.append("<td>");
        sb.append(parameter.getDescription());
        sb.append("</td>");
        sb.append("</tr>");
        // sb.append("<p
        // class=\"mia-main-text\"><b>").append(parameter.getName()).append("</b>:
        // ").append(parameter.getDescription());

        if (parameter instanceof ParameterGroup)
            for (Parameter collectionParam : ((ParameterGroup) parameter).getTemplateParameters().values())
                sb.append(getParameterSummary(collectionParam));

        // if (!(parameter instanceof ChoiceP))
        // sb.append("<br>");

        return sb.toString();

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
        Modules tempCollection = new Modules();
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor<Module> constructor = clazz.getDeclaredConstructor(Modules.class);
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
