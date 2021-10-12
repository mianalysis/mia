package io.github.mianalysis.mia.module.visualisation;

import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.module.visualise.CreateOrthogonalView;

import static org.junit.jupiter.api.Assertions.*;


public class CreateOrthogonalViewTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateOrthogonalView<>(null).getDescription());
    }
}