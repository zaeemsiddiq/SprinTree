package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Tree extends SugarRecord {
    String comId;
    String commonName;
    String scientificName;
    String genus;
    String family;
    int diameter;
    int yearPlanted;
    String datePlanted;
    String ageDescription;
    String usefulLifeExpectency;
    int usefulLifeExpectencyValue;
    String precinct;
    String locatedIn;
    String uploadedDate;
    String uploadDate;
    Double latitude;
    double longitude;
    double easting;
    double northing;
}
