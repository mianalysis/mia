package io.github.mianalysis.mia.object.metadata;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-content result structure as an abstract class, which is extended on an experiment-by experiment basis
 * Created by sc13967 on 25/10/2016.
 */
public class Metadata extends LinkedHashMap<String,Object> implements MetadataI {
    // GETTERS AND SETTERS

    public String getFilename() {
        return get(FILENAME) == null ? null : (String) get(FILENAME);
    }

    public void setFilename(String filename) {
        put(FILENAME, filename);
    }
    
    public String getFilepath() {
        return get(FILEPATH) == null ? null : (String) get(FILEPATH);
    }

    public void setFilepath(String filepath) {
        put(FILEPATH,filepath);
    }

    public String getExt() {
        return get(EXTENSION) == null ? null : (String) get(EXTENSION);
    }

    public void setExt(String ext) {
        put(EXTENSION,ext);
    }

    public File getFile() {
        return get(FILE) == null ? null : (File) get(FILE);
    }

    public void setFile(File file) {
        put(FILE,file);
    }

    public int getHour() {
        return get(HOUR) == null ? -1 : (Integer) get(HOUR);
    }

    public void setHour(int hour) {
        put(HOUR,hour);
    }

    public int getMin() {
        return get(MINUTE) == null ? -1 : (Integer) get(MINUTE);
    }

    public void setMin(int min) {
        put(MINUTE,min);
    }

    public int getSec() {
        return get(SECOND) == null ? -1 : (Integer) get(SECOND);

    }

    public void setSec(int sec) {
        put(SECOND,sec);
    }

    public String getWell() {
        return get(WELL) == null ? null : (String) get(WELL);
    }

    public void setWell(String well) {
        put(WELL,well);
    }

    public int getRow() {
        return get(ROW) == null ? -1 : (Integer) get(ROW);
    }

    public void setRow(int row) {
        put(ROW,row);
    }

    public int getCol() {
        return get(COL) == null ? -1 : (Integer) get(COL);
    }

    public void setCol(int col) {
        put(COL,col);
    }

    public int getField() {
        return get(FIELD) == null ? -1 : (Integer) get(FIELD);
    }

    public void setField(int field) {
        put(FIELD,field);
    }

    public int getTimepoint() {
        return get(TIMEPOINT) == null ? -1 : (Integer) get(TIMEPOINT);
    }

    public void setTimepoint(int timepoint) {
        put(TIMEPOINT,timepoint);
    }

    public int getZ() {
        return get(ZPOSITION) == null ? -1 : (Integer) get(ZPOSITION);
    }

    public void setZ(int z) {
        put(ZPOSITION,z);
    }

    public int getChannel() {
        return get(CHANNEL) == null ? -1 : (Integer) get(CHANNEL);
    }

    public void setChannel(int channel) {
        put(CHANNEL,channel);
    }

    public String getCelltype() {
        return get(CELLTYPE) == null ? null : (String) get(CELLTYPE);
    }

    public void setCelltype(String celltype) {
        put(CELLTYPE,celltype);
    }

    public String getMag() {
        return get(MAGNIFICATION) == null ? null : (String) get(MAGNIFICATION);
    }

    public void setMag(String mag) {
        put(MAGNIFICATION,mag);
    }

    public int getYear() {
        return get(YEAR) == null ? -1 : (Integer) get(YEAR);
    }

    public void setYear(int year) {
        put(YEAR,year);
    }

    public int getMonth() {
        return get(MONTH) == null ? -1 : (Integer) get(MONTH);
    }

    public void setMonth(int month) {
        put(MONTH,month);
    }

    public int getDay() {
        return get(DAY) == null ? -1 : (Integer) get(DAY);
    }

    public void setDay(int day) {
        put(DAY,day);
    }

    public String getComment() {
        return get(COMMENT) == null ? null : (String) get(COMMENT);
    }

    public void setComment(String comment) {
        put(COMMENT,comment);
    }

    public String getKeyword() {
        return get(KEYWORD) == null ? null : (String) get(KEYWORD);
    }

    public void putKeyword(String keyword) {
        put(KEYWORD,keyword);
    }

    public int getSeriesNumber() {
        return get(SERIES_NUMBER) == null ? -1 : (Integer) get(SERIES_NUMBER);
    }

    public void setSeriesNumber(int seriesNumber) {
        put(SERIES_NUMBER,seriesNumber);
    }

    public String getSeriesName() {
        return get(SERIES_NAME) == null ? null : (String) get(SERIES_NAME);
    }

    public void setSeriesName(String seriesName) {
        put(SERIES_NAME,seriesName);
    }

    public String getUnits() {
        return get(UNITS) == null ? null : (String) get(UNITS);
    }

    public void setUnits(String units) {
        put(UNITS,units);
    }

    public String getPlateName() {
        return get(PLATE_NAME) == null ? null : (String) get(PLATE_NAME);
    }

    public void setPlateName(String plateName) {
        put(PLATE_NAME,plateName);
    }

    public String getPlateManufacturer() {
        return get(PLATE_MANUFACTURER) == null ? null : (String) get(PLATE_MANUFACTURER);
    }

    public void setPlateManufacturer(String plateManufacturer) {
        put(PLATE_MANUFACTURER,plateManufacturer);
    }

    public String getPlateModel() {
        return get(PLATE_MODEL) == null ? null : (String) get(PLATE_MODEL);
    }

    public void setPlateModel(String plateModel) {
        put(PLATE_MODEL,plateModel);
    }

    public int getTimelineNumber() {
        return get(TIMELINE_NUMBER) == null ? -1 : (Integer) get(TIMELINE_NUMBER);
    }

    public void setTimelineNumber(int timelineNumber) {
        put(TIMELINE_NUMBER,timelineNumber);
    }

    public int getActionNumber() {
        return get(ACTION_NUMBER) == null ? -1 : (Integer) get(ACTION_NUMBER);
    }

    public void setActionNumber(int actionNumber) {
        put(ACTION_NUMBER,actionNumber);
    }

    public String getAsString(String property) {
        Object propertyObject = get(property);
        if (propertyObject == null) return null;

        if (propertyObject instanceof File) {
            return ((File) propertyObject).getAbsolutePath();

        }

        return propertyObject.toString();

    }

    public void printParameters() {
        DecimalFormat time_df = new DecimalFormat("00");

        System.out.println("    Primary channel: " + getFile().getName());

        System.out.println("        Date: " + getDay() + "/" + getMonth() + "/" + getYear());
        System.out.println("        Time: " + time_df.format(getHour()) + ":" + time_df.format(getMin()) + ":"
                + time_df.format(getSec()));
        System.out.println("        Well: " + getWell());
        System.out.println("        Field: " + getField());
        System.out.println("        Timepoint: " + getTimepoint());
        System.out.println("        Z-position: " + getZ());
        System.out.println("        Channel: " + getChannel());
        System.out.println("        Magnification: " + getMag());
        System.out.println("        Cell type: " + getCelltype());
        System.out.println("        Comment: " + getComment());
    }

    public String insertMetadataValues(String genericFormat) {
        String outputName = genericFormat;

        // Use regex to find instances of "M{ }" and replace the contents with the
        // appropriate metadata value
        Pattern pattern = Pattern.compile("M\\{([^{}]+)}");
        Matcher matcher = pattern.matcher(genericFormat);
        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);
            String metadataValue = getAsString(metadataName);

            outputName = outputName.replace(fullName, metadataValue);

        }

        return outputName;

    }

    public Object remove(String key) {
        return super.remove(key);        
    }

    @Override
    public boolean hasKey(String key) {
        return containsKey(key);
    }   
}
