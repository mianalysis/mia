package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ChoiceP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class DocumentationGenerator {
    public static void main(String[]args){
        try {
            generateModuleList();
            generateModulePages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateModuleList() throws IOException {
        // Generate module list HTML document
        String template = new String(Files.readAllBytes(Paths.get("docs/templatehtml/modules.html")));
        String moduleList = getModuleList();
        template = template.replace("${INSERT}",moduleList);

        new File("docs/html/").mkdirs();
        FileWriter writer = new FileWriter("docs/html/modulelist.html");
        writer.write(template);
        writer.flush();
        writer.close();

    }

    private static void generateModulePages() throws IOException {
        HashSet<Module> modules = getModules();
        for (Module module:modules) {
            String template = new String(Files.readAllBytes(Paths.get("docs/templatehtml/modules.html")));
            String moduleList = getModuleSummary(module);
            template = template.replace("${INSERT}",moduleList);

            new File("docs/html/modules/").mkdirs();

            FileWriter writer = new FileWriter("docs/"+getSimpleModulePath(module));
            writer.write(template);
            writer.flush();
            writer.close();

        }
    }

    private static String getModuleSummary(Module module) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Adding a return to Module list button
        sb.append("<a href=\"/html/modulelist.html\">Back to module list</a>\r\n");

        // Adding the Module title
        sb.append("<h1>")
                .append(module.getTitle())
                .append("</h1>\r\n");

        // Adding the Module summary
        sb.append("<h2>Description</h2>\r\n")
                .append(module.getNotes())
                .append("\r\n");

        sb.append("<h2>Parameters</h2>\r\n<ul>");
        for (Parameter parameter:module.getAllParameters()) {
            sb.append("<li>")
                    .append(parameter.getName())
                    .append("<ul><li>Description: ")
                    .append(parameter.getDescription())
                    .append("</li><li>Type: ")
                    .append(parameter.getClass().getSimpleName())
                    .append("</li><li>Default value: ")
                    .append(parameter.getValueAsString());

                    if (parameter instanceof ChoiceP) {
                        String[] choices = ((ChoiceP) parameter).getChoices();
                        sb.append("</li><li>Choices: ")
                                .append("<ul>");

                        for (String choice:choices) {
                            sb.append("<li>")
                            .append(choice)
                            .append("</li>");
                        }
                        sb.append("</ul>");
                    }

                    sb.append("</li></ul>");

        }
        sb.append("</ul>");

        // Generate module list HTML document
        return sb.toString();

    }

    private static String getModuleList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Modules</h1>");

        // Getting a list of unique package names
        HashSet<Module> modules = getModules();
        TreeSet<String> packageNames = new TreeSet<>();
        for (Module module:modules) packageNames.add(module.getPackageName());

        // For each package name, adding a list of the matching Modules
        for (String packageName:packageNames) {
            String prettyPackageName = packageName.replace("\\"," / ");
            sb.append("<h2>")
                    .append(prettyPackageName)
                    .append("</h2>\r\n")
                    .append("<ul>\r\n");

            // For each Module in this package, create a link to the description document
            for (Module module:modules) {
                if (!module.getPackageName().equals(packageName)) continue;

                sb.append("<li><a href=\"")
                        .append(getSimpleModulePath(module))
                        .append("\">")
                        .append(module.getTitle())
                        .append("</a></li>\r\n");

            }

            sb.append("</ul>\r\n");

        }

        return sb.toString();

    }

    private static HashSet<Module> getModules() {
        // Get a list of Modules
        Set<Class<? extends Module>> clazzes = new ClassHunter<Module>().getClasses(Module.class, false);

        // Converting the list of classes to a list of Modules
        HashSet<Module> modules = new HashSet<>();
        for (Class<? extends Module> clazz:clazzes) {
            try {
                modules.add((Module) clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return modules;

    }

    private static String getSimpleModulePath(Module module) {
        StringBuilder sb = new StringBuilder();

        String simplePackageName = module.getPackageName();
        simplePackageName = simplePackageName.replaceAll("[^A-Za-z0-9]","");
        simplePackageName = simplePackageName.replace(".","");

        sb.append("/html/modules/")
                .append(simplePackageName)
                .append(module.getClass().getSimpleName())
                .append(".html");

        return sb.toString();

    }
}