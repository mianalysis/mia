package io.github.mianalysis.mia;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.imagej.updater.util.Platforms;

public class FijiCompiler {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String platform = Platforms.current();
        boolean isMac = platform.contains("mac");
        boolean isWin = platform.contains("win");
        boolean isLinux = platform.contains("linux");

        // String path = "/Users/sc13967/Applications/Fiji (release).app/db.xml";
        String path = "C:\\Users\\sc13967\\Applications\\Fiji\\db.xml\\db.xml";
        // String target = "/Users/sc13967/Desktop/Demo2/";
        String target = "C:\\Users\\sc13967\\Desktop\\Demo\\";

        new File(target).mkdirs();

        String xml = FileUtils.readFileToString(new File(path), "UTF-8");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        doc.getDocumentElement().normalize();

        // Creating a list of update sites
        HashMap<String, String> updateSites = new HashMap<>();
        NodeList nodes = doc.getChildNodes().item(1).getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("update-site") ||
                    node.getNodeName().equals("disabled-update-site")) {
                NamedNodeMap attributes = node.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String url = attributes.getNamedItem("url").getNodeValue();
                updateSites.put(name, url);
            }
        }

        // Finding all files to download
        nodes = doc.getChildNodes().item(1).getChildNodes();
        HashMap<String, Object[]> installList = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("plugin")) {
                NamedNodeMap attributes = node.getAttributes();
                String updateSite = attributes.getNamedItem("update-site").getNodeValue();
                String url = updateSites.get(updateSite);
                String filename = attributes.getNamedItem("filename").getNodeValue();
                String timestamp = "";
                boolean executable = false;
                if (attributes.getNamedItem("executable") != null)
                    executable = Boolean.parseBoolean(attributes.getNamedItem("executable").getNodeValue());

                // If there is a specific "platform" node, check if this file is for this
                // platform
                boolean wrongPlatform = false;
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    if (childNodes.item(j).getNodeName().equals("platform")) {
                        String currentPlatform = childNodes.item(j).getTextContent();

                        // With Mac, the <platform> tag doesn't distinguish between different forms of
                        // Mac, so we have to compare to the filename, Some Mac filenames don't include
                        // any platform name, so this includes a check for these
                        if (isMac && !currentPlatform.equals("macosx"))
                            wrongPlatform = true;

                        if (isWin && (!currentPlatform.equals("winx") && !currentPlatform.equals(platform)))
                            wrongPlatform = true;
                                                    
                        if (isLinux && !currentPlatform.equals(platform))
                            wrongPlatform = true;                            
                        
                        // There can only be one platform node
                        break;

                    }
                }

                if (wrongPlatform)
                    continue;

                // If there is a specific "version" node, loading this file
                for (int j = 0; j < childNodes.getLength(); j++) {
                    if (childNodes.item(j).getNodeName().equals("version")) {
                        timestamp = childNodes.item(j).getAttributes().getNamedItem("timestamp").getNodeValue();
                        break;
                    }
                }

                if (timestamp.equals(""))
                    continue;

                String finalUrl = url + filename + "-" + timestamp;
                finalUrl = finalUrl.replace(" ", "%20");
                installList.put(finalUrl, new Object[] { filename, executable });

            }
        }

        // Downloading files
        int count = 0;
        for (String url : installList.keySet()) {
            count++;
            String outputFilename = (String) installList.get(url)[0];
            boolean executable = (Boolean) installList.get(url)[1];

            if (new File(target + "Fiji/" + outputFilename).exists())
                continue;

            System.out.println("Installing " + count + " of " + installList.size() + ": " + url);

            try {
                FileUtils.copyURLToFile(new URL(url), new File(target + "Fiji/" +
                        outputFilename));
                Thread.sleep(100); // Slow it down, so not overloading the server (can cause 403 error)
            } catch (Exception e) {
                System.out.println("Can't find " + url);
                e.printStackTrace();
            }

            new File(target + "Fiji/" + outputFilename).setExecutable(executable);

        }

        System.out.println("Complete!");

    }
}
