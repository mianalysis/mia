// TODO: Have global parameters for things like overlay line width
// TODO: Add input/output parameters to .mia file (and condition to avoid problems if this isn't present in files)
// TODO: Set "Run" button to simply start processing.  File path should be set at input options

package wbif.sjx.ModularImageAnalysis;

import ij.ImageJ;
import ij.plugin.PlugIn;
import org.apache.commons.io.output.TeeOutputStream;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.MainGUI;
import wbif.sjx.ModularImageAnalysis.Object.ErrorLog;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisReader;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisRunner;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisWriter;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sc13967 on 14/07/2017.
 */
public class ModularImageAnalysisPlugin implements PlugIn {
    private static final ErrorLog errorLog = new ErrorLog();

    /*
    Gearing up for the transition from ImagePlus to ImgLib2 formats.  Modules can use this to add compatibility.
     */
    private static final boolean imagePlusMode = true;

    public static void main(String[] args) {
        // Redirecting the error OutputStream, so as well as printing to the usual stream, it stores it as a string.
        ErrorLog errorLog = new ErrorLog();
        TeeOutputStream teeOutputStream = new TeeOutputStream(System.err,errorLog);
        PrintStream printStream = new PrintStream(teeOutputStream);
        System.setErr(printStream);

        try {
            if (args.length == 0) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                new ImageJ();
                new MainGUI(true);

            } else {
                String filepath = args[0];

                InputStream inputStream = new FileInputStream(filepath);

                Analysis analysis = AnalysisReader.loadAnalysis(inputStream);
                AnalysisRunner.startAnalysis(analysis);

            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ParserConfigurationException | IOException | GenericMIAException | InterruptedException | SAXException e) {
            e.printStackTrace(System.err);

        }
    }

    @Override
    public void run(String s) {
        // Checking the relevant plugins are available
        boolean missing = false;
        try {
            Class.forName("inra.ijpb.binary.BinaryImages");
        } catch (ClassNotFoundException e) {
            System.err.println("MorphoLibJ plugin missing:\n" +
                    "   - Install via Fiji Updater (Help > Update...)\n" +
                    "   - Click \"Manage update sites\"\n" +
                    "   - Select \"IJPB-plugins\" and close window\n" +
                    "   - Click \"Apply changes\" and restart Fiji");
            missing = true;
        }

        try {
            Class.forName("de.biomedical_imaging.ij.steger.Line");
        } catch (ClassNotFoundException e) {
            System.err.println("Ridge detection plugin missing:\n" +
                    "   - Install via Fiji Updater (Help > Update...)\n" +
                    "   - Click \"Manage update sites\"\n" +
                    "   - Select \"Biomedgroup\" and close window\n" +
                    "   - Click \"Apply changes\" and restart Fiji");
            missing = true;
        }

        if (missing) return;

        // Redirecting the error OutputStream, so as well as printing to the usual stream, it stores it as a string.
        ErrorLog errorLog = new ErrorLog();
        TeeOutputStream teeOutputStream = new TeeOutputStream(System.err,errorLog);
        PrintStream printStream = new PrintStream(teeOutputStream);
        System.setErr(printStream);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new MainGUI(false);
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    public static ErrorLog getErrorLog() {
        return errorLog;
    }

    public static boolean isImagePlusMode() {
        return imagePlusMode;
    }
}
