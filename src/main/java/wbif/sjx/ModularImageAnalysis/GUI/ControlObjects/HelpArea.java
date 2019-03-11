package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRef;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class HelpArea extends JTextPane {
    public HelpArea(Module module) {
        setContentType("text/html");
        if (module != null) {
            setText("<html><body><font face=\"sans-serif\" size=\"3\">"+getHelpText(module)+"</font></body></html>");
        }

        setBackground(null);
        setEditable(false);
        setCaretPosition(0);

        revalidate();
        repaint();

    }

    private static String getHelpText(Module module) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>DESCRIPTION</b><br>")
                .append(module.getHelp())
                .append("<br><br><br>")
                .append("<b>PARAMETERS</b><br>");

        for (Parameter parameter:module.getAllParameters()) {
            sb.append("<i>")
                    .append(parameter.getName())
                    .append("</i>:<br>    ")
                    .append(parameter.getDescription())
                    .append("<br><br>");
        }

        sb.append("<br>");

        MeasurementRefCollection objectMeasRefs = module.updateAndGetObjectMeasurementRefs();
        if (objectMeasRefs != null && objectMeasRefs.hasExportedMeasurements()) {
            sb.append("<b>OBJECT MEASUREMENTS</b><br>")
                    .append("The following measurements are currently calculated by this module.<br><br>");

            for (MeasurementRef measurementRef : objectMeasRefs.values()) {
                sb.append("<i>")
                        .append(measurementRef.getFinalName())
                        .append("</i>:<br>")
                        .append(measurementRef.getDescription())
                        .append("<br><br>");
            }
                    sb.append("<br>");

        }

        MeasurementRefCollection imageMeasRefs = module.updateAndGetImageMeasurementRefs();
        if (imageMeasRefs != null && imageMeasRefs.hasExportedMeasurements()) {
            sb.append("<b>IMAGE MEASUREMENTS</b><br>")
                    .append("The following measurements are currently calculated by this module.<br><br>");

            for (MeasurementRef measurementRef : imageMeasRefs.values()) {
                sb.append("<i>")
                        .append(measurementRef.getName())
                        .append("</i>:<br>")
                        .append(measurementRef.getDescription())
                        .append("<br><br>");
            }
        }

        return sb.toString();

    }
}
