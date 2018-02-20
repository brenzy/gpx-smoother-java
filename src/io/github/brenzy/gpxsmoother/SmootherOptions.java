package io.github.brenzy.gpxsmoother;

import java.io.File;

public class SmootherOptions {

   boolean isValid = true;
   String inputFileName;
   String outputFileName;
   int numPoints = 0;
   int elevate = 0;
   int slopeChange = 0;
   int minSlope = 0;
   int maxSlope = 0;

   // The output file name is inputFileName_smoothed.extension.
   // If the output file already exists a version number is added.
   private void initializeOutputFileName() {
      if (this.inputFileName == null) {
         return;
      }
      int extensionPos = this.inputFileName.lastIndexOf('.');
      String extension = this.inputFileName.substring(extensionPos + 1);
      String baseFilename = this.inputFileName.substring(0, extensionPos) + "_smoothed";
      String outputFileName = baseFilename + "." + extension;
      // Make sure the output file does not already exist
      File fOutput = new File(outputFileName);
      int counter = 1;
      while (fOutput.exists()) {
         outputFileName = baseFilename + "_" + (counter++) + "." + extension;
         fOutput = new File(outputFileName);
      }
      this.outputFileName = outputFileName;
   }

   public SmootherOptions() {
   }

   public void parse(String[] args) {
      int i = 0;
      int numArgs = args.length;
      while (i < numArgs - 1) {
         String parameter = args[i];
         String value = args[i + 1];
         switch (parameter) {
         case "-i":
            this.inputFileName = value;
            this.initializeOutputFileName();
            break;
         case "-n":
            this.numPoints = Integer.parseInt(value);
            break;
         case "-e":
            this.elevate = Integer.parseInt(value);
            break;
         case "-s":
            this.slopeChange = Integer.parseInt(value);
            break;
         case "-r":
            String[] split = value.split(",");
            if (split.length != 2) {
               System.err.println("A range should contain comma separated values with no spaces");
            }
            this.minSlope = Integer.parseInt(split[0]);
            this.maxSlope = Integer.parseInt(split[1]);
            break;
         default:
            this.isValid = false;
            break;
         }
         if (!this.isValid) {
            break;
         }
         i += 2;
      }

      if (this.inputFileName == null) {
         this.isValid = false;
      }

      if (!this.isValid) {
         this.displayUsage();
      }
   }

   public void displayUsage() {
      if (this.inputFileName == null) {
         System.err.println("Please provide an input file path.");
      }

      System.err.println("GpxSmoother Usage:");
      System.err.println("-i <inputFilePath.gpx>");
      System.err.println("-n <number of points to smooth over (odd), suggested value = 5>");
      System.err.println("-e <meters to shift elevation up or down>");
      System.err.println("-s <maximum change in slope between points>");
      System.err.println("-r <min change in slope between points>,<max change in slope between points>");
   }

}
