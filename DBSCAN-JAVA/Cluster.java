import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Aditya Kandel
 *         Student number : 300190020
 * 
 */
public class Cluster {

    protected HashSet<TripRecord> neighbors = new HashSet<>();

    /**
     * set the neighbors list
     * 
     * @param list
     */
    public void setNeighborsList(ArrayList<TripRecord> list) {
        neighbors.clear();
        neighbors.addAll(list);
    }

    /**
     * add to neighbors list
     * 
     * @param p
     */
    public void add(TripRecord p) {
        neighbors.add(p);
    }

    /**
     * 
     * @return hashset of neighborsList
     */
    public HashSet<TripRecord> getNeighborsList() {
        return neighbors;
    }

    /**
     * 
     * @return the average of latitude
     */
    public double AverageLan() {
        double total = 0.0;
        for (TripRecord tripRecord : neighbors) {

            total += tripRecord.getPickup_Location().getLanPoint();
        }
        return total / neighbors.size();
    }

    /**
     * 
     * @return the average of longitude
     */
    public double AverageLon() {
        double total = 0.0;
        for (TripRecord tripRecord : neighbors) {
            total += tripRecord.getPickup_Location().getLonPoint();
        }
        return total / neighbors.size();
    }

}
