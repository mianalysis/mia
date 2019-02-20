package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class DocumentationGenerator {
    public static void main(String[]args){
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        File newFile = new File("docs/html/testfile.html");
//        try {
//            FileWriter writer = new FileWriter(newFile);
//            writer.write("");
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void run() throws IOException {
        // Generate module list HTML document
        String template = new String(Files.readAllBytes(Paths.get("docs/templatehtml/modulelist.html")));
        String moduleList = getModuleList();

        template = template.replace("${INSERT}",moduleList);

        System.out.println(template);

        FileWriter writer = new FileWriter("docs/html/modulelist.html");
        writer.write(template);
        writer.flush();
        writer.close();

    }

    private static String getModuleList() {
        StringBuilder sb = new StringBuilder();

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

        // Getting a list of unique package names
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

                String simplePackageName = module.getPackageName();
                simplePackageName = simplePackageName.replaceAll("[^A-Za-z0-9]","");
                simplePackageName = simplePackageName.replace(".","");

                sb.append("<li><a href=\"/html/modules/")
                        .append(simplePackageName)
                        .append(module.getClass().getSimpleName())
                        .append("\">")
                        .append(module.getTitle())
                        .append("</a></li>\r\n");

            }

            sb.append("</ul>\r\n");

        }

        return sb.toString();

    }
}