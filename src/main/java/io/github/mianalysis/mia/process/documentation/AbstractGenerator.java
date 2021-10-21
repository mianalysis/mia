package io.github.mianalysis.mia.process.documentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractGenerator {
    protected enum Page {
        HOME, GUIDES, MODULES, ABOUT;
    }

    public abstract void generate() throws IOException;

    protected String getPageTemplate(String pathToTemplate, String pathToRoot) {
        try {
            String page = new String(Files.readAllBytes(Paths.get(pathToTemplate)));
            page = insertPathToRoot(page, pathToRoot);
            return page;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String setNavbarActive(String content, Page page) {
        content = content.replace("${ACTIVE_HOME}", page == Page.HOME ? "active" : "");
        content = content.replace("${ACTIVE_GUIDES}", page == Page.GUIDES ? "active" : "");
        content = content.replace("${ACTIVE_MODULES}", page == Page.MODULES ? "active" : "");
        content = content.replace("${ACTIVE_ABOUT}", page == Page.ABOUT ? "active" : "");

        return content;

    }

    protected static String insertPathToRoot(String content, String pathToRoot) {
        return content.replace("${PTR}", pathToRoot);
    }

    protected String insertMIAVersion(String content, String version) {
        return content.replace("${MIA_VERSION}", version);
    }
}
