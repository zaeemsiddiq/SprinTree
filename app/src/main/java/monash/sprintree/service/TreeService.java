package monash.sprintree.service;
import com.google.firebase.database.DataSnapshot;
import monash.sprintree.data.Tree;

import com.google.firebase.database.DataSnapshot;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 3/28/2017.
 */

class TreeService {

    static String saveTrees(DataSnapshot dataSnapshot) {
        List<Tree> trees = new ArrayList<>();
        int counter = 0;
        Object key = dataSnapshot.getKey();
        Object value = dataSnapshot.getValue();
        String comId = "";
        for (DataSnapshot tree : dataSnapshot.getChildren()) {
            comId = tree.getKey();
            Tree t = new Tree();
            t.comId = comId;
            for (DataSnapshot columns : tree.getChildren()) {
                String attribute = columns.getKey();
                Object data = (Object) columns.getValue();
                if (attribute.equals("Age Description"))
                    t.ageDescription = (String) data;
                if (attribute.equals("Common Name"))
                    t.commonName = (String) data;
                if (attribute.equals("CoordinateLocation"))
                    t.coordinateLocation = (String) data;
                if (attribute.equals("Date Planted"))
                    t.datePlanted = (String) data;
                if (attribute.equals("Diameter Breast Height"))
                    t.diameter = (Long) data;
                if (attribute.equals("Easting")){
                    if(data instanceof Double) {
                        t.easting = (Double) data;
                    } else {
                        Long l = new Long((Long)data);
                        t.easting = l.doubleValue();
                    }
                }
                if (attribute.equals("Family"))
                    t.family = (String) data;
                if (attribute.equals("Genus"))
                    t.genus = (String) data;
                if (attribute.equals("Latitude"))
                    t.latitude = (Double) data;
                if (attribute.equals("Located in"))
                    t.locatedIn = (String) data;
                if (attribute.equals("Longitude"))
                    t.longitude = (Double) data;
                if (attribute.equals("Northing")) {
                    if(data instanceof Double) {
                        t.northing = (Double) data;
                    } else {
                        Long l = new Long((Long)data);
                        t.northing = l.doubleValue();
                    }
                }
                if (attribute.equals("Precinct")){
                    if(data instanceof String) {
                        t.precinct = (String) data;
                    }
                }
                if (attribute.equals("Scientific Name"))
                    t.scientificName = (String) data;
                if (attribute.equals("UploadDate"))
                    t.uploadDate = (String) data;
                if (attribute.equals("Useful Life Expectancy"))
                    t.usefulLifeExpectency = (String) data;
                if (attribute.equals("Useful Life Expectancy Value"))
                    t.usefulLifeExpectencyValue = (Long) data;
                if (attribute.equals("Year Planted"))
                    t.yearPlanted = (Long) data;
                int n = 0;
            }
            trees.add(t);
            //t.save();
            counter++;
        }
        SugarRecord.saveInTx(trees);
        return comId;   // return the
    }
}