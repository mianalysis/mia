package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.InputOutput.ImageSaver;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 13/11/2017.
 */
public class FilterImageTest {
    @Test @Ignore
    public void testRunGaussian2DFilterMethod() throws Exception {
    }

    @Test @Ignore
    public void testRunRollingFrameFilterMethod() throws Exception {
    }

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new FilterImage().getTitle());
    }

    @Test @Ignore
    public void testRunDoG2DFilter() throws Exception {
    }

    @Test @Ignore
    public void testRunDoG2DFilter3DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunDoG2DFilter4DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunGaussian2DFilter() throws Exception {
    }

    @Test @Ignore
    public void testRunGaussian2DFilter3DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunGaussian2DFilter4DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunGaussian3DFilter() throws Exception {
    }

    /**
     * Tests the module doesn't crash if a 2D image is passed to the 3D Gaussian filter
     * @throws Exception
     */
    @Test @Ignore
    public void testRunGaussian3DFilter2DStack() throws Exception {
    }

    @Test @Ignore
    public void testRunMedian3DFilter() throws Exception {
    }

    /**
     * Tests the module doesn't crash if a 2D image is passed to the 3D median filter
     * @throws Exception
     */
    @Test @Ignore
    public void testRunMedian3DFilter2DStack() throws Exception {
    }

    /**
     * Tests the rolling frame filter (designed for image stacks with more than 1 timepoint)
     * @throws Exception
     */
    @Test @Ignore
    public void testRunRollingFrameFilter() throws Exception {
    }

    /**
     * Verifyig the output when the stack only contains 1 timepoint, but a 3D stack (need to check it doesn't average
     * over Z)
     * @throws Exception
     */
    @Test @Ignore
    public void testRunRollingFrameFilterOneTimepoint() throws Exception {
    }
}