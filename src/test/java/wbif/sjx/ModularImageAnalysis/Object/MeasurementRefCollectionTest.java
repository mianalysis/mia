package wbif.sjx.ModularImageAnalysis.Object;

import org.junit.Test;

import static org.junit.Assert.*;

public class MeasurementRefCollectionTest {
    @Test
    public void get() {
        // Creating some measurements
        MeasurementRef ref1 = new MeasurementRef("Ref 1");
        MeasurementRef ref2 = new MeasurementRef("Second ref");
        MeasurementRef ref3 = new MeasurementRef("Ref 3");
        MeasurementRef ref4 = new MeasurementRef("One more");

        // Populating the collection
        MeasurementRefCollection collection = new MeasurementRefCollection();
        collection.add(ref1);
        collection.add(ref2);
        collection.add(ref3);
        collection.add(ref4);

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
        MeasurementRef ref1 = new MeasurementRef("Ref 1");
        MeasurementRef ref2 = new MeasurementRef("Second ref");
        MeasurementRef ref3 = new MeasurementRef("Ref 3");
        MeasurementRef ref4 = new MeasurementRef("One more");

        // Populating the collection
        MeasurementRefCollection collection = new MeasurementRefCollection();
        collection.add(ref1);
        collection.add(ref2);
        collection.add(ref3);
        collection.add(ref4);

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