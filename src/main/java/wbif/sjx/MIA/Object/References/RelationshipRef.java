package wbif.sjx.MIA.Object.References;

import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;
import wbif.sjx.MIA.Object.References.Abstract.Ref;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashMap;

public class RelationshipRef extends ExportableRef {
    private final String parentName;
    private final String childName;

    public RelationshipRef(String parentName, String childName) {
        super(createName(parentName,childName));
        this.parentName = parentName;
        this.childName = childName;

    }

    @Override
    public void appendXMLAttributes(Element element)  {
        // Adding the values from ExportableRef
        super.appendXMLAttributes(element);

        element.setAttribute("CHILD_NAME",childName);
        element.setAttribute("PARENT_NAME",parentName);

    }

    @Override
    public void addSummaryXLSX(Sheet sheet, LinkedHashMap<Integer, Workspace> workspaces) {

    }

    public String getParentName() {
        return parentName;
    }

    public String getChildName() {
        return childName;
    }

    public static String createName(String parent, String child) {
        return parent+" // "+child;
    }

}
