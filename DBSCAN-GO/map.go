// Project CSI2120/CSI2520
// Winter 2022
// Robert Laganiere, uottawa.ca

package main

import (
	"encoding/csv"
	"fmt"
	"io"
	"math"
	"os"
	"runtime"
	"strconv"
	"sync"
	"time"
)

type GPScoord struct {
	lat  float64
	long float64
}

type LabelledGPScoord struct {
	GPScoord
	ID    int // point ID
	Label int // cluster ID
}

type job struct{
	coords []LabelledGPScoord
	offset int
	eps float64
	minpts int

}
const N int = 2
const MinPts int = 5
const eps float64 = 0.0003
const filename string = "yellow_tripdata_2009-01-15_9h_21h_clean.csv"

func main() {
	

    start := time.Now(); 

    gps, minPt, maxPt := readCSVFile(filename)
	fmt.Printf("Number of points: %d\n", len(gps))
	
	minPt = GPScoord{40.7, -74.}
	maxPt = GPScoord{40.8, -73.93}
	
	// geographical limits
	fmt.Printf("SW:(%f , %f)\n", minPt.lat, minPt.long)
	fmt.Printf("NE:(%f , %f) \n\n", maxPt.lat, maxPt.long)
	
	// Parallel DBSCAN STEP 1.
	incx := (maxPt.long-minPt.long)/float64(N)
	incy := (maxPt.lat-minPt.lat)/float64(N)
	
	var grid [N][N][]LabelledGPScoord  // a grid of GPScoord slices
	
	// Create the partition
	// triple loop! not very efficient, but easier to understand
	
	partitionSize:=0
    for j:=0; j<N; j++ {
        for i:=0; i<N; i++ {
		
		    for _, pt := range gps {
			
			    // is it inside the expanded grid cell
			    if (pt.long >= minPt.long+float64(i)*incx-eps) && (pt.long < minPt.long+float64(i+1)*incx+eps) && (pt.lat >= minPt.lat+float64(j)*incy-eps) && (pt.lat < minPt.lat+float64(j+1)*incy+eps) {
				
                    grid[i][j]= append(grid[i][j], pt) // add the point to this slide
					partitionSize++;
                }				
			}
	    }
	}
	
	// ***
	// This is the non-concurrent procedural version
	// It should be replaced by a producer thread that produces jobs (partition to be clustered)
	// And by consumer threads that clusters partitions
    // for j:=0; j<N; j++ {
    //     for i:=0; i<N; i++ {
		
	// 	    DBscan(grid[i][j], MinPts, eps, i*10000000+j*1000000)
	// 	}
	// }
	// Parallel DBSCAN STEP 2.
	// Apply DBSCAN on each partition
	// ...
	//DBscan(grid[0][0], MinPts, eps, 0*10000000+0*1000000)
// channel for sending jobs
	//here a job is a simple int
	
	jobs := make(chan job)
	
	numConsumer := 0
	var mutex sync.WaitGroup
	mutex.Add(numConsumer) 
	
	// for j := 0; j < N; j++ {
	// 	for i := 0; i < N; i++ {
	// 		jobs <-job{grid[i][j],i*10000000+j*1000000, eps, MinPts}
	// 	}
	// }
	go producer(jobs, grid)
    // start one consumer
	for j := 0; j < numConsumer; j++ {
			go consomme(jobs, &mutex)
	}

	// producer
	
//	 close(jobs)

    // wait for consumers to terminate
	mutex.Wait()


	// Parallel DBSCAN step 3.
	// merge clusters
	// *DO NOT PROGRAM THIS STEP

	end := time.Now();
    fmt.Printf("\nExecution time: %s of %d points\n", end.Sub(start), partitionSize)
    fmt.Printf("Number of CPUs: %d", runtime.NumCPU())
}
func consomme(jobs chan job, done *sync.WaitGroup) {
	for {	
		job, more := <-jobs

		if more {
			DBscan(job.coords, job.minpts,job.eps,job.offset)
		} else {
			done.Done()
			return
		}
	}
}
// Applies DBSCAN algorithm on LabelledGPScoord points
// LabelledGPScoord: the slice of LabelledGPScoord points
// MinPts, eps: parameters for the DBSCAN algorithm
// offset: label of first cluster (also used to identify the cluster)
// returns number of clusters found

func producer(ch chan job, grid [N][N][]LabelledGPScoord){
	for j := 0; j < N; j++ {
		for i := 0; i < N; i++ {
			jobs := job{grid[i][j],i*10000000+j*1000000, eps, MinPts}
			ch<-jobs
		}
		close(ch)
	}
}
func DBscan(coords []LabelledGPScoord, MinPts int, eps float64, offset int) (nclusters int) {

	//ACTUAL CODE STARTS HERE
	nclusters = 0
	for n:=0; n<len(coords);n++{
		if coords[n].Label != 0 {
			continue
		}
		var N= rangeQuery(coords, coords[n], eps)
		if len(N) < MinPts {
			coords[n].Label = -1
			continue
		}
		nclusters++
		coords[n].Label = offset + nclusters
		var seed =  N
		for k:=0; k<len(seed);k++{
			if 	seed[k].Label == -1 {
				seed[k].Label = offset + nclusters
			}
			if 	seed[k].Label != 0 {
				continue
			}
			seed[k].Label = offset + nclusters
 			var L = rangeQuery(coords,*seed[k], eps)
			if len(L) >= MinPts {
				for i := 0; i < len(L); i++ {
					seed = append(seed,L[i])
				}
			}			
		}
	}
	// End of DBscan function
	// Printing the result (do not remove)
	fmt.Printf("Partition %10d : [%4d,%6d]\n", offset, nclusters, len(coords))
	return nclusters
}


func rangeQuery(coords []LabelledGPScoord, curCoord LabelledGPScoord, eps float64) []*LabelledGPScoord{
	var N = make([]*LabelledGPScoord,0,5000)
	for i:=0; i<len(coords);i++{
		if curCoord.ID != coords[i].ID && distfunc(curCoord.GPScoord, coords[i].GPScoord)<= eps {
			N = append(N,&coords[i])
		}
	}
	return N
}

func distfunc(q GPScoord, p GPScoord) float64 {
	var ac float64 = q.lat - p.lat
	var cd float64 = q.long - p.long
	return math.Sqrt((ac * ac) + (cd * cd))

}





// reads a csv file of trip records and returns a slice of the LabelledGPScoord of the pickup locations
// and the minimum and maximum GPS coordinates
func readCSVFile(filename string) (coords []LabelledGPScoord, minPt GPScoord, maxPt GPScoord) {

    coords= make([]LabelledGPScoord, 0, 5000)

    // open csv file
    src, err := os.Open(filename)
	defer src.Close()
    if err != nil {
        panic("File not found...")
    }
	
	// read and skip first line
    r := csv.NewReader(src)
    record, err := r.Read()
    if err != nil {
        panic("Empty file...")
    }

    minPt.long = 1000000.
    minPt.lat = 1000000.
    maxPt.long = -1000000.
    maxPt.lat = -1000000.
	
	var n int=0
	
    for {
        // read line
        record, err = r.Read()

        // end of file?
        if err == io.EOF {
            break
        }

        if err != nil {
             panic("Invalid file format...")
        }
		
		// get lattitude
		lat, err := strconv.ParseFloat(record[9], 64)
        if err != nil {
             panic("Data format error (lat)...")
        }

        // is corner point?
		if lat>maxPt.lat {
		    maxPt.lat= lat
		}		
		if lat<minPt.lat {
		    minPt.lat= lat
		}
		
		// get longitude
		long, err := strconv.ParseFloat(record[8], 64)
        if err != nil {
             panic("Data format error (long)...")
        }
		
        // is corner point?
		if long>maxPt.long {
		    maxPt.long= long
		}
		
		if long<minPt.long {
		    minPt.long= long
		}

        // add point to the slice
		n++
        pt:= GPScoord{lat,long}
        coords= append(coords, LabelledGPScoord{pt,n,0})
    }

    return coords, minPt,maxPt
}
