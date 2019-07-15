package wbif.sjx.MIA.Process;

import net.imagej.ui.swing.updater.ResolveDependencies;
import net.imagej.updater.FilesCollection;
import net.imagej.updater.Installer;
import net.imagej.updater.UpdateSite;
import net.imagej.updater.util.AvailableSites;
import org.scijava.util.AppUtils;
import org.xml.sax.SAXException;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class DependencyValidator {
    public static boolean run() {
        // Checking the relevant plugins are available
        boolean[] toInstall = new boolean[2];
        Arrays.fill(toInstall,false);

        // Checking for Biomedgroup (ridge detection)
        try {
            Class.forName("de.biomedical_imaging.ij.steger.Line");
        } catch (ClassNotFoundException e) {
            toInstall[0] = true;
        }

        // Checking for MorphoLibJ (2D and 3D morphological tools)
        try {
            Class.forName("inra.ijpb.binary.BinaryImages");
        } catch (ClassNotFoundException e) {
            toInstall[1] = true;
        }

        // Checking for StackReg (image registration)
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
                MIA.log.write("Installation complete.  Please restart Fiji.", LogRenderer.Level.MESSAGE);
                JOptionPane.showMessageDialog(null, "Please restart Fiji, then process the plugin again");
            }
        }

        return (toInstall[0] || toInstall[1]);

    }

    private static void update(boolean[] toInstall) {
        try {
            String imagejDirProperty = System.getProperty("imagej.dir");
            final File imagejRoot = imagejDirProperty != null ? new File(imagejDirProperty) :
                    AppUtils.getBaseDirectory("ij.dir", FilesCollection.class, "updater");

            final FilesCollection files = new FilesCollection(imagejRoot);
            AvailableSites.initializeAndAddSites(files);

            final ResolveDependencies resolver = new ResolveDependencies(null, files, true);
            resolver.resolve();

            MIA.log.write("Gathering available sites", LogRenderer.Level.MESSAGE);
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

            MIA.log.write("Installing dependencies", LogRenderer.Level.MESSAGE);
            Installer installer = new Installer(files,null);
            installer.start();
            installer.moveUpdatedIntoPlace();

            MIA.log.write("Writing to file", LogRenderer.Level.MESSAGE);
            files.write();

        } catch (SAXException | ParserConfigurationException | IOException | TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }
}
