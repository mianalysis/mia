package wbif.sjx.MIA.Object.Parameters;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Object.ModuleCollection;

import static org.junit.Assert.*;

public class ChildObjectsPTest {

    @Test
    public void testDuplicate() {
        ModuleCollection modules = new ModuleCollection();
        FilterImage filterImage = new FilterImage(modules);

        ChildObjectsP childObjectsP = new ChildObjectsP("Test param",filterImage);
        childObjectsP.setParentObjectsName("Par_name");
        ChildObjectsP duplicated = childObjectsP.duplicate();

        assertEquals("Test param",duplicated.getName());
        assertEquals(filterImage,duplicated.getModule());
                

    }

    @Test @Ignore
    public void getRawStringValue() {
    }

    @Test @Ignore
    public void verify() {
    }

    @Test @Ignore
    public void getRawStringValue1() {
    }

    @Test @Ignore
    public void appendXMLAttributes() {
    }

    @Test @Ignore
    public void setAttributesFromXML() {
    }
}