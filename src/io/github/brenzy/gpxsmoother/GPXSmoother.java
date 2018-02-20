package io.github.brenzy.gpxsmoother;
public class GPXSmoother {

   public static void main(String[] args) throws Exception {

      SmootherOptions options = new SmootherOptions();
      options.parse(args);
      
      if (!options.isValid) {
         System.exit(1);
      }
      
      System.out.println("Loading: " + options.inputFileName);
      GPXFile gpxFile = new GPXFile(options.inputFileName);
      if (gpxFile.loadFile()) {
         if (options.numPoints > 0) {
            System.out.println("Smoothing over " + options.numPoints + " points.");
            gpxFile.smooth(options.numPoints);
         }
         if (options.elevate != 0) {
            System.out.println("Shifting the elevation by " + options.elevate + " metres.");
            gpxFile.elevate(options.elevate);
         }
         if (options.slopeChange != 0) {
            System.out.println("Setting the maximum change In slope between points " + options.slopeChange);
            gpxFile.flatten(options.slopeChange);
         }
         if (options.minSlope != 0 && options.maxSlope != 0) {
            System.out.println("Setting the slope change between " + options.minSlope + " and " + options.maxSlope );
            gpxFile.flatten(options.slopeChange);
         }
         gpxFile.saveAs(options.outputFileName);
      } else {
         System.err.println("Error loading input file");
      };
      
      System.out.println("Done...");
      
   }

}