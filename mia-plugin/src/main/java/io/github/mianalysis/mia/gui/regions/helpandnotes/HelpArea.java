package io.github.mianalysis.mia.gui.regions.helpandnotes;

import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import io.github.mianalysis.mia.gui.HyperlinkOpener;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

public class HelpArea extends JTextPane {
    /**
     *
     */
    private static final long serialVersionUID = 1232662621405470033L;

    public HelpArea(Module module, Modules modules) {
        setContentType("text/html");
        addHyperlinkListener(new HyperlinkOpener());

        if (module != null) {
            setText("<html><body><font face=\"sans-serif\" size=\"3\">" + getHelpText(module, modules)
                    + "</font></body></html>");
        }

        setBackground(null);
        setOpaque(false);
        setEditable(false);
        setCaretPosition(0);
        setBorder(new EmptyBorder(2, 5, 5, 5));

        revalidate();
        repaint();

    }

    private static String getHelpText(Module module, Modules modules) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>DESCRIPTION</b><br>").append(module.getDescription()).append("<br><br><br>")
                .append("<b>PARAMETERS</b><br>");

        for (Parameter parameter : module.getAllParameters().values()) {
            if (parameter.isExported())
                sb.append(getParameterHelpText(parameter));
        }

        sb.append("<br>");

        // Only evaluate if module can be run
        if (module.isRunnable()) {
            ObjMeasurementRefs objectMeasRefs = module.updateAndGetObjectMeasurementRefs();
            if (objectMeasRefs != null && objectMeasRefs.hasExportedMeasurements()) {
                sb.append("<font face=\"sans-serif\" size=\"3\"><b>OBJECT MEASUREMENTS</b><br>")
                        .append("The following measurements are currently calculated by this module.<br><br></font>");

                for (ObjMeasurementRef measurementRef : objectMeasRefs.values()) {
                    sb.append("<font face=\"sans-serif\" size=\"3\"><i>").append(measurementRef.getFinalName())
                            .append("</i></font>:<div style=\"margin-left:10px\"><font face=\"sans-serif\" size=\"3\">")
                            .append(measurementRef.getDescription()).append("</font></div><br>");
                }
                sb.append("<br>");
            }
        }

        ImageMeasurementRefs imageMeasRefs = module.updateAndGetImageMeasurementRefs();
        if (imageMeasRefs != null && imageMeasRefs.hasExportedMeasurements()) {
            sb.append("<font face=\"sans-serif\" size=\"3\"><b>IMAGE MEASUREMENTS</b><br>")
                    .append("The following measurements are currently calculated by this module.<br><br></font>");

            for (ImageMeasurementRef measurementRef : imageMeasRefs.values()) {
                sb.append("<font face=\"sans-serif\" size=\"3\"><i>").append(measurementRef.getName())
                        .append("</i></font>:<div style=\"margin-left:10px\"><font face=\"sans-serif\" size=\"3\">")
                        .append(measurementRef.getDescription()).append("</font></div><br>");
            }
        }

        return sb.toString();

    }

    private static String getParameterHelpText(Parameter parameter) {
        StringBuilder sb = new StringBuilder();

        sb.append("<font face=\"sans-serif\" size=\"3\"><i>").append(parameter.getName())
                .append("</i></font>:<div style=\"margin-left:10px\"><font face=\"sans-serif\" size=\"3\">")
                .append(parameter.getDescription()).append("</font></div><br>");

        if (parameter instanceof ParameterGroup) {
            for (Parameter currParameter : ((ParameterGroup) parameter).getTemplateParameters().values()) {
                if (currParameter.isExported())
                    sb.append(getParameterHelpText(currParameter));
            }
        }

        return sb.toString();

    }
}
