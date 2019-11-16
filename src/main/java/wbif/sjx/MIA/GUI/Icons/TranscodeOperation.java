package wbif.sjx.MIA.GUI.Icons;

import org.w3c.dom.Document;
import wbif.sjx.MIA.GUI.Colours;

public class TranscodeOperation {
    private final String target;
    private final String qualifiedName;
    private final String value;

    public TranscodeOperation(String target, String qualifiedName, String value) {
        this.target = target;
        this.qualifiedName = qualifiedName;
        this.value = value;
    }

    public void applyOperation(Document doc) {
        doc.getElementById(target).setAttributeNS(null, qualifiedName, value);
    }
}
