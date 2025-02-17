package io.github.mianalysis.mia.object.metadata;

import java.io.File;
import java.util.Collection;
import java.util.Set;


public interface MetadataI {
    public static final String FILENAME = "Filename";
    public static final String FILEPATH = "Filepath";
    public static final String WELL = "Well";
    public static final String ROW = "Row";
    public static final String COL = "Col";
    public static final String FIELD = "Field";
    public static final String TIMEPOINT = "Timepoint";
    public static final String ZPOSITION = "Z-position";
    public static final String CHANNEL = "Channel";
    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DAY = "Day";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    public static final String SECOND = "Second";
    public static final String CELLTYPE = "CellType";
    public static final String MAGNIFICATION = "Magnification";
    public static final String COMMENT = "Comment";
    public static final String FILE = "File";
    public static final String EXTENSION = "Extension";
    public static final String KEYWORD = "Keyword";
    public static final String SERIES_NUMBER = "Series number";
    public static final String SERIES_NAME = "Series name";
    public static final String UNITS = "Units";
    public static final String PLATE_NAME = "Plate name";
    public static final String PLATE_MANUFACTURER = "Plate manufacturer";
    public static final String PLATE_MODEL = "Plate model";
    public static final String TIMELINE_NUMBER = "Timeline number";
    public static final String ACTION_NUMBER = "Action number";
    public static final String AREA_NAME = "Area name";

    // GETTERS AND SETTERS

    public String getFilename();

    public void setFilename(String filename);
    
    public String getFilepath();

    public void setFilepath(String filepath);

    public String getExt();

    public void setExt(String ext);

    public File getFile();

    public void setFile(File file);

    public int getHour();

    public void setHour(int hour);

    public int getMin();

    public void setMin(int min);

    public int getSec();

    public void setSec(int sec);

    public String getWell();

    public void setWell(String well);

    public int getRow();

    public void setRow(int row);

    public int getCol();

    public void setCol(int col);

    public int getField();

    public void setField(int field);

    public int getTimepoint();

    public void setTimepoint(int timepoint);

    public int getZ();

    public void setZ(int z);

    public int getChannel();

    public void setChannel(int channel);

    public String getCelltype();

    public void setCelltype(String celltype);

    public String getMag();

    public void setMag(String mag);

    public int getYear();

    public void setYear(int year);

    public int getMonth();

    public void setMonth(int month);

    public int getDay();

    public void setDay(int day);

    public String getComment();

    public void setComment(String comment);

    public String getKeyword();

    public void putKeyword(String keyword);

    public int getSeriesNumber();

    public void setSeriesNumber(int seriesNumber);

    public String getSeriesName();

    public void setSeriesName(String seriesName);

    public String getUnits();

    public void setUnits(String units);

    public String getPlateName();

    public void setPlateName(String plateName);

    public String getPlateManufacturer();

    public void setPlateManufacturer(String plateManufacturer);

    public String getPlateModel();

    public void setPlateModel(String plateModel);

    public int getTimelineNumber();

    public void setTimelineNumber(int timelineNumber);

    public int getActionNumber();

    public void setActionNumber(int actionNumber);

    public String getAsString(String property);

    public void printParameters();

    public String insertMetadataValues(String genericFormat);

    public boolean hasKey(String key);

    public Set<String> keySet();

    public Collection<Object> values();

    public void clear();

    public Object put(String key, Object value);

    public Object get(Object key);

    public Object clone();

}
