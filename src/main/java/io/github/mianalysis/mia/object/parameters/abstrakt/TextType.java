package io.github.mianalysis.mia.object.parameters.abstrakt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.objecthunter.exp4j.ExpressionBuilder;
import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;
import io.github.mianalysis.mia.gui.parametercontrols.TextParameter;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.miscellaneous.GlobalVariables;

public abstract class TextType extends Parameter {
    public TextType(String name, Module module) {
        super(name, module);
    }

    public TextType(String name, Module module, String description) {
        super(name, module, description);
    }

    public abstract void setValueFromString(String value);

    public static boolean containsCalculation(String string) {
        Pattern pattern = Pattern.compile("C\\{([^}]+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }

    public static String applyCalculation(String string) {
        Pattern pattern = Pattern.compile("C\\{([^}]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);

            double valueDouble = new ExpressionBuilder(metadataName).build().evaluate();
            string = string.replace(fullName, String.valueOf(valueDouble));

        }
        
        return string;

    }

    @Override
    protected ParameterControl initialiseControl() {
        return new TextParameter(this);
    }

    @Override
    public boolean verify() {
        // The only thing to check is that any global variables and metadata values have
        // been defined        
        return GlobalVariables.variablesPresent(getRawStringValue(), module.getModules());
        
    }
}
