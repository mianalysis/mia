package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.swing.*;
import java.awt.*;

public class HelpArea extends JTextPane {
    public HelpArea() {
        Module activeModule = GUI.getActiveModule();

        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setContentType("text/html");
        if (activeModule != null) {
            setText("<html><body><font face=\"sans-serif\" size=\"3\">"+getHelpText(activeModule)+"</font></body></html>");
        }

        setBackground(null);
//        setLineWrap(true);
//        setWrapStyleWord(true);
        setEditable(false);
        setCaretPosition(0);

    }

    private static String getHelpText(Module module) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>DESCRIPTION</b><br>")
        .append(module.getHelp())
        .append("<br><br>")
        .append("<b>PARAMETERS</b><br>");

        for (Parameter parameter:module.getAllParameters()) {
            sb.append("<i>")
                    .append(parameter.getName())
                    .append("</i>: ")
                    .append(parameter.getDescription())
                    .append("<br><br>");
        }

        return sb.toString();

    }
}
