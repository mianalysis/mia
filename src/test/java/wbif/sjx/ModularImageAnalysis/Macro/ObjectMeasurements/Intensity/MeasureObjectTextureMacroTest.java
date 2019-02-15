package wbif.sjx.ModularImageAnalysis.Macro.ObjectMeasurements.Intensity;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperationTest;

import static org.junit.Assert.*;

public class MeasureObjectTextureMacroTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MeasureObjectTextureMacro(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MeasureObjectTextureMacro(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MeasureObjectTextureMacro(null).getDescription());
    }
}