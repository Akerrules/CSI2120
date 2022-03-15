dbscan build in golang, by partitioning the data points and finding their cluster


Time with different consumer threads

n=2 c=4, 24.7228313s of 198194 points
n=4 c=4, 8.7003807s of 206102 points
n=4 c=10, 8.4949452s of 206102 points
n=10 c=4, 1.9074995s of 222488 points
n=10,c=10, 1.7336998s of 222488 points
n=10, c=50, 2.0301367s of 222488 points
n=20, c=10,1.3857624s of 255300 points
n=20, c=50, 1.3763585s of 255300 points
n=20, c=200, 1.3187238s of 255300 points

The conclusion based on the result was that the time didn't change much when I increased the consumer threads, so there seems to be a good point in which increasing the threads doesn't help much