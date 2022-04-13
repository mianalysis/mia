package io.github.mianalysis.mia.module;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.cache.CacheService;
import org.scijava.plugin.Parameter;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.system.Preferences;
import net.imagej.ImageJ;
import net.imagej.ImageJService;
import net.imagej.ops.OpMatchingService;
import net.imagej.ops.OpService;

public abstract class ModuleTest {
    /** Subclasses can override to create a context with different services. */
	protected Context createContext() {
		return new Context(OpService.class, OpMatchingService.class,
			CacheService.class);
	}

	/** Sets up a SciJava context with {@link OpService}. */
	@BeforeEach
    public void setup() {
        Module.setVerbose(false);
        Context context = createContext();
        context.inject(this);
        MIA.ijService = (ImageJService) context.getService("net.imagej.ImageJService");
        
        MIA.preferences = new Preferences(null);
        MIA.preferences.setDataStorageMode(Preferences.DataStorageModes.KEEP_IN_RAM);

	}

    @Test
    public abstract void testGetHelp();

}