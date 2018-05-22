# NextBus samples


Sample applications in this project demonstrate using this toolkit to retreive and display NextBus data on a map.
The `MapVehiclePosition` application is a separate application.

### Hangout demo samples
The samples in the `com.ibm.streamsx.transportation.hangout.demo` namespace also use the Hangout operator in the Geospatial toolkit to detect idle buses and compute some statistics with that data.
To run those samples, first launch `SimpleNextBusHangout` to connect to NextBus and publish a stream of  detected hangouts, which will be used by the other 2 sample applications.
These applications use live NextBus data, meaning that you might not get any hangouts detected even after leaving it running for a while.
The defaults are to report a hangout after a bus is in the same 75x75m area for 3 minutes or more.
If you are running one of the samples in that namespace and notice that you do not get any hangouts detected, try increasing the cell size or reducing the minimum dwell time.
Note that this would probably no longer meet the definition of "idle", but it would help  you see some results for demonstration purposes.

The hangout related samples will be discussed in a future article on Streamsdev.
