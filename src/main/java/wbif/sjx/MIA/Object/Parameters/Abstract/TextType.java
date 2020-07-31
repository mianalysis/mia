package wbif.sjx.MIA.Object.Parameters.Abstract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.objecthunter.exp4j.ExpressionBuilder;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.TextParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;

public abstract class TextType extends Parameter {
    public TextType(String name, Module module) {
        super(name, module);
    }

    public TextType(String name, Module module, String description) {
        super(name, module, description);
    }

    public abstract void setValueFromString(String value);

    public static boolean containsCalculation(String string) {
        Pattern pattern = Pattern.compile("C\\{(.+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }

    public static String applyCalculation(String string) {
        Pattern pattern = Pattern.compile("C\\{(.+)}");
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
