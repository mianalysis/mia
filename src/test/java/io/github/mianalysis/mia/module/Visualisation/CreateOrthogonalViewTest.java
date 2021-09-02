package io.github.mianalysis.mia.module.Visualisation;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class CreateOrthogonalViewTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateOrthogonalView<>(null).getDescription());
    }
}