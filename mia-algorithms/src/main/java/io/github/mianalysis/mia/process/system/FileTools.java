package io.github.mianalysis.mia.process.system;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.metadata.MetadataI;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;

public class FileTools {
    public interface FileTypes {
        String FILE_TYPE = "File";
        String FOLDER_TYPE = "Folder";
        String EITHER_TYPE = "Either";

    }

    public static String getGenericName(MetadataI metadata, String genericFormat)
            throws ServiceException, DependencyException, FormatException, IOException {
        // Returns the first generic name matching the specified format
        return getGenericNames(metadata, genericFormat)[0];

    }

    public static String[] getGenericNames(MetadataI metadata, String genericFormat)
            throws ServiceException, DependencyException, FormatException, IOException {
        String absolutePath = metadata.insertMetadataValues(genericFormat);
        String filepath = FilenameUtils.getFullPath(absolutePath);
        String filename = FilenameUtils.getName(absolutePath);

        // If name includes "*" get first instance of wildcard
        if (filename.contains("*")) {
            String[] filenames = new File(filepath).list(new WildcardFileFilter(filename));

            // Appending the filepath to the start of each name
            return Arrays.stream(filenames).map(v -> filepath + v).sorted().toArray(s -> new String[s]);
        }

        return new String[] { filepath + filename };

    }
}
