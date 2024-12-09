package io.github.mianalysis.mia.object.parameters.abstrakt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.process.ParameterControlFactory;
import io.github.mianalysis.mia.process.math.CumStat;
import net.objecthunter.exp4j.ExpressionBuilder;

public abstract class TextType extends Parameter {
    public TextType(String name, Module module) {
        super(name, module);
    }

    public TextType(String name, Module module, String description) {
        super(name, module, description);
    }

    public abstract void setValueFromString(String value);

    public static boolean containsReference(String string) {
        if (Pattern.compile("C[ID]?\\{([^}]?)}").matcher(string).find())
            return true;

        if (Pattern.compile("Me\\{([^}]+)}").matcher(string).find())
            return true;

        if (Pattern.compile("Im\\{([^}]+)}").matcher(string).find())
            return true;

        if (Pattern.compile("Oc\\{([^}]+)}").matcher(string).find())
            return true;

        if (Pattern.compile("Os\\{([^}]+)}").matcher(string).find())
            return true;

        return false;

    }

    public static String applyCalculation(String string) {
        try {
            Pattern pattern = Pattern.compile("(C[ID]?)\\{([^}]+)}");
            Matcher matcher = pattern.matcher(string);

            while (matcher.find()) {
                String fullName = matcher.group(0);
                String type = matcher.group(1);
                String metadataName = matcher.group(2);

                double valueDouble = new ExpressionBuilder(metadataName).build().evaluate();
                switch (type) {
                    case "C":
                    case "CD":
                        string = string.replace(fullName, String.valueOf(valueDouble));
                        break;
                    case "CI":
                        string = string.replace(fullName, String.valueOf((int) Math.round(valueDouble)));
                        break;
                }
                string = string.replace(fullName, String.valueOf(valueDouble));

            }
        } catch (Exception e) {
            // If input string contained a metadata reference (e.g. AddCustomMetadataItem
            // may do this), don't output the warning
            if (!string.contains("M{"))
                MIA.log.writeError(e);
        }

        return string;

    }

    public static String insertWorkspaceValues(String string, WorkspaceI workspace) {
        if (workspace == null)
            return string;

        // Inserting metadata values
        string = insertMetadataValues(string, workspace);

        // Inserting image measurements
        string = insertImageMeasurementValues(string, workspace);

        // Insert object collection statistics
        string = insertObjectMeasurementValues(string, workspace);

        // Insert object count
        string = insertObjectCountValues(string, workspace);

        return string;

    }

    public static String insertMetadataValues(String string, WorkspaceI workspace) {
        if (workspace == null)
            return string;

        // Inserting metadata values
        Pattern pattern = Pattern.compile("Me\\{([^}]+)}");
        Matcher matcher = pattern.matcher(string);
        Metadata metadata = workspace.getMetadata();
        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);

            if (metadata.containsKey(metadataName))
                string = string.replace(fullName, metadata.getAsString(metadataName));

        }

        return string;

    }

    public static String insertImageMeasurementValues(String string, WorkspaceI workspace) {
        if (workspace == null)
            return string;

        Pattern pattern = Pattern.compile("Im\\{([^\\}]+)}");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            Pattern imMeasPattern = Pattern.compile("([^\\|]+)\\|([^\\|]+)");
            Matcher imMeasMatcher = imMeasPattern.matcher(matcher.group(1));
            if (imMeasMatcher.find()) {
                Image image = workspace.getImage(imMeasMatcher.group(1));
                if (image == null)
                    break;

                Measurement measurement = image.getMeasurement(imMeasMatcher.group(2));
                if (measurement == null)
                    break;

                string = string.replace(matcher.group(0), String.valueOf(measurement.getValue()));

            }
        }

        return string;

    }

    public static String insertObjectMeasurementValues(String string, WorkspaceI workspace) {
        if (workspace == null)
            return string;

        Pattern pattern = Pattern.compile("Os\\{([^\\}]+)}");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            Pattern objMeasPattern = Pattern.compile("([^\\|]+)\\|([^\\|]+)\\|([^\\|]+)");
            Matcher objMeasMatcher = objMeasPattern.matcher(matcher.group(1));
            if (objMeasMatcher.find()) {
                Objs objs = workspace.getObjects(objMeasMatcher.group(1));
                if (objs == null)
                    break;

                String measurementName = objMeasMatcher.group(2);
                String statistic = objMeasMatcher.group(3).toUpperCase();
                CumStat cs = new CumStat();
                for (Obj obj : objs.values()) {
                    Measurement measurement = obj.getMeasurement(measurementName);
                    if (measurement == null)
                        continue;

                    cs.addMeasure(measurement.getValue());

                }

                switch (statistic) {
                    case "MAX":
                    case "MAXIMUM":
                        string = string.replace(matcher.group(0), String.valueOf(cs.getMax()));
                        break;
                    case "MEAN":
                        string = string.replace(matcher.group(0), String.valueOf(cs.getMean()));
                        break;
                    case "MIN":
                    case "MINIMUM":
                        string = string.replace(matcher.group(0), String.valueOf(cs.getMin()));
                        break;
                    case "STD":
                    case "STDEV":
                        string = string.replace(matcher.group(0), String.valueOf(cs.getStd()));
                        break;
                    case "SUM":
                        string = string.replace(matcher.group(0), String.valueOf(cs.getSum()));
                        break;
                }
            }
        }

        return string;

    }

    public static String insertObjectCountValues(String string, WorkspaceI workspace) {
        if (workspace == null)
            return string;

        Pattern pattern = Pattern.compile("Oc\\{([^\\}]+)}");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            Objs objs = workspace.getObjects(matcher.group(1));
            if (objs == null)
                break;

            string = string.replace(matcher.group(0), String.valueOf(objs.size()));

        }

        return string;

    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getTextTypeControl(this);
    }

    @Override
    public boolean verify() {
        // The only thing to check is that any global variables and metadata values have
        // been defined
        return GlobalVariables.variablesPresent(getRawStringValue(), module.getModules());

    }
}
