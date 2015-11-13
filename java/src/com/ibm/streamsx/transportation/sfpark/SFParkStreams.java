/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
 */
package com.ibm.streamsx.transportation.sfpark;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ibm.json.java.JSONObject;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.inet.HTTPStreams;
import com.ibm.streamsx.topology.json.JSONStreams;

// http://sfpark.org/
public class SFParkStreams {
	
	/**
	 * Publish a standard JSON stream containing parking spaces that
	 * report with availability under the topic:
	 * {@code "/streamsx/transportation/sfpark/availability"}.
	 */
	public static TStream<JSONObject> availibilityLocations(Topology topology, boolean publish) {
		TStream<JSONObject> spaces = locationsWithAvailability(sfparkSpaces(topology));
		if (publish)
			spaces.publish("/streamsx/transportation/sfpark/availability");
		return spaces;
	}

    // Declare a stream of JSONObject, each representing
    // a parking space within 1.5 miles of the
    // IBM Spark Technology Center
    public static TStream<JSONObject> sfparkSpaces(Topology topology) {

        // Available parking within 5 miles of the the center of SF
        String url = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat=37.773972&long=-122.431297&radius=5.0&uom=mile&response=json";

        TStream<JSONObject> availability = HTTPStreams.getJSON(topology, url,
                10, TimeUnit.SECONDS);
        
        // Only if the response was successful
        availability = availability.filter(allSpaces->"SUCCESS".equals(allSpaces.get("STATUS")));
        
        // SF Park data comes back as a JSON object with multiple
        // available parking locations in it, in the key AVL.
        // Split each up into a single tuple using JSONStreams.flattenArray
        
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        // Convert the timestamp into number of milliseconds
        // since 1970, using the current time if there is no ts.
        availability = availability.modify(allSpaces-> {
            long ts;
            String auts = (String) allSpaces
                    .get("AVAILABILITY_UPDATED_TIMESTAMP");
            if (auts == null)
                ts = System.currentTimeMillis();
            else {
                try {
                    ts = sdf.parse(auts).getTime();
                } catch (Exception e) {
                    ts = System.currentTimeMillis();
                }
            }
            allSpaces.put("ts", ts);
            return allSpaces;
        });
        
        // Flatten the array, keeping the timestamp ts with each tuple.
        TStream<JSONObject> locations = JSONStreams.flattenArray(availability, "AVL", "ts");
        	       
        return locations;       
    }
    // Since December 2013 on-street parking no longer
    // reports availability!!!!!
    // So only allow through off street parking.
    public static TStream<JSONObject> locationsWithAvailability(TStream<JSONObject> locations) {
        return filterByType(locations, "OFF");
    }
    
    public static TStream<JSONObject> filterByType(TStream<JSONObject> locations, String ...types) {
        
        Set<String> uniqueTypes = new HashSet<>();
        uniqueTypes.addAll(Arrays.asList(types));

        TStream<JSONObject> spaces = locations.filter(tuple -> uniqueTypes.contains(tuple.get("TYPE")));

        return spaces;
    }
}
