package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement.MergeObjects;

import static org.junit.Assert.*;

public class MergeObjectsTest {

    @Test
    public void getTitle() {
        assertNotNull(new MergeObjects().getTitle());
    }

    @Test @Ignore
    public void run() {
    }
}