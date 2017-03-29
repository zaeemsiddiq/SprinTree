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

    public Tree() {

    }

    public Tree(String comId, String commonName, String scientificName, String genus, String family, int diameter, int yearPlanted, String datePlanted, String ageDescription, String usefulLifeExpectency, int usefulLifeExpectencyValue, String precinct, String locatedIn, String uploadedDate, String uploadDate, Double latitude, double longitude, double easting, double northing) {
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
        this.locatedIn = locatedIn;
        this.uploadedDate = uploadedDate;
        this.uploadDate = uploadDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.easting = easting;
        this.northing = northing;
    }
}
