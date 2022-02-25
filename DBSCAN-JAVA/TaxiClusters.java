import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Aditya Kandel
 *         Student number : 300190020
 * 
 */
public class TaxiClusters {
  private ArrayList<TripRecord> db = new ArrayList<>();
  private ArrayList<Cluster> clusterList = new ArrayList<>();

  /**
   * 
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void readFile(String fileName) throws FileNotFoundException, IOException {
    BufferedReader bf = new BufferedReader(new FileReader(fileName));
    String row = "";
    int i = 0;
    boolean headerRead = false;
    while ((row = bf.readLine()) != null) {
      if (headerRead) {
        String[] data = row.split(",");
        GPScoord pickUp = new GPScoord(
            Double.parseDouble(data[8]),
            Double.parseDouble(data[9]));
        GPScoord dropOff = new GPScoord(
            Double.parseDouble(data[12]),
            Double.parseDouble(data[12]));

        String dateTime = data[4];
        double distance = Double.parseDouble(data[7]);
        TripRecord newTripRecord = new TripRecord("undefined", pickUp, dropOff, dateTime, distance); // gather data
        i++;
        db.add(newTripRecord);
      } else {
        headerRead = true;
      }
    }
    bf.close();
  }

  /**
   * Reference use for this algo is Wikipedia but its divided in to two methods,
   * https://en.wikipedia.org/wiki/DBSCAN
   * its easier to read.
   * 
   * @param eps
   * @param minPts
   */
  public void DBSCAN(double eps, int minPts) {
    int c = 0;
    System.out.println("db size " + db.size());
    for (int i = 0; i < db.size(); i++) {
      if (!db.get(i).getType().equals("undefined")) { // check if its visited
        continue;
      }
      ArrayList<TripRecord> N = RangeQuery(db, db.get(i), eps);
      if (N.size() < minPts) { // if if noise
        db.get(i).setType("noise");
        continue;
      }
      // new Cluster
      Cluster cluster = new Cluster();
      db.get(i).setType("core");
     // cluster.add(db.get(i));
      ArrayList<TripRecord> seed = N;

      // Expand neighbors
      cluster = expandNeighBors(seed, cluster, minPts, eps);
       clusterList.add(cluster);
    }
    System.err.println("Num of cluster " + clusterList.size() + " " + c);
  }

  /**
   * expand the neighbors using the initial neighbors list
   * 
   * @param seed
   * @param cluster
   * @param minPts
   * @param eps
   * @return The cluster of all the neighbors that belong inside it
   */
  public Cluster expandNeighBors(ArrayList<TripRecord> seed, Cluster cluster, int minPts, double eps) {
    for (int j = 0; j < seed.size(); j++) {

      // turn noise to boarder and add to the cluster
      if (seed.get(j).getType().equals("noise")) {
        seed.get(j).setType("border");
        cluster.add(seed.get(j));
      }
      // if label is undefined then skip to next point
      if (!seed.get(j).getType().equals("undefined")) {
        continue;
      }
      seed.get(j).setType("core");
      ArrayList<TripRecord> L = new ArrayList<>();
      cluster.add(seed.get(j));
      L = RangeQuery(db, seed.get(j), eps);
      if (L.size() >= minPts) { // merge two lists
        seed = merge(seed, L);
      }
    }
    return cluster;
  }

  /**
   * look for points inside the dataset that belong inside a given Q point;
   * 
   * @param db  dataset
   * @param Q   given point Q
   * @param eps epsilon value
   * @return return list of point inside the radius of point Q
   */
  public ArrayList<TripRecord> RangeQuery(ArrayList<TripRecord> db, TripRecord Q, double eps) {
    ArrayList<TripRecord> newNeighbours = new ArrayList<>();
    for (int i = 0; i < db.size(); i++) {
      if (distFunc(Q, db.get(i)) <= eps && Q != db.get(i)) { // check if its belongs in the neighbor
        newNeighbours.add(db.get(i));
      }
    }
    return newNeighbours;
  }

  /**
   * merge two array list while removing duplicate;
   * 
   * @param l1 list 1
   * @param l2 list 2
   * @return the merged Arraylist of l1 and l2
   */
  public ArrayList<TripRecord> merge(ArrayList<TripRecord> l1, ArrayList<TripRecord> l2) {
    Set<TripRecord> set = new LinkedHashSet<>(l1);
    set.addAll(l2);
    return new ArrayList<>(set);
  }

  /**
   * calculate the euclidean distance of two point
   * 
   * @param q point 1
   * @param p point 2
   * @return
   */
  public double distFunc(TripRecord q, TripRecord p) {
    double ac = q.getPickup_Location().getLanPoint() - p.getPickup_Location().getLanPoint();
    double cb = q.getPickup_Location().getLonPoint() - p.getPickup_Location().getLonPoint();
    return (double) Math.sqrt(ac * ac + cb * cb);
  }

  /**
   * write result of the dbscan to a csv file
   * 
   * @throws IOException
   */
  public void writeToCvs() throws IOException {
    FileWriter fw = new FileWriter("output.csv");
    BufferedWriter bf = new BufferedWriter(fw);
    PrintWriter pw = new PrintWriter(bf);
    String[] header = new String[] {
        "cluster",
        "startLat",
        "startLon",
        "number of points"
    };
    pw.println(Arrays.toString(header).replace("[", "").replace("]", ""));
    int total = 0;
    for (int i = 0; i < clusterList.size(); i++) {
      pw.println(
          (i + 1) +
              "," +
              clusterList.get(i).AverageLan() +
              "," +
              clusterList.get(i).AverageLon() +
              "," +
              clusterList.get(i).getNeighborsList().size());
      total += clusterList.get(i).getNeighborsList().size();
    }
    System.err.println("Total points:" + total);

    pw.flush();
    pw.close();
  }

  public static void main(String[] args) {
    TaxiClusters main = new TaxiClusters();
    if (args.length == 3) {
      try {
        System.err.println(Arrays.toString(args));

        main.readFile(args[0]);
        main.DBSCAN(Double.parseDouble(args[1]), Integer.parseInt(args[2]));
        main.writeToCvs();
      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }
    } else {
      System.out.println("Running default values");
      try {
        main.readFile("yellow_tripdata_2009-01-15_1hour_clean.csv");
        main.DBSCAN(0.0001, 5);
        main.writeToCvs();
      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }
    }
  }
}
