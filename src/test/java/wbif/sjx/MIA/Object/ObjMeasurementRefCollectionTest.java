package wbif.sjx.MIA.Object;

import org.junit.Test;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

import static org.junit.Assert.*;

public class ObjMeasurementRefCollectionTest {
    @Test
    public void get() {
        // Creating some measurements
        ObjMeasurementRef ref1 = new ObjMeasurementRef("ExportableRef 1");
        ObjMeasurementRef ref2 = new ObjMeasurementRef("Second ref");
        ObjMeasurementRef ref3 = new ObjMeasurementRef("ExportableRef 3");
        ObjMeasurementRef ref4 = new ObjMeasurementRef("One more");

        // Populating the collection
        ObjMeasurementRefCollection collection = new ObjMeasurementRefCollection();
        collection.put(ref1.getName(),ref1);
        collection.put(ref2.getName(),ref2);
        collection.put(ref3.getName(),ref3);
        collection.put(ref4.getName(),ref4);

        assertEquals(ref1,collection.get("ExportableRef 1"));
        assertEquals(ref3,collection.get("ExportableRef 3"));
        assertEquals(ref2,collection.get("Second ref"));
        assertEquals(ref4,collection.get("One more"));

        assertNull(collection.get("Not there"));
        assertNull(collection.get("ExportableRef 11"));

    }

    @Test
    public void updateImageObjectName() {
        // Creating some measurements
        ObjMeasurementRef ref1 = new ObjMeasurementRef("ExportableRef 1");
        ObjMeasurementRef ref2 = new ObjMeasurementRef("Second ref");
        ObjMeasurementRef ref3 = new ObjMeasurementRef("ExportableRef 3");
        ObjMeasurementRef ref4 = new ObjMeasurementRef("One more");

        // Populating the collection
        ObjMeasurementRefCollection collection = new ObjMeasurementRefCollection();
        collection.put(ref1.getName(),ref1);
        collection.put(ref2.getName(),ref2);
        collection.put(ref3.getName(),ref3);
        collection.put(ref4.getName(),ref4);

        // Checking the imageObjectName before changing
        assertEquals("",collection.get("Second ref").getObjectsName());

        // Changing the name
        collection.updateImageObjectName("Second ref","im name");

        // Checking the imageObjectName values for all references
        assertEquals("",collection.get("ExportableRef 1").getObjectsName());
        assertEquals("im name",collection.get("Second ref").getObjectsName());
        assertEquals("",collection.get("ExportableRef 3").getObjectsName());
        assertEquals("",collection.get("One more").getObjectsName());

    }
}