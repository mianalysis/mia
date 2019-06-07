// TODO: Have global parameters for things like overlay line width
// TODO: Add input/output parameters to .mia file (and condition to avoid problems if this isn't present in files)
// TODO: Set "Run" button to simply start processing.  File path should be set at input options

package wbif.sjx.MIA;

import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import ij.plugin.AVI_Reader;
import ij.plugin.MacroInstaller;
import ij.plugin.PlugIn;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Macro.EnableExtensions;
import wbif.sjx.MIA.Module.Hidden.GlobalVariables;
import wbif.sjx.MIA.Object.ErrorLog;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisReader;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisRunner;
import wbif.sjx.MIA.Process.DependencyValidator;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static wbif.sjx.MIA.Module.Hidden.GlobalVariables.ADD_NEW_VARIABLE;
import static wbif.sjx.MIA.Module.Hidden.GlobalVariables.VARIABLE_NAME;
import static wbif.sjx.MIA.Module.Hidden.GlobalVariables.VARIABLE_VALUE;


/**
 * Created by sc13967 on 14/07/2017.
 */
public class MIA implements PlugIn {
    private static final ErrorLog errorLog = new ErrorLog();
    private static ArrayList<String> pluginPackageNames = new ArrayList<>();
    private static String version = "";
    private static boolean debug = false;
    private static GlobalVariables globalVariables = new GlobalVariables(null);
    private static boolean macroLock = false;

    /*
    Gearing up for the transition from ImagePlus to ImgLib2 formats.  Modules can use this to addRef compatibility.
     */
    private static final boolean imagePlusMode = true;

    public static void main(String[] args) throws Exception {
        debug = true;

        // Determining the version number from the pom file
        try {
            FileReader reader = new FileReader("pom.xml");
            Model model = new MavenXpp3Reader().read(reader);
            reader.close();
            version = new MavenProject(model).getVersion();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        // Redirecting the error OutputStream, so as well as printing to the usual stream, it stores it as a string.
        ErrorLog errorLog = new ErrorLog();
        TeeOutputStream teeOutputStream = new TeeOutputStream(System.err,errorLog);
        PrintStream printStream = new PrintStream(teeOutputStream);
        System.setErr(printStream);

        try {
            if (args.length == 0) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new ImageJ();
                new GUI();

            } else {
                Analysis analysis = AnalysisReader.loadAnalysis(args[0]);
                AnalysisRunner.startAnalysis(analysis);

            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException |
                UnsupportedLookAndFeelException | ParserConfigurationException | InterruptedException e) {
            e.printStackTrace(System.err);

        }
    }

    @Override
    public void run(String s) {
        debug = false;

        // Determining the version number from the pom file
        try {
            FileReader reader = new FileReader("pom.xml");
            Model model = new MavenXpp3Reader().read(reader);
            version = new MavenProject(model).getVersion();
        } catch (XmlPullParserException | IOException e) {
            version = getClass().getPackage().getImplementationVersion();
        }

        // Run the dependency validator.  If updates were required, return.
        if (DependencyValidator.run()) return;

        // Redirecting the error OutputStream, so as well as printing to the usual stream, it stores it as a string.
        ErrorLog errorLog = new ErrorLog();
        TeeOutputStream teeOutputStream = new TeeOutputStream(System.err,errorLog);
        PrintStream printStream = new PrintStream(teeOutputStream);
        System.setErr(printStream);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new GUI();
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

    public static void addPluginPackageName(String packageName) {
        pluginPackageNames.add(packageName);
    }

    public static ArrayList<String> getPluginPackages() {
        return pluginPackageNames;
    }

    public static String getVersion() {
        return version;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        MIA.debug = debug;
    }

    public static String getSlashes() {
        // Setting the file path slashes depending on the operating system
        if (SystemUtils.IS_OS_WINDOWS) return  "\\";
        else if (SystemUtils.IS_OS_MAC_OSX) return  "/";
        else if (SystemUtils.IS_OS_LINUX) return  "/";

        return "\\";

    }

    public static void setGlobalVariables(GlobalVariables globalVariables) {
        MIA.globalVariables = globalVariables;
    }

    public static GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    public static boolean isMacroLocked() {
        return macroLock;
    }

    public static void setMacroLock(boolean macroLock) {
        MIA.macroLock = macroLock;
    }
}
