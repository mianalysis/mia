package io.github.mianalysis.mia.process.documentation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class PublicationsGenerator extends AbstractGenerator {

    @Override
    public void generate() throws IOException {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.PUBLICATIONS);

        String mainContent = getPageTemplate("src/main/resources/templatehtml/publicationstemplate.html", pathToRoot);

        String publicationsContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/templatemd/publicationsshort.md")));
        publicationsContent = renderer.render(parser.parse(publicationsContent));
        mainContent = mainContent.replace("${PUBLICATIONS}", publicationsContent);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs/html/publications.html");
        writer.write(page);
        writer.flush();
        writer.close();
        
    }    
}
