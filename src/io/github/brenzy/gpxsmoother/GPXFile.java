package io.github.brenzy.gpxsmoother;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class GPXFile {

   String inputFileName;
   ArrayList<TrackPoint> rawValues = new ArrayList<TrackPoint>();
   Document gpxDocument;

   public GPXFile(String inputFileName) {
      this.inputFileName = inputFileName;
   }

   public boolean loadFile() {

      try {

         File fXmlFile = new File(this.inputFileName);
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         this.gpxDocument = dBuilder.parse(fXmlFile);

         this.gpxDocument.getDocumentElement().normalize();

         NodeList trkptNodes = this.gpxDocument.getElementsByTagName("trkpt");
         TrackPoint previous = null;
         for (int iPoint = 0; iPoint < trkptNodes.getLength(); iPoint++) {

            Node trkptNode = trkptNodes.item(iPoint);
            if (trkptNode.getNodeType() == Node.ELEMENT_NODE) {
               Element trkpElement = (Element) trkptNode;
               String latitude = trkpElement.getAttribute("lat");
               String longitude = trkpElement.getAttribute("lon");

               NodeList elevationtNodes = trkpElement.getElementsByTagName("ele");
               if (elevationtNodes.getLength() > 0) {
                  Element elevationElement = (Element) elevationtNodes.item(0);
                  String elevation = elevationElement.getTextContent();
                  TrackPoint point = new TrackPoint(Double.parseDouble(latitude), Double.parseDouble(longitude),
                        Double.parseDouble(elevation));
                  if (previous != null) {
                     point.setDistance(previous);
                  }
                  this.rawValues.add(point);
                  previous = point;
               }
            }
         }
      } catch (java.io.FileNotFoundException eNotFound) {
         System.err.println("File " + this.inputFileName + " not found.");
         return false;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
      return true;
   }

   public void smooth(int smoothingPoints) {
      int dataLength = this.rawValues.size();
      if (dataLength == 0) {
         return;
      }

      int smoothingSize = (int) Math.floor(smoothingPoints / 2.0);
      if (smoothingSize < 2 || smoothingSize > dataLength / 2) {
         smoothingSize = 2;
      }

      TrackPoint previous = null;
      for (int i = 0; i < dataLength; i++) {
         double sumValues = 0;
         int start = i - smoothingSize;
         if (start < 0) {
            start = 0;
         }
         int end = i + smoothingSize;
         if (end > dataLength - 1) {
            end = dataLength - 1;
         }
         for (int j = start; j <= end; j++) {
            sumValues += this.rawValues.get(j).getElevation();
         }
         TrackPoint point = this.rawValues.get(i);
         point.setNewElevation(sumValues / (end - start + 1));
         if (previous != null) {
            point.setNewSlope(previous);
         }
         previous = point;
      }
   }

   public void flatten(int maxDelta) {
      int dataLength = this.rawValues.size();
      if (dataLength == 0) {
         return;
      }

      double dMaxDelta = Math.abs(maxDelta) / 100;
      TrackPoint previous = null;
      for (int i = 0; i < dataLength; i++) {
         TrackPoint point = this.rawValues.get(i);
         if (previous != null) {
            double deltaSlope = point.getNewSlope() - previous.getNewSlope();
            if (Math.abs(deltaSlope) > dMaxDelta) {
               if (deltaSlope > 0) {
                  point.setNewSlope(previous.getNewSlope() + maxDelta, previous);
               } else if (deltaSlope < 0) {
                  point.setNewSlope(previous.getNewSlope() - maxDelta, previous);
               }
            }
         }
         previous = point;
      }
   }

   public void setRange(int minSlope, int maxSlope) {
      int dataLength = this.rawValues.size();
      if (dataLength == 0) {
         return;
      }

      double dMaxSlope = minSlope / 100.0;
      double dMinSlope = maxSlope / 100.0;
      TrackPoint previous = null;
      for (int i = 0; i < dataLength; i++) {
         TrackPoint point = this.rawValues.get(i);
         if (previous != null) {
            double newSlope = point.getNewSlope();
            if (newSlope > dMaxSlope) {
               point.setNewSlope(dMaxSlope, previous);
            } else if (newSlope < dMinSlope) {
               point.setNewSlope(dMinSlope, previous);
            }
         }
         previous = point;
      }
   }

   public void elevate(int numMetres) {
      int dataLength = this.rawValues.size();
      if (dataLength == 0) {
         return;
      }

      TrackPoint previous = null;
      for (int i = 0; i < dataLength; i++) {
         TrackPoint point = this.rawValues.get(i);
         point.setNewElevation(point.getElevation() + numMetres);
         if (previous != null) {
            point.setNewSlope(previous);
         }
         previous = point;
      }
   }

   public void saveAs(String outputFileName) {

      NumberFormat formatter = new DecimalFormat("##.##");
      NodeList trkptNodes = this.gpxDocument.getElementsByTagName("trkpt");
      int numValues = Math.min(trkptNodes.getLength(), this.rawValues.size());
      for (int iPoint = 0; iPoint < numValues; iPoint++) {
         TrackPoint point = this.rawValues.get(iPoint);
         Node trkptNode = trkptNodes.item(iPoint);
         if (trkptNode.getNodeType() == Node.ELEMENT_NODE) {
            Element trkpElement = (Element) trkptNode;
            NodeList elevationtNodes = trkpElement.getElementsByTagName("ele");
            if (elevationtNodes.getLength() > 0) {
               Element elevationElement = (Element) elevationtNodes.item(0);
               String elevation = formatter.format(point.getNewElevation());
               elevationElement.setTextContent(elevation);
            }
         }
      }

      Transformer transformer;
      try {
         transformer = TransformerFactory.newInstance().newTransformer();
         System.out.println("Saving smoothed gpx track to " + outputFileName);
         Result output = new StreamResult(new File(outputFileName));
         Source input = new DOMSource(this.gpxDocument);
         transformer.transform(input, output);
      } catch (TransformerConfigurationException e) {
         e.printStackTrace();
      } catch (TransformerFactoryConfigurationError e) {
         e.printStackTrace();
      } catch (TransformerException e) {
         e.printStackTrace();
      }
   }

}
