package wbif.sjx.MIA.GUI.Icons;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGRenderingHints;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.MIA;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;

public class IconTranscoder extends ImageTranscoder {
    private ImageIcon icon;
    private String url;
    HashSet<TranscodeOperation> operations = new HashSet<>();


    public IconTranscoder(String url, int width, int height) {
        this.url = url;

        hints.put(ImageTranscoder.KEY_WIDTH, (float) width);
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) height);

        // We have to set rendering via CSS
        try {
            String css = "svg {shape-rendering: geometricPrecision;text-rendering:  geometricPrecision;" +
                    "color-rendering: optimizeQuality;image-rendering: optimizeQuality;}";
            File cssFile = File.createTempFile("batik-default-override-", ".css");
            FileUtils.writeStringToFile(cssFile, css, "UTF-8");
            hints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ImageIcon transcode() {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());

        try {
            // Getting image document
            SVGDocument doc = factory.createSVGDocument(url);

            // Applying all operations to the icon
            for (TranscodeOperation operation:operations) operation.applyOperation(doc);

            // Converting to an image
            transcode(doc,null,null);

            return icon;

        } catch (IOException | TranscoderException e) {
            MIA.log.writeError(e.getMessage());
        }

        return null;

    }

    public void addTranscodeOperation(TranscodeOperation operation) {
        operations.add(operation);
    }

    @Override
    public BufferedImage createImage(int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        icon = new ImageIcon(bufferedImage);
        return bufferedImage;

    }

    @Override
    public void writeImage(BufferedImage bufferedImage, TranscoderOutput transcoderOutput) throws TranscoderException {
    }
}
