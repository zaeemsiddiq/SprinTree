package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Tree extends SugarRecord<Tree> {
    public String comId;
    public String commonName;
    public String scientificName;
    public String genus;
    public String family;
    public long diameter;
    public long yearPlanted;
    public String datePlanted;
    public String ageDescription;
    public String usefulLifeExpectency;
    public long usefulLifeExpectencyValue;
    public String precinct;
    public String coordinateLocation;
    public String locatedIn;
    public String uploadDate;
    public double latitude;
    public double longitude;
    public double easting;
    public double northing;
}