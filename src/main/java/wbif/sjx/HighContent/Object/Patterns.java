package wbif.sjx.common.HighContent.Object;

/**
 * Created by sc13967 on 23/03/2017.
 */
@Deprecated
public class Patterns {
    private final static String incuCyteShortFilenamePattern = "(.+)_([A-Z]\\d+?)_(\\d++)";
    private final static String incuCyteLongFilenamePattern = "(.+)_([A-Z]\\d+?)_(\\d++)_(\\d{4})y(\\d{2})m(\\d{2})d_(\\d{2})h(\\d{2})m";
    private final static String cellVoyagerFilenamePattern = "W(\\d+?)F(\\d+?)T(\\d+?)Z(\\d+?)C(\\d+?)";
    private final static String cellVoyagerFolderPattern = "(\\d{4})(\\d{2})(\\d{2})T(\\d{2})(\\d{2})(\\d{2})_([^_]+?)_([^_\\\\]++)_?([.[^\\\\]]++)?";


    // GETTERS AND SETTERS

    public static String getIncuCyteShortFilenamePattern() {
        return incuCyteShortFilenamePattern;
    }

    public static String getIncuCyteLongFilenamePattern() {
        return incuCyteShortFilenamePattern;
    }

    public static String getCellVoyagerFilenamePattern() {
        return cellVoyagerFilenamePattern;
    }

    public static String getCellVoyagerFolderPattern() {
        return cellVoyagerFolderPattern;
    }

}
