package wbif.sjx.MIA.Process;

import wbif.sjx.MIA.Macro.MacroHandler;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DocumentationGenerator {
    public static void main(String[]args){
        try {
            generateModuleList();
            generateModulePages();
            generateMacroList();
            generateMacroPages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateModuleList() throws IOException {
        // Generate module list HTML document
        String template = new String(Files.readAllBytes(Paths.get("docs/templatehtml/modulelist.html")));
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

            FileWriter writer = new FileWriter("docs/html/"+getSimpleModulePath(module));
            writer.write(template);
            writer.flush();
            writer.close();

        }
    }

    private static void generateMacroList() throws IOException {
        // Generate module list HTML document
        String template = new String(Files.readAllBytes(Paths.get("docs/templatehtml/macrolist.html")));
        String macroList = getMacroList();
        template = template.replace("${INSERT}",macroList);

        new File("docs/html/").mkdirs();
        FileWriter writer = new FileWriter("docs/html/macrolist.html");
        writer.write(template);
        writer.flush();
        writer.close();

    }

    private static void generateMacroPages() throws IOException {
        ArrayList<MacroOperation> macros = MacroHandler.getMacroOperations();
        for (MacroOperation macro:macros) {
            String template = new String(Files.readAllBytes(Paths.get("docs/templatehtml/macros.html")));
            String macroList = getMacroSummary(macro);
            template = template.replace("${INSERT}",macroList);

            new File("docs/html/macros/").mkdirs();

            FileWriter writer = new FileWriter("docs/html/"+getSimpleMacroPath(macro));
            writer.write(template);
            writer.flush();
            writer.close();

        }
    }

    private static String getModuleSummary(Module module) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Adding a return to Module list button
        sb.append("<a href=\"../modulelist.html\">Back to module list</a>\r\n");

        // Adding the Module title
        sb.append("<h1>")
                .append(module.getTitle())
                .append("</h1>\r\n");

        // Adding the Module summary
        String helpText = module.getHelp();
        helpText = helpText == null ? "" : helpText;
        sb.append("<h2>Description</h2>\r\n")
                .append(helpText)
                .append("\r\n");

        sb.append("<h2>Parameters</h2>\r\n<ul>");
        for (Parameter parameter:module.getAllParameters()) {
            sb.append("<li>")
                    .append(parameter.getName())
                    .append("<ul>");

            if (!parameter.getDescription().equals("")) {
                sb.append("<li>Description: ")
                        .append(parameter.getDescription())
                        .append("</li>");
            }

            sb.append("<li>Type: ")
                    .append(parameter.getClass().getSimpleName())
                    .append("</li>");

            if (!parameter.getValueAsString().equals("")) {
                sb.append("<li>Default value: ")
                        .append(parameter.getValueAsString())
                        .append("</li>");
            }

            if (parameter instanceof ChoiceP) {
                String[] choices = ((ChoiceP) parameter).getChoices();
                sb.append("<li>Choices: ")
                        .append("<ul>");

                for (String choice:choices) {
                    sb.append("<li>")
                            .append(choice)
                            .append("</li>");
                }
                sb.append("</ul></li>");
            }

            sb.append("</ul>");

        }
        sb.append("</ul>");

        // Generate module list HTML document
        return sb.toString();

    }

    private static String getMacroSummary(MacroOperation macro) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Adding a return to Module list button
        sb.append("<a href=\"../macrolist.html\">Back to macro list</a>\r\n");

        // Adding the MacroOperation title
        sb.append("<h1>")
                .append(macro.getName())
                .append("</h1>\r\n");

        // Adding the Module summary
        String helpText = macro.getDescription();
        helpText = helpText == null ? "" : helpText;
        sb.append("<h2>Description</h2>\r\n")
                .append(helpText)
                .append("\r\n");

        sb.append("<h2>Parameters</h2>\r\n");

        String parameterList = macro.getArgumentsDescription();
        String[] parameters = parameterList.split(",");
        if (parameters.length > 0) sb.append("<ul");
        
        for (String parameter:parameters) {
            sb.append("<li>")
                    .append(parameter)
                    .append("</li>");

        }
        sb.append("</ul>");

        // Generate module list HTML document
        return sb.toString();

    }

    private static String getModuleList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Modules</h1>");

        // Getting a list of unique package names
        LinkedHashSet<Module> modules = getModules();
        TreeSet<String> packageNames = new TreeSet<>();

        for (Module module:modules) {
            packageNames.add(module.getPackageName());
        }

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

                sb.append("<li><a href=\".")
                        .append(getSimpleModulePath(module))
                        .append("\">")
                        .append(module.getTitle())
                        .append("</a></li>\r\n");

            }

            sb.append("</ul>\r\n");

        }

        return sb.toString();

    }

    private static String getMacroList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Macros</h1>");

        // Getting a list of unique package names
        ArrayList<MacroOperation> macros = MacroHandler.getMacroOperations();

        // For each package name, adding a list of the matching Modules
        for (MacroOperation macro:macros) {
            sb.append("<li><a href=\".")
                    .append(getSimpleMacroPath(macro))
                    .append("\">")
                    .append(macro.getName())
                    .append("</a></li>\r\n");
        }

        sb.append("</ul>\r\n");

        return sb.toString();

    }

    private static LinkedHashSet<Module> getModules() {
        // Get a list of Modules
        Set<Class<? extends Module>> clazzes = new ClassHunter<Module>().getClasses(Module.class, false);

        // Converting the list of classes to a list of Modules
        LinkedHashSet<Module> modules = new LinkedHashSet<>();
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

        sb.append("/modules/")
                .append(simplePackageName)
                .append(module.getClass().getSimpleName())
                .append(".html");

        return sb.toString();

    }

    private static String getSimpleMacroPath(MacroOperation macro) {
        StringBuilder sb = new StringBuilder();

        String simpleName = macro.getName();
        simpleName = simpleName.replaceAll("[^A-Za-z0-9]","");
        simpleName = simpleName.replace(".","");

        sb.append("/macros/")
                .append(simpleName)
                .append(".html");

        return sb.toString();

    }
}