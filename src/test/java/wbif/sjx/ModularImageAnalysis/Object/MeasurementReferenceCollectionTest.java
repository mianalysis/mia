package wbif.sjx.ModularImageAnalysis.Object;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class MeasurementReferenceCollectionTest {
    @Test
    public void get() {
        // Creating some measurements
        MeasurementReference ref1 = new MeasurementReference("Ref 1");
        MeasurementReference ref2 = new MeasurementReference("Second ref");
        MeasurementReference ref3 = new MeasurementReference("Ref 3");
        MeasurementReference ref4 = new MeasurementReference("One more");

        // Populating the collection
        MeasurementReferenceCollection collection = new MeasurementReferenceCollection();
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
        MeasurementReference ref1 = new MeasurementReference("Ref 1");
        MeasurementReference ref2 = new MeasurementReference("Second ref");
        MeasurementReference ref3 = new MeasurementReference("Ref 3");
        MeasurementReference ref4 = new MeasurementReference("One more");

        // Populating the collection
        MeasurementReferenceCollection collection = new MeasurementReferenceCollection();
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

    @Test
    public void getMeasurementNickNames() {
        // Creating some measurements
        MeasurementReference ref1 = new MeasurementReference("Ref 1");
        MeasurementReference ref2 = new MeasurementReference("Second ref");
        MeasurementReference ref3 = new MeasurementReference("Ref 3");
        MeasurementReference ref4 = new MeasurementReference("One more");

        // Populating the collection
        MeasurementReferenceCollection collection = new MeasurementReferenceCollection();
        collection.add(ref1);
        collection.add(ref2);
        collection.add(ref3);
        collection.add(ref4);

        // Checking the imageObjectName before changing
        assertEquals("",collection.get("Second ref").getImageObjName());

        // Checking the nickname values for all references
        assertEquals("Ref 1",collection.get("Ref 1").getNickName());
        assertEquals("Second ref",collection.get("Second ref").getNickName());
        assertEquals("Ref 3",collection.get("Ref 3").getNickName());
        assertEquals("One more",collection.get("One more").getNickName());

        // Changing some nicknames
        collection.get("Ref 1").setNickName("NN 1");
        collection.get("One more").setNickName("NN 1");
        collection.get("Ref 3").setNickName("Another name");

        // Checking the nickname values for all references
        assertEquals("NN 1",collection.get("Ref 1").getNickName());
        assertEquals("Second ref",collection.get("Second ref").getNickName());
        assertEquals("Another name",collection.get("Ref 3").getNickName());
        assertEquals("NN 1",collection.get("One more").getNickName());

    }
}