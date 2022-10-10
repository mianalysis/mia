package io.github.mianalysis.mia.macro;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.macro.Functions;
import io.github.mianalysis.mia.process.DependencyValidator;

@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)>Enable MIA Extensions", visible = false)
public class EnableExtensions implements Command {
    @Override
    public void run() {
        // Run the dependency validator. If updates were required, return.
        if (DependencyValidator.run())
            return;

        MacroHandler macroHandler = MacroHandler.getMacroHandler();

        if (!IJ.macroRunning()) {
            IJ.error("Cannot install extensions from outside a macro.");
            return;
        }

        Functions.registerExtensions(macroHandler);

    }
}