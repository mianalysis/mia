package wbif.sjx.MIA.Object;

import org.junit.Test;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;

import static org.junit.Assert.*;

public class MeasurementRefCollectionTest {
    @Test
    public void get() {
        // Creating some measurements
        MeasurementRef ref1 = new MeasurementRef("Ref 1", MeasurementRef.Type.OBJECT);
        MeasurementRef ref2 = new MeasurementRef("Second ref", MeasurementRef.Type.OBJECT);
        MeasurementRef ref3 = new MeasurementRef("Ref 3", MeasurementRef.Type.OBJECT);
        MeasurementRef ref4 = new MeasurementRef("One more", MeasurementRef.Type.OBJECT);

        // Populating the collection
        MeasurementRefCollection collection = new MeasurementRefCollection();
        collection.put(ref1.getName(),ref1);
        collection.put(ref2.getName(),ref2);
        collection.put(ref3.getName(),ref3);
        collection.put(ref4.getName(),ref4);

        assertEquals(ref1,collection.get("Ref 1"));
        assertEquals(ref3,collection.get("Ref 3"));
        assertEquals(ref2,collection.get("Second ref"));
        assertEquals(ref4,collection.get("One more"));

        assertNull(collection.get("Not there"));
        assertNull(collection.get("Ref 11"));

    }

    @Test
    public void updateImageObjectName() {
        // Creating some measurements
        MeasurementRef ref1 = new MeasurementRef("Ref 1", MeasurementRef.Type.OBJECT);
        MeasurementRef ref2 = new MeasurementRef("Second ref", MeasurementRef.Type.OBJECT);
        MeasurementRef ref3 = new MeasurementRef("Ref 3", MeasurementRef.Type.OBJECT);
        MeasurementRef ref4 = new MeasurementRef("One more", MeasurementRef.Type.OBJECT);

        // Populating the collection
        MeasurementRefCollection collection = new MeasurementRefCollection();
        collection.put(ref1.getName(),ref1);
        collection.put(ref2.getName(),ref2);
        collection.put(ref3.getName(),ref3);
        collection.put(ref4.getName(),ref4);

        // Checking the imageObjectName before changing
        assertEquals("",collection.get("Second ref").getImageObjName());

        // Changing the name
        collection.updateImageObjectName("Second ref","im name");

        // Checking the imageObjectName values for all references
        assertEquals("",collection.get("Ref 1").getImageObjName());
        assertEquals("im name",collection.get("Second ref").getImageObjName());
        assertEquals("",collection.get("Ref 3").getImageObjName());
        assertEquals("",collection.get("One more").getImageObjName());

    }
}