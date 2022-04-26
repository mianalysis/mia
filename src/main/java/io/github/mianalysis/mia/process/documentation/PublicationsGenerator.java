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

        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publications2022.md")));
        content = renderer.render(parser.parse(content));
        mainContent = mainContent.replace("${PUBLICATIONS_2022}", content);

        content = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publications2021.md")));
        content = renderer.render(parser.parse(content));
        mainContent = mainContent.replace("${PUBLICATIONS_2021}", content);

        content = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publications2020.md")));
        content = renderer.render(parser.parse(content));
        mainContent = mainContent.replace("${PUBLICATIONS_2020}", content);

        content = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publications2019.md")));
        content = renderer.render(parser.parse(content));
        mainContent = mainContent.replace("${PUBLICATIONS_2019}", content);

        content = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publications2018.md")));
        content = renderer.render(parser.parse(content));
        mainContent = mainContent.replace("${PUBLICATIONS_2018}", content);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs/html/publications.html");
        writer.write(page);
        writer.flush();
        writer.close();
        
    }    
}
