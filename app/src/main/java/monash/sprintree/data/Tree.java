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

    public Tree(String comId, String commonName, String scientificName, String genus, String family, long diameter, long yearPlanted, String datePlanted, String ageDescription, String usefulLifeExpectency, long usefulLifeExpectencyValue, String precinct, String coordinateLocation, String locatedIn, String uploadDate, double latitude, double longitude, double easting, double northing) {
        this.comId = comId;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.genus = genus;
        this.family = family;
        this.diameter = diameter;
        this.yearPlanted = yearPlanted;
        this.datePlanted = datePlanted;
        this.ageDescription = ageDescription;
        this.usefulLifeExpectency = usefulLifeExpectency;
        this.usefulLifeExpectencyValue = usefulLifeExpectencyValue;
        this.precinct = precinct;
        this.coordinateLocation = coordinateLocation;
        this.locatedIn = locatedIn;
        this.uploadDate = uploadDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.easting = easting;
        this.northing = northing;
    }

    public Tree() {
    }
}
