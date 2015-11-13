/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package com.ibm.streamsx.transportation.sfpark;

import com.ibm.json.java.JSONObject;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.tuple.Keyable;

public class ParkingOccupancy implements Keyable<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 6721694536946821937L;

    public static TStream<ParkingOccupancy> toParkingOccupancy(
            TStream<JSONObject> spaces) {
        return spaces.transform(v -> {
                String ospid = (String) v.get("OSPID");
                String occ = (String) v.get("OCC");
                String oper = (String) v.get("OPER");
                // missing information?
                if (ospid == null || occ == null || oper == null)
                    return null;
                return new ParkingOccupancy(ospid, (Long) v.get("ts"),
                        Integer.parseInt(occ),
                        Integer.parseInt(oper));
        });
    }

    private final String ospid;

    private final long ts;
    private final int oper;
    private final int occ;

    private ParkingOccupancy(String ospid, long ts, int occ, int oper) {

        this.ospid = ospid.intern();
        this.ts = ts;
        this.occ = occ;
        this.oper = oper;
    }

    public int getOper() {
        return oper;
    }

    public int getOcc() {
        return occ;
    }

    public String getOspid() {
        return ospid;
    }

    @Override
    public String getKey() {
        return ospid;
    }

    public long getTs() {
        return ts;
    }
}
