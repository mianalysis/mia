package io.github.mianalysis.mia.object.metadata;

import loci.common.RandomAccessInputStream;
import loci.formats.in.FlexReader;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by sc13967 on 20/07/2017.
 */
public class OperaFileExtractor implements FileExtractor {
    private static final String name = "Opera file (.flex)";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean extract(Metadata result, File file) {
        try {
            RandomAccessInputStream in = new RandomAccessInputStream(file.getAbsolutePath());

            TiffParser tiffParser = new TiffParser(in);
            IFD firstIFD = tiffParser.getIFDs().get(0);
            String xml = firstIFD.getIFDTextValue(FlexReader.FLEX);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            result.put(Metadata.AREA_NAME,doc.getElementsByTagName("AreaName").item(0).getChildNodes().item(0).getNodeValue());

        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return false;
    }
}
