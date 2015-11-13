package com.ibm.streamsx.transportation.sfpark.apps;

/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ibm.json.java.JSONObject;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.TWindow;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.spl.SPL;
import com.ibm.streamsx.transportation.sfpark.ParkingFill;
import com.ibm.streamsx.transportation.sfpark.ParkingInfo;
import com.ibm.streamsx.transportation.sfpark.ParkingLocation;
import com.ibm.streamsx.transportation.sfpark.ParkingOccupancy;
import com.ibm.streamsx.transportation.sfpark.SFParkStreams;

// http://sfpark.org/
public class SFParkDemo {

    public static void main(String[] args) throws Exception {
    	
    	boolean selfMap = args.length == 0;
    	String mapTopic = selfMap ? null : args[0];

        /*
         * First, Declare the topology.
         */

        Topology topology = new Topology();
        
        SPL.addToolkit(topology, new File(System.getProperty("user.home"),
                "toolkits/com.ibm.streamsx.inet"));

        // Get a stream containing parking locations with
        // available spaces.
        TStream<JSONObject> spaces = SFParkStreams.availibilityLocations(topology, true);

        TStream<ParkingInfo> info = monitorSpaceTrends(spaces);

        ParkingInfo.toJSONAndView(info, mapTopic);

        // Note above is is three phases
        // Ingest -> Analyse -> Action (to "dashboard")
        /*---------------------------------------------*/

        /*
         * Second, execute it by submitting to a StreamsContext.
         */

        Map<String, Object> config = new HashMap<>();
        // config.put(ContextProperties.TRACING_LEVEL, TraceLevel.TRACE);

        StreamsContextFactory.getStreamsContext(Type.DISTRIBUTED)
                .submit(topology, config).get();
    }

    // Aggregate occupancy over 15 minutes, calculating average
    // fill and trend. Then join with the parking location stream.
    public static TStream<ParkingInfo> monitorSpaceTrends(TStream<JSONObject> spaces) {
        
        // ParkingLocation is the id, address, lat/long etc.
        TStream<ParkingLocation> locations = ParkingLocation.toParkingLocation(spaces);
        
        // ParkingOccupancy is the id and available space information
        TStream<ParkingOccupancy> occs = ParkingOccupancy.toParkingOccupancy(spaces);
              
        // Create a partitioned window by the OSPID.
        TWindow<ParkingOccupancy, String> last15mins = occs.last(15, TimeUnit.MINUTES).key(ParkingOccupancy::getOspid);
        
        // ParkingFill is the id and an aggregation of information over the last 15 mins
        TStream<ParkingFill> fillRates = last15mins
                .aggregate(occupancies -> new ParkingFill().aggregate(occupancies));
        
        
        // ParkingInfo is just a ParkingFill and ParkingLocation
        TStream<ParkingInfo> parkingInfo = fillRates.joinLast(
                ParkingFill::getOspid,
                locations,
                ParkingLocation::getOspid,
                (fill, location) -> location == null ? null : new ParkingInfo(location,fill));

        return parkingInfo;
    }

}
