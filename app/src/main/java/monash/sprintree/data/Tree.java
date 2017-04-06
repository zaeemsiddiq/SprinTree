package monash.sprintree.data;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Tree extends SugarRecord<Tree> {

    @SerializedName("com_id")
    public String comId;

    @SerializedName("common_name")
    public String commonName;

    @SerializedName("scientific_name")
    public String scientificName;

    @SerializedName("genus")
    public String genus;

    @SerializedName("family")
    public String family;

    @SerializedName("diameter")
    public long diameter;

    @SerializedName("year_planted")
    public long yearPlanted;

    @SerializedName("date_planted")
    public String datePlanted;

    @SerializedName("age_description")
    public String ageDescription;

    @SerializedName("useful_life_expectency")
    public String usefulLifeExpectency;

    @SerializedName("useful_life_expectency_value")
    public long usefulLifeExpectencyValue;

    @SerializedName("precinct")
    public String precinct;

    @SerializedName("located_in")
    public String locatedIn;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("easting")
    public double easting;

    @SerializedName("northing")
    public double northing;
}