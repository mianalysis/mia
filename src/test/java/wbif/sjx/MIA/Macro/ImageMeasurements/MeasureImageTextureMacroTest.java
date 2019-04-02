package wbif.sjx.MIA.Macro.ImageMeasurements;

import wbif.sjx.MIA.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class MeasureImageTextureMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MeasureImageTextureMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MeasureImageTextureMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MeasureImageTextureMacro(null).getDescription());
    }
}