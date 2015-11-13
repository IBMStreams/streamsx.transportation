/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package com.ibm.streamsx.transportation.sfpark;

import java.io.Serializable;
import java.util.Scanner;

import com.ibm.json.java.JSONObject;
import com.ibm.streamsx.topology.TStream;

public class ParkingLocation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6721694536946821937L;

    private final String ospid;
    private final String name;
    private final String desc;
    private final double longitude;
    private final double latitude;

    public ParkingLocation(JSONObject space) {
        
        String _ospid = (String) space.get("OSPID");
        if (_ospid == null)
            _ospid = "";

        ospid = _ospid;
        name = (String) space.get("NAME");
        desc = (String) space.get("DESC");

        Scanner lonlat = new Scanner((String) space.get("LOC"));
        lonlat.useDelimiter(",");
        longitude = lonlat.nextDouble();
        latitude = lonlat.nextDouble();
        lonlat.close();
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getOspid() {
        return ospid;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * Convert a JSONObject into a Java ParkingLocation, just using the
     * ParkingLocation constructor, passing in the JSONObject tuple.
     */
    public static TStream<ParkingLocation> toParkingLocation(
            TStream<JSONObject> spaces) {
        return spaces.transform(ParkingLocation::new).filter(loc -> !loc.getOspid().isEmpty());
    }
}
