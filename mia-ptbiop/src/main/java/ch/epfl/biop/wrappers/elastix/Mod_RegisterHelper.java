package ch.epfl.biop.wrappers.elastix;

import java.io.File;
import java.util.function.Supplier;

import ch.epfl.biop.java.utilities.image.ElastixMultiFile;

public class Mod_RegisterHelper extends RegisterHelper {
    private int nThreads = 1;

    public void setNThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public int getNThreads() {
        return this.nThreads;
    }

    @Override
    public void align(ElastixTask align) throws Exception {
        ElastixTaskSettings settings = new ElastixTaskSettings();
        settings.nThreads(nThreads);
        
        if (!alignTaskSet) {
            if (checkParametersForAlignement()) {

                ElastixMultiFile emfMoving = ((ElastixMultiFile) movingImage.to(ElastixMultiFile.class));
                for (File f : emfMoving.files) {
                    settings.movingImage(() -> f.getAbsolutePath());
                }
                ElastixMultiFile emfFixed = ((ElastixMultiFile) fixedImage.to(ElastixMultiFile.class));
                for (File f : emfFixed.files) {
                    settings.fixedImage(() -> f.getAbsolutePath());
                }
                settings.outFolder(outputDir);

                if (verbose)
                    settings.verbose();

                if (registerInfo != null)
                    settings.taskInfo = registerInfo;

                for (Supplier<String> s : this.transformFilesSupplier) {
                    settings.addTransform(s);
                }

                if (this.initialTransformFilePath != null) {
                    settings.addInitialTransform(initialTransformFilePath);
                }
                alignTaskSet = true;
            } else {
                align = null;
                alignTaskSet = false;
            }
        }
        if (alignTaskSet) {
            assert align != null;
            align.run(settings);
        }
    }
}
