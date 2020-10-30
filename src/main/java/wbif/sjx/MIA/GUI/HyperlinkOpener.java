package wbif.sjx.MIA.GUI;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HyperlinkOpener implements HyperlinkListener {

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(e.getURL().toURI());
            } catch (IOException | URISyntaxException e2) {
            }
        }
    }
}
