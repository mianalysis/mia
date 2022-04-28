package io.github.mianalysis.mia.process;

import io.github.mianalysis.mia.MIA;

public class DependencyValidator {
    public static boolean run() {
        try {
            Class.forName("inra.ijpb.binary.BinaryImages");
        } catch (ClassNotFoundException e) {
            MIA.log.writeError(
                    "MorphoLibJ plugin missing.\r\nPlease install via Fiji Updater by adding the \"IJPB-Plugins\" update site.");
            return true;
        }

        return false;

    }

    // public static boolean run() {
    // // Checking the relevant plugins are available
    // boolean[] toInstall = new boolean[1];
    // Arrays.fill(toInstall,false);

    // // Checking for MorphoLibJ (2D and 3D morphological tools)
    // try {
    // Class.forName("inra.ijpb.binary.BinaryImages");
    // } catch (ClassNotFoundException e) {
    // toInstall[0] = true;
    // }

    // if (toInstall[0]) {
    // String message = "Missing dependency IJPB-plugins\nClick \"Yes\" to install";
    // int dialogResult = JOptionPane.showConfirmDialog(null, message, "Dependency
    // missing", JOptionPane.YES_NO_OPTION);
    // if (dialogResult == JOptionPane.YES_OPTION) {
    // update(toInstall);
    // MIA.log.writeMessage("Installation complete. Please restart Fiji.");
    // JOptionPane.showMessageDialog(null, "Please restart Fiji, then process the
    // plugin again");
    // }
    // }

    // return toInstall[0];

    // }

    // private static void update(boolean[] toInstall) {
    // try {
    // String imagejDirProperty = System.getProperty("imagej.dir");
    // final File imagejRoot = imagejDirProperty != null ? new
    // File(imagejDirProperty) :
    // AppUtils.getBaseDirectory("ij.dir", FilesCollection.class, "updater");

    // final FilesCollection files = new FilesCollection(imagejRoot);
    // AvailableSites.initializeAndAddSites(files);

    // final ResolveDependencies resolver = new ResolveDependencies(null, files,
    // true);
    // resolver.resolve();

    // MIA.log.writeStatus("Gathering available sites");
    // Map<String,UpdateSite> sites = AvailableSites.getAvailableSites();
    // if (toInstall[0]) {
    // UpdateSite updateSite = sites.get("http://sites.imagej.net/IJPB-plugins/");
    // if (updateSite == null) updateSite =
    // sites.get("https://sites.imagej.net/IJPB-plugins/");
    // if (updateSite == null) {
    // MIA.log.writeError("Can't load IJPB-plugins dependency. Please install
    // manually using Fiji Updater.");
    // return;
    // }
    // files.addUpdateSite(updateSite);
    // files.activateUpdateSite(updateSite, null);
    // }

    // MIA.log.writeStatus("Installing dependencies");
    // Installer installer = new Installer(files,null);
    // installer.start();
    // installer.moveUpdatedIntoPlace();

    // MIA.log.writeStatus("Writing to file");
    // files.write();

    // } catch (SAXException | ParserConfigurationException | IOException |
    // TransformerConfigurationException e) {
    // e.printStackTrace();
    // }
    // }
}
