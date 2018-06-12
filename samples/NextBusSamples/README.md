# NextBus samples


Sample applications in this project demonstrate using this toolkit to retrieve and display NextBus data on a map.
The `MapVehiclePosition` application is a separate application.

### Hangout demo samples

The samples in the `com.ibm.streamsx.transportation.hangout.demo` namespace also use the `Hangout` operator in the Geospatial toolkit to detect idle buses and compute some statistics with that data.
They are discussed in [this article on Streamsdev](https://developer.ibm.com/streamsdev/docs/common-patterns-tracking-moving-objects-streams-part-1/).

#### Running the samples
Launch `SimpleNextBusHangout` first to connect to NextBus and publish a stream of  detected hangouts. This exported stream which will be used by the other 2 sample applications.
These applications use live NextBus data, meaning that if no buses are idle, you might not get any hangouts detected even after leaving it running for a while.
The defaults are to report a hangout after a bus is in the same 75x75m area for 3 minutes or more.
If you are running one of the samples in that namespace and notice that you do not get any hangouts detected, try increasing the cell size or reducing the minimum dwell time.
Note that this would probably no longer meet the definition of "idle", but it would help  you see some results for demonstration purposes.



### Geofence demo samples

The samples in the `com.ibm.streamsx.transportation.geofence.demo` namespace  use the `Geofence` operator in the Geospatial toolkit to perform geofencing with moving buses.
There are 2 kinds of geofences defined in CSV files in the `poi` and `fences` folders:
* Circular geofences: comprised of a point, its id and the  distance in meters from that point to be considered as within the fence. Also includes the message to send to any bus in the fence. These are defined in the `poi` folder.
	* Add a new fence by adding a line to poi.csv of the form:
	  `poi_name,latitude,longitude,radius,message`
* Polygon geofences: a WKT polygon covering an area, the id of the geofence, and the update action (1 = add this fence to the Geofence operator, 0 = remove the fence.  Also includes the message to send to any bus in the fence.  These are in the `fences` folder. 
	* Add a new fence by adding a line to fences.txt of the form:
	  `geofence name,1,polygon wkt,message`
#### Running the applications
	  
Launch either of the 2 geofencing applications first.  The circular geofence application is called `CircularGeofencing`, and the polygon based geofencing using the Geofence operator is in `GeofencingWithPolygons`.

Then you can launch the data source applications:

`PublishBusLocation`: this connects to NextBus and exports the stream of bus data.
`GeofenceSource`: this exports a stream of geofences and the alerts. Tip: Wait until all the other applications are running first before launching this one since it is using a `FileSource` and only submits a few tuples.
Check the console output of the `AlertPrinter` operator for output.