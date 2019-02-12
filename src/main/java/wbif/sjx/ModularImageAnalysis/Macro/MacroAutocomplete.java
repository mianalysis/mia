package wbif.sjx.ModularImageAnalysis.Macro;

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
        System.err.println("Starting autocompletion search");
        ArrayList<BasicCompletion> completions = new ArrayList<>();
        ArrayList<MacroOperation> macroOperations = new MacroHandler().getMacroOperations();
        for (MacroOperation macroOperation:macroOperations) {
            System.err.println("    Found macro "+macroOperation.getName());
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
