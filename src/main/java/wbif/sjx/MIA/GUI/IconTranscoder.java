package wbif.sjx.MIA.GUI;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import wbif.sjx.MIA.MIA;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class IconTranscoder extends ImageTranscoder {
    ImageIcon icon = null;

    public IconTranscoder(String url) {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
//        TranscodingHints hints = new TranscodingHints();

        hints.put(ImageTranscoder.KEY_WIDTH, new Float(30)); // e.g. width=new Float(300)
        hints.put(ImageTranscoder.KEY_HEIGHT,new Float(30));// e.g. height=new Float(75)

        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        try {
            SVGDocument doc = factory.createSVGDocument(url);

            doc.getElementById("path815").setAttributeNS(null, "fill", Colours.BLUE_HEX);

            transcode(doc,null,null);
        } catch (IOException | TranscoderException e) {
            e.printStackTrace();
        }



//        TranscoderInput input = new TranscoderInput(url);
//        Document doc = input.getDocument();
//        MIA.log.writeDebug("DOC "+doc);
//        MIA.log.writeDebug(doc.getChildNodes().getLength());
//        doc.getElementById("path815").setAttributeNS(null, "fill", Colours.BLUE_HEX);
//
//        try {
//            transcode(input,null);
//        } catch (TranscoderException e) {
//            e.printStackTrace();
//        }
//
//        MIA.log.writeDebug("DOC "+doc);
//        MIA.log.writeDebug(doc.getChildNodes().getLength());

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

    public ImageIcon getIcon() {
        return icon;
    }
}
