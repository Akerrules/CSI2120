/**
 * @author Aditya Kandel
 *         Student number : 300190020
 * 
 */
public class GPScoord {

    private double lan;
    private double lon;

    public GPScoord(double lan, double lon) {
        this.lan = lan;
        this.lon = lon;
    }

    /**
     * 
     * @return latitude
     */
    public double getLanPoint() {
        return lan;
    }

    /**
     * 
     * @return longitude
     */
    public double getLonPoint() {
        return lon;
    }

    /**
     * set latitude
     * 
     * @param point
     */
    public void setLanPoint(double point) {
        lan = point;
    }

    /**
     * set longitude
     * 
     * @param point
     */
    public void setLonPoint(double point) {
        lon = point;

    }
}
