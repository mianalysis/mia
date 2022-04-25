package io.github.mianalysis.mia.process.documentation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class AboutGenerator extends AbstractGenerator {
    private SitePaths sitePaths = new SitePaths();

    @Override
    public void generate() throws IOException {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        page = setNavbarActive(page, Page.ABOUT);

        String mainContent = getPageTemplate("src/main/resources/templatehtml/abouttemplate.html", pathToRoot);

        String aboutContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_INTRODUCTION}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/description.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_DESCRIPTION}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/acknowledgements.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_ACKNOWLEDGEMENTS}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/citing.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_CITING}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/publicationsshort.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_PUBLICATIONS}", aboutContent);

        aboutContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/note.md")));
        aboutContent = renderer.render(parser.parse(aboutContent));
        mainContent = mainContent.replace("${ABOUT_DEVELOPMENT}", aboutContent);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs/html/about.html");
        writer.write(page);
        writer.flush();
        writer.close();
        
    }    
}
