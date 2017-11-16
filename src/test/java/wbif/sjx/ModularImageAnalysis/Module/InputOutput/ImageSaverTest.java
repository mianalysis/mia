package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.ChannelExtractor;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 13/11/2017.
 */
public class ImageSaverTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new ImageSaver().getTitle());
    }

    @Test @Ignore
    public void testRunSaveWithInputFile() throws Exception {
    }

    @Test @Ignore
    public void testRunSaveAtSpecificLocation() throws Exception {
    }

    @Test @Ignore
    public void testRunSaveInMirroredDirectory() throws Exception {
    }

    @Test @Ignore
    public void testRunSaveWithFlattenedOverlay() throws Exception {
    }
}