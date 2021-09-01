package io.github.mianalysis.MIA.Module.Visualisation;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CreateOrthogonalViewTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateOrthogonalView<>(null).getDescription());
    }
}