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
    private int height = 290;
    private int width = 250;

    JPanel panel = new JPanel();
    private BorderLayout layout = new BorderLayout();

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

        setLayout(layout);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(width,height));
        setMinimumSize(new Dimension(width,height));
        setMaximumSize(new Dimension(width,height));
        setResizable(false);

        canvasLogo.setDocumentState(JSVGCanvas.ALWAYS_INTERACTIVE);
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
        canvasLogo.setPreferredSize(new Dimension(width,210));
        canvasLogo.setMinimumSize(new Dimension(width,210));
        canvasLogo.setMaximumSize(new Dimension(width,210));
        canvasLogo.setURI(Splash.class.getResource("/Images/SingleLogo.svg").toString());

        canvasTextDetectingModules.setOpaque(false);
        canvasTextDetectingModules.setBackground(new Color(0,0,0,0));
        canvasTextDetectingModules.setPreferredSize(new Dimension(width,210));
        canvasTextDetectingModules.setMinimumSize(new Dimension(width,210));
        canvasTextDetectingModules.setMaximumSize(new Dimension(width,210));
        canvasTextDetectingModules.setURI(Splash.class.getResource("/Images/DetectingModules.svg").toString());

        canvasTextInitialisingModules.setOpaque(false);
        canvasTextInitialisingModules.setBackground(new Color(0,0,0,0));
        canvasTextInitialisingModules.setPreferredSize(new Dimension(width,210));
        canvasTextInitialisingModules.setMinimumSize(new Dimension(width,210));
        canvasTextInitialisingModules.setMaximumSize(new Dimension(width,210));
        canvasTextInitialisingModules.setURI(Splash.class.getResource("/Images/InitialisingModules.svg").toString());

        canvasTextCreatingInterface.setOpaque(false);
        canvasTextCreatingInterface.setBackground(new Color(0,0,0,0));
        canvasTextCreatingInterface.setPreferredSize(new Dimension(width,210));
        canvasTextCreatingInterface.setMinimumSize(new Dimension(width,210));
        canvasTextCreatingInterface.setMaximumSize(new Dimension(width,210));
        canvasTextCreatingInterface.setURI(Splash.class.getResource("/Images/CreatingInterface.svg").toString());

        add(BorderLayout.PAGE_START,canvasLogo);
        panel.add(canvasTextDetectingModules);
        panel.setBackground(new Color(0,0,0,0));
        add(BorderLayout.PAGE_END,panel);

//        validate();
//        pack();

    }

    public void setStatus(Status status) {
        removeAll();
        add(BorderLayout.PAGE_START,canvasLogo);
        panel = new JPanel();
        panel.setBackground(new Color(0,0,0,0));
//        panel.add(canvasTextDetectingModules);

        switch (status) {
            case DETECTING_MODULES:
//                panel.remove(0);
                panel.add(canvasTextDetectingModules);
//                getContentPane().remove(layout.getLayoutComponent(BorderLayout.PAGE_END));
//                getContentPane().add(canvasTextDetectingModules,BorderLayout.PAGE_END);
                break;
            case INITIALISING_MODULES:
//                panel.remove(0);
                panel.add(canvasTextInitialisingModules);
//                getContentPane().remove(layout.getLayoutComponent(BorderLayout.PAGE_END));
//                getContentPane().add(canvasTextInitialisingModules,BorderLayout.PAGE_END);
                break;
            case CREATING_INTERFACE:
//                panel.remove(0);
                panel.add(canvasTextCreatingInterface);
//                getContentPane().remove(layout.getLayoutComponent(BorderLayout.PAGE_END));
//                getContentPane().add(canvasTextCreatingInterface,BorderLayout.PAGE_END);
                break;
        }
        add(BorderLayout.PAGE_END,panel);
        repaint();
//        revalidate();
//        pack();
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
