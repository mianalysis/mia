package io.github.mianalysis.MIA.Module.Visualisation.Overlays;

import io.github.mianalysis.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 29/03/2019.
 */
public class AddTracksTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new AddTracks(null).getDescription());
    }
}