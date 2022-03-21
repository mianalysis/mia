package io.github.mianalysis.mia.macro;

import net.imagej.legacy.plugin.MacroExtensionAutoCompletionPlugin;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;


@Plugin(type = MacroExtensionAutoCompletionPlugin.class)
public class MacroAutocomplete implements MacroExtensionAutoCompletionPlugin {
    @Override
    public List<BasicCompletion> getCompletions(CompletionProvider completionProvider) {
        ArrayList<BasicCompletion> completions = new ArrayList<>();
        ArrayList<MacroOperation> macroOperations = MacroHandler.getMacroHandler().getMacroOperations();
        for (MacroOperation macroOperation:macroOperations) {
            String name = macroOperation.getName();
            String argDescription = macroOperation.getArgumentsDescription();
            String description = macroOperation.getDescription();

            if (name == null) name = "";
            if (argDescription == null) argDescription = "";
            if (description == null) description = "";

            String command = "Ext."+name+" ("+argDescription+")";
            completions.add(new BasicCompletion(completionProvider,command,null,description));

        }

        return completions;

    }
}
