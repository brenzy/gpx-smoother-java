package io.github.brenzy.gpxsmoother;

public class TrackPoint {

   private Double latitude;
   private Double longitude;
   private Double elevation;
   private Double slope = 0.0;
   private Double distance = 0.0;

   private Double newElevation;
   private Double newSlope;

   TrackPoint(Double latitude, Double longitude, Double elevation) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.elevation = elevation;
      this.newElevation = elevation;
   }

   public void setDistance(TrackPoint previous) {
      this.distance = VincentyDistance.getDistance(previous.latitude, previous.longitude, this.latitude, this.latitude);
      this.slope = (this.elevation - previous.elevation) / this.distance;
      this.newSlope = this.slope;
   }

   public double getElevation() {
      return this.elevation;
   }

   public double getSlope() {
      return this.slope;
   }

   public void setNewElevation(double value) {
      this.newElevation = value;
   }

   public void setNewSlope(TrackPoint previous) {
      this.newSlope = (this.newElevation - previous.newElevation) / this.distance;
   }

   public void setNewSlope(double slope, TrackPoint previous) {
      this.newSlope = slope;
      this.newElevation = (slope * this.distance) + previous.newElevation;
   }

   public Double getNewElevation() {
      return this.newElevation;
   }

   public Double getNewSlope() {
      return this.newSlope;
   }

}
