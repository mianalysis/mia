package io.github.mianalysis.mia.process.documentation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class IndexGenerator extends AbstractGenerator {

    @Override
    public void generate() throws IOException {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        // Generate module list HTML document
        String pathToRoot = ".";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.HOME);

        String mainContent = getPageTemplate("src/main/resources/templatehtml/indextemplate.html", pathToRoot);

        String descriptionContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/templatemd/description.md")));
        descriptionContent = renderer.render(parser.parse(descriptionContent));
        mainContent = mainContent.replace("${INDEX_INTRODUCTION}", descriptionContent);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs/index.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }
}
