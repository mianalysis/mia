// TODO: Have global parameters for things like overlay line width
// TODO: Add input/output parameters to .mia file (and condition to avoid problems if this isn't present in files)
// TODO: Set "Run" button to simply start processing.  File path should be set at input options

package wbif.sjx.ModularImageAnalysis;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import net.imagej.ui.swing.updater.ResolveDependencies;
import net.imagej.updater.*;
import net.imagej.updater.util.*;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.avutil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.janelia.it.jacs.shared.ffmpeg.FFMPGByteAcceptor;
import org.janelia.it.jacs.shared.ffmpeg.FFMpegLoader;
import org.scijava.util.AppUtils;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Object.ErrorLog;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisReader;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisRunner;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Created by sc13967 on 14/07/2017.
 */
public class MIA implements PlugIn {
    private static final ErrorLog errorLog = new ErrorLog();
    private static ArrayList<String> pluginPackageNames = new ArrayList<>();
    private static String version = "";
    private static boolean debug = false;

    /*
    Gearing up for the transition from ImagePlus to ImgLib2 formats.  Modules can use this to add compatibility.
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
                String filepath = args[0];

                InputStream inputStream = new FileInputStream(filepath);
                Analysis analysis = AnalysisReader.loadAnalysis(inputStream);
                inputStream.close();

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

        // Checking the relevant plugins are available
        boolean[] toInstall = new boolean[2];
        Arrays.fill(toInstall,false);

        try {
            Class.forName("de.biomedical_imaging.ij.steger.Line");
        } catch (ClassNotFoundException e) {
            toInstall[0] = true;
        }

        try {
            Class.forName("inra.ijpb.binary.BinaryImages");
        } catch (ClassNotFoundException e) {
            toInstall[1] = true;
        }

        if (toInstall[0] | toInstall[1]) {
            String message = "Missing dependencies Biomedgroup and/or IJPB-plugins\nClick \"Yes\" to install";
            String title = "Dependencies missing";
            int dialogResult = JOptionPane.showConfirmDialog(null, message, "Dependencies missing", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                update(toInstall);
                JOptionPane.showMessageDialog(null, "Please restart Fiji, then run the plugin again");
                return;
            }
        }

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

    public static void update(boolean[] toInstall) {
        try {
            String imagejDirProperty = System.getProperty("imagej.dir");
            final File imagejRoot = imagejDirProperty != null ? new File(imagejDirProperty) :
                    AppUtils.getBaseDirectory("ij.dir", FilesCollection.class, "updater");

            final FilesCollection files = new FilesCollection(imagejRoot);
            AvailableSites.initializeAndAddSites(files);

            final ResolveDependencies resolver = new ResolveDependencies(null, files, true);
            resolver.resolve();

            System.err.println("Gathering available sites");
            Map<String,UpdateSite> sites = AvailableSites.getAvailableSites();
            if (toInstall[0]) {
                UpdateSite updateSite = sites.get("http://sites.imagej.net/Biomedgroup/");
                files.addUpdateSite(updateSite);
                files.activateUpdateSite(updateSite, null);
            }
            if (toInstall[1]) {
                UpdateSite updateSite = sites.get("http://sites.imagej.net/IJPB-plugins/");
                files.addUpdateSite(updateSite);
                files.activateUpdateSite(updateSite, null);
            }

            System.err.println("Installing dependencies");
            Installer installer = new Installer(files,null);
            installer.start();
            installer.moveUpdatedIntoPlace();

            files.write();

        } catch (SAXException | ParserConfigurationException | IOException | TransformerConfigurationException e) {
            e.printStackTrace();
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
}
