package io.github.mianalysis.mia.process;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.compress.archivers.ArchiveException;

import ai.nets.samj.install.EfficientSamEnvManager;
import ai.nets.samj.install.SamEnvManagerAbstract;
import ij.IJ;
import io.bioimage.modelrunner.apposed.appose.Mamba;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.github.mianalysis.mia.MIA;

public class SAMJUtils {
    public static String getDefaultEnvironmentPath() {
        return new File(
                IJ.getDirectory("imagej") + "/" + new File(SamEnvManagerAbstract.DEFAULT_DIR).getName())
                .getAbsolutePath();

    }

    public static SamEnvManagerAbstract installSAMJ(String environmentPath)
            throws IOException, InterruptedException, ArchiveException, URISyntaxException, MambaInstallException {
        SAMJConsumer samjConsumer = new SAMJConsumer();

        SamEnvManagerAbstract envManager = EfficientSamEnvManager.create(environmentPath, samjConsumer);

        // Mamba installation via SAMJ throws an error (Consumer appears to be null when
        // Mamba calls it), so doing this part manually
        Mamba mamba = new Mamba(environmentPath);
        if (!mamba.checkMambaInstalled()) {
            MIA.log.writeStatus("Installing Mamba");
            MIA.log.writeDebug("Installing Mamba to " + environmentPath);
            new File(environmentPath).mkdirs();
            mamba.setConsoleOutputConsumer(samjConsumer);
            mamba.setErrorOutputConsumer(samjConsumer);
            mamba.installMicromamba();
        }

        if (!envManager.checkSAMDepsInstalled()) {
            MIA.log.writeStatus("Installing SAM dependencies");
            MIA.log.writeDebug("Installing SAM dependencies to " + environmentPath);
            envManager.installSAMDeps();
        }

        if (!envManager.checkModelWeightsInstalled()) {
            MIA.log.writeStatus("Installing SAM model");
            MIA.log.writeDebug("Installing SAM model weights to " + environmentPath);
            envManager.installModelWeigths();
        }

        return envManager;

    }
}
