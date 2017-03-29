package monash.sprintree.service;
import com.google.firebase.database.DataSnapshot;
import monash.sprintree.data.Tree;
/**
 * Created by kalyani on 28/3/17.
 */

class TreeService {

    static String saveTree(DataSnapshot dataSnapshot) {
        int addedTrees = 0;

        Object key = dataSnapshot.getKey();
        Object value = dataSnapshot.getValue();

        String comId = "";
        for( DataSnapshot tree: dataSnapshot.getChildren()) {
            comId = tree.getKey();
            Tree t= new Tree();
            t.comId=comId;
            for( DataSnapshot columns: tree.getChildren() ) {
                String attribute = columns.getKey();
                Object data = (Object)columns.getValue();
                if (attribute=="Age Description")
                    t.ageDescription=(String)data;
                if(attribute=="Common Name")
                    t.commonName=(String)data;
                if(attribute=="CoordinateLocation")
                    t.coordinateLocation=(String)data;
                if(attribute=="Date Planted")
                    t.datePlanted=(String)data;
                if(attribute=="Diameter Breast Height")
                    t.diameter=(Long)data;
                if(attribute=="Easting")
                    t.easting=(Double)data;
                if(attribute=="Family")
                    t.family=(String)data;
                if(attribute=="Genus")
                    t.genus=(String)data;
                if(attribute=="Latitude")
                    t.latitude=(Double)data;
                if(attribute=="Located in")
                    t.locatedIn=(String)data;
                if(attribute=="Longitude")
                    t.longitude=(Double)data;
                if(attribute=="Northing")
                    t.northing=(Double)data;
                if(attribute=="Precinct")
                    t.precinct=(String)data;
                if(attribute=="Scientific Name")
                    t.scientificName=(String)data;
                if(attribute=="UploadDate")
                    t.uploadDate=(String)data;
                if(attribute=="Useful Life Expectancy")
                    t.usefulLifeExpectency=(String)data;
                if(attribute=="Useful Life Expectancy Value")
                    t.usefulLifeExpectencyValue=(Long)data;
                if(attribute=="Year Planted")
                    t.yearPlanted=(Long)data;
                t.save();
            }
        }
        return comId;   // return the
    }
}
