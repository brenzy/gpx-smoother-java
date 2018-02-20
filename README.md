# gpx-smoother-java
Command line GPX smoother written in Java.

This is a quick port of the GPX smoother from a javascript-based web application to a stand alone java application.

Currently a simple box smoothing algorithm is used.  Hopefully, the Java version of the smoother will allow for quick experimentation with some more complex smoothing algorithms.

To compile and run:
javac -d bin src\io\github\brenzy\gpxsmoother\*.java
java -cp bin io.github.brenzy.gpxsmoother.GPXSmoother -i data/test.gpx -n 5

Usage:
-i <inputFilePath.gpx>
-n <number of points to smooth over (odd), suggested value = 5>
-e <meters to shift elevation up or down>
-s <maximum change in slope between points>
-r <min change in slope between points>,<max change in slope between points>

The smoothed file is saved to <inputFilePath_smoothed.gpx>.
