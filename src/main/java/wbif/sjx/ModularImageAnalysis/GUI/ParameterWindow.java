package wbif.sjx.ModularImageAnalysis.GUI;

import ij.gui.GenericDialog;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.HCMeasurementCollection;
import wbif.sjx.ModularImageAnalysis.Object.HCModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.HCName;
import wbif.sjx.ModularImageAnalysis.Object.HCParameter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class ParameterWindow {
    public void updateParameters(HCModuleCollection modules) {
        GenericDialog gd = new GenericDialog("Parameters");

        // Creating a font for the module titles
        Font titleFont = new Font(Font.SANS_SERIF,Font.BOLD,12);

        // Running through all the modules, adding their respective parameters
        for (HCModule module:modules) {
            for (Map.Entry<String,HCParameter> entry:module.getActiveParameters().getParameters().entrySet()) {
                if (entry.getValue().isVisible()) {
                    if (entry.getValue().getType() == HCParameter.INTEGER) {
                        gd.addNumericField(entry.getKey(), (double) ((int) entry.getValue().getValue()), 1);

                    } else if (entry.getValue().getType() == HCParameter.DOUBLE) {
                        gd.addNumericField(entry.getKey(), entry.getValue().getValue(), 1);

                    } else if (entry.getValue().getType() == HCParameter.STRING) {
                        gd.addStringField(entry.getKey(), String.valueOf(entry.getValue()));

                    } else if (entry.getValue().getType() == HCParameter.CHOICE_ARRAY) {
                        gd.addChoice(entry.getKey(),(String[]) entry.getValue().getValueSource(),
                                entry.getValue().getValue());

                    } else if (entry.getValue().getType() == HCParameter.CHOICE_MAP) {
                        HashMap<String, String> map = entry.getValue().getValue();

                        for (String k:map.keySet()) {
                            gd.addStringField(k,map.get(k),1);
                        }
                    } else if (entry.getValue().getType() == HCParameter.BOOLEAN) {
                        gd.addCheckbox(entry.getKey(), entry.getValue().getValue());

                    } else if (entry.getValue().getType() == HCParameter.MEASUREMENT) {
                        // Getting the measurements available to this module
                        HCMeasurementCollection measurements = modules.getMeasurements(module);
                        String[] measurementChoices = measurements.getMeasurementNames(
                                (HCName) entry.getValue().getValueSource());
                        gd.addChoice(entry.getKey(),measurementChoices,measurementChoices[0]);

                    }
                }
            }
        }

        // Only displays the dialog if parameters were written
        if (gd.getComponentCount() > 0) {
            gd.showDialog();

            // Retrieving the results
            for (HCModule module:modules) {
                for (Map.Entry<String,HCParameter> entry:module.getActiveParameters().getParameters().entrySet()) {
                    if (entry.getValue().isVisible()) {
                        if (entry.getValue().getType() == HCParameter.INTEGER) {
                            module.getActiveParameters().getParameter(entry.getKey()).setValue(
                                    (int) Math.round(gd.getNextNumber()));

                        } else if (entry.getValue().getType() == HCParameter.DOUBLE) {
                            module.getActiveParameters().getParameter(entry.getKey()).setValue(gd.getNextNumber());

                        } else if (entry.getValue().getType() == HCParameter.STRING) {
                            module.getActiveParameters().getParameter(entry.getKey()).setValue(gd.getNextString());

                        } else if (entry.getValue().getType() == HCParameter.CHOICE_ARRAY) {
                            module.getActiveParameters().getParameter(entry.getKey()).setValue(gd.getNextChoice());

                        } else if (entry.getValue().getType() == HCParameter.CHOICE_MAP) {
                            HashMap<String,String> map = entry.getValue().getValue();

                            for (String k:map.keySet()) {
                                map.put(k,gd.getNextString());

                            }

                            module.getActiveParameters().getParameter(entry.getKey()).setValue(map);

                        } else if (entry.getValue().getType() == HCParameter.BOOLEAN) {
                            module.getActiveParameters().getParameter(entry.getKey()).setValue(gd.getNextBoolean());

                        } else if (entry.getValue().getType() == HCParameter.MEASUREMENT) {
                            module.getActiveParameters().getParameter(entry.getKey()).setValue(gd.getNextChoice());

                        }
                    }
                }
            }
        }
    }
}
