/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package com.ibm.streamsx.transportation.sfpark;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.ibm.json.java.JSONObject;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.inet.RestServer;
import com.ibm.streamsx.topology.json.JSONStreams;
import com.ibm.streamsx.topology.tuple.JSONAble;

public class ParkingInfo implements Serializable, JSONAble {

    /**
     * 
     */
    private static final long serialVersionUID = 6721694536946821937L;

    private ParkingLocation location;
    private ParkingFill fill;

    public static StreamSchema PARKING_INFO = com.ibm.streams.operator.Type.Factory
    .getStreamSchema("tuple<rstring id, rstring note, float64 longitude, float64 latitude, rstring markerType>");

    public ParkingInfo(ParkingLocation location, ParkingFill fill) {
        this.location = location;
        this.fill = fill;
    }

    public ParkingLocation getLocation() {
        return location;
    }

    public void setLocation(ParkingLocation location) {
        this.location = location;
    }

    public ParkingFill getFill() {
        return fill;
    }

    public void setFill(ParkingFill fill) {
        this.fill = fill;
    }

    public String getOspid() {
        return location.getOspid();
    }

    public int getAvailableSpaces() {
        return fill.getOper() - fill.getOcc();
    }
    
    @Override
    public JSONObject toJSON() {
        JSONObject jpi = new JSONObject();
        
        jpi.put("layer", "sfpark.org");
        jpi.put("id", getOspid());
        jpi.put("longitude", getLocation()
                .getLongitude());
        jpi.put("latitude", getLocation()
                .getLatitude());
        
        int fill = getFill().getFill();
        int spaces = getAvailableSpaces();
        
        String state = null;
        
        if (spaces < 20)
            state = "RED";
        else if (spaces < 100) {
            state = fill < 50 ? "GREEN" : "YELLOW";
        } else {
            state = fill > 90 ? "YELLOW" : "GREEN";
        }
        switch (state) {
        case "RED": state = "icons/parking-red.png"; break;
        case "YELLOW": state = "icons/parking-orange.png"; break;
        case "GREEN":state = "icons/parking-blue.png"; break;
        }
        jpi.put("markerType", state);

        String arrow = "\u2194"; // no change
        if (getFill().getTrend() > 2.0) // cars/min
            arrow = "<font color=\"red\">\u25B2</font>"; // "\u2191";
        else if (getFill().getTrend() < -2.0)
            arrow = "<font color=\"green\">\u25BC</font>";
        
        jpi.put("note", "Spaces:" + spaces + " (" + fill
                + "%" + arrow + " full)<BR>"
                + getLocation().getName()
                + "<BR>"
                + getLocation().getDesc()
                + "<BR>@ "
                + new Date(getFill().getTs())
                                .toString());
        return jpi;
    }

    /**
     * Create an SPLStream to send into the RestServer to visualize the data.
     * 
     * @param parkingInfo
     *            Stream of parking info.
     */
    public static void toJSONAndView(
            TStream<ParkingInfo> parkingInfo,
            String publishTopic) {
        
        TStream<JSONObject> jsonParkingInfo = JSONStreams.toJSON(parkingInfo);
                
        if (publishTopic != null)
        {
        	jsonParkingInfo.publish(publishTopic);
        }
        else
        {  
            RestServer rs = new RestServer(parkingInfo, 8082);
            rs.viewer(jsonParkingInfo.last(11, TimeUnit.SECONDS), "sfpark", "spaces");
        }
    }

}
