/**
 * @author Aditya Kandel
 *         Student number : 300190020
 * 
 */
public class TripRecord {

    private String dateTime;
    private GPScoord pickup_Location, dropoff_Location;
    private double tripDistance;
    private String type;

    public TripRecord(String s, GPScoord pickup, GPScoord dropOff, String dateTime, double distance) {
        type = s;
        pickup_Location = pickup;
        dropoff_Location = dropOff;
        this.dateTime = dateTime;
        tripDistance = distance;

    }

    /**
     * 
     * @return pickup_Location
     */
    public GPScoord getPickup_Location() {
        return pickup_Location;
    }

    /**
     * 
     * @return dropoff_Location
     */
    public GPScoord getDropOff_Location() {
        return dropoff_Location;
    }

    /**
     * set tripDistance to parameter 'd'
     * 
     * @param d
     */
    public void setTripDistance(double d) {
        tripDistance = d;
    }

    /**
     * set dateTime to parameter 's'
     * 
     * @param s
     */
    public void setPickup_DateTime(String s) {
        dateTime = s;
    }

    /**
     * set pickup_Location to parameter 'gps'
     * 
     * @param gps
     */
    public void setPickup_Location(GPScoord gps) {
        pickup_Location = gps;
    }

    /**
     * 
     * set dropoff_Location to parameter 'gps'
     * 
     * @param gps
     */
    public void setDropoff_Location(GPScoord gps) {
        dropoff_Location = gps;
    }

    /**
     * set type to parameter 's'
     * 
     * @param s
     */
    public void setType(String s) {
        type = s;
    }

    /**
     * 
     * @return type
     */
    public String getType() {
        return type;
    }

}
