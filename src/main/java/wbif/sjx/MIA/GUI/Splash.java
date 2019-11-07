package wbif.sjx.MIA.GUI;

import ij.IJ;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wbif.sjx.MIA.MIA;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Splash extends JFrame {
    private int height = 220;
    private int width = 250;

    JSVGCanvas canvasLogo = new JSVGCanvas();
    JSVGCanvas canvasTextDetectingModules = new JSVGCanvas();
    JSVGCanvas canvasTextInitialisingModules = new JSVGCanvas();
    JSVGCanvas canvasTextCreatingInterface = new JSVGCanvas();

    public enum Status {
        DETECTING_MODULES, INITIALISING_MODULES, CREATING_INTERFACE;
    }

    public Splash() {
        // Determine special date
        String suffix = getSpecialSuffix();
        setLayout(new BorderLayout());
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(width,height));
        setMinimumSize(new Dimension(width,height));
        setMaximumSize(new Dimension(width,height));
        setResizable(false);

        canvasLogo.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        canvasLogo.setOpaque(false);
        canvasLogo.setBackground(new Color(0,0,0,0));
        canvasLogo.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
            @Override
            public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                Document doc = e.getSVGDocument();
                doc.getElementById("letterM").setAttributeNS(null, "fill", Colours.BLUE_HEX);
                doc.getElementById("letterI").setAttributeNS(null, "fill", Colours.ORANGE_HEX);
                doc.getElementById("letterA").setAttributeNS(null, "fill", Colours.GREEN_HEX);
            }
        });
        canvasLogo.setPreferredSize(new Dimension(width,170));
        canvasLogo.setMinimumSize(new Dimension(width,170));
        canvasLogo.setMaximumSize(new Dimension(width,170));
        canvasLogo.setURI(Splash.class.getResource("/Images/SingleLogo.svg").toString());

        canvasTextDetectingModules.setOpaque(false);
        canvasTextDetectingModules.setBackground(new Color(0,0,0,0));
        canvasTextDetectingModules.setPreferredSize(new Dimension(width,90));
        canvasTextDetectingModules.setMinimumSize(new Dimension(width,90));
        canvasTextDetectingModules.setMaximumSize(new Dimension(width,90));
        canvasTextDetectingModules.setURI(Splash.class.getResource("/Images/DetectingModules.svg").toString());
        canvasTextDetectingModules.validate();
        canvasTextDetectingModules.repaint();

        add(canvasLogo,BorderLayout.PAGE_START);
        add(canvasTextDetectingModules,BorderLayout.PAGE_END);

    }

    public void setStatus(Status status) {
        switch (status) {
            case DETECTING_MODULES:
                canvasTextDetectingModules.setURI(Splash.class.getResource("/Images/DetectingModules.svg").toString());
                break;
            case INITIALISING_MODULES:
                canvasTextDetectingModules.setURI(Splash.class.getResource("/Images/InitialisingModules.svg").toString());
                break;
            case CREATING_INTERFACE:
                canvasTextDetectingModules.setURI(Splash.class.getResource("/Images/CreatingInterface.svg").toString());
                break;
        }
    }

    static String getSpecialSuffix() {
        Date date = new Date();
        switch (new SimpleDateFormat("dd-MM").format(date)) {
            case "31-10":
                if (Integer.parseInt(new SimpleDateFormat("HH").format(date)) >= 18) return "_3110";
        }

        return "";

    }
}
