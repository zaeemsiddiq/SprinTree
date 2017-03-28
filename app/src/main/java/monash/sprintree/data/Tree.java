package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Tree extends SugarRecord {
    public String comId;
    public String commonName;
    public String scientificName;
    public String genus;
    public String family;
    public int diameter;
    public int yearPlanted;
    public String datePlanted;
    public String ageDescription;
    public String usefulLifeExpectency;
    public int usefulLifeExpectencyValue;
    public String precinct;
    public String locatedIn;
    public String uploadedDate;
    public String uploadDate;
    public Double latitude;
    public double longitude;
    public double easting;
    public double northing;
}
