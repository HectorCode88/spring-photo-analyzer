package pe.aws.photo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AnalyzePhotos {

  public PhotoCompareResponse compareTwoFaces(Float similarityThreshold,
                                              byte[] bytesSourceImage, byte[] bytesTargetImage) {
    log.info("start compareTwoFaces");
    try {

      var region = Region.US_EAST_1;
      var rekognitionClient = RekognitionClient.builder()
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .region(region)
        .build();

      var sourceBytes = SdkBytes.fromByteArray(bytesSourceImage);
      var targetBytes = SdkBytes.fromByteArray(bytesTargetImage);
      // Create an Image object for the source image.
      Image souImage = Image.builder()
        .bytes(sourceBytes)
        .build();

      Image tarImage = Image.builder()
        .bytes(targetBytes)
        .build();

      CompareFacesRequest facesRequest = CompareFacesRequest.builder()
        .sourceImage(souImage)
        .targetImage(tarImage)
        .similarityThreshold(similarityThreshold)
        .build();

      // Compare the two images.
      CompareFacesResponse compareFacesResult = rekognitionClient.compareFaces(facesRequest);
      var faceDetails = compareFacesResult.faceMatches();
      for (CompareFacesMatch match: faceDetails){
        ComparedFace face= match.face();
        BoundingBox position = face.boundingBox();
        System.out.println("Face at " + position.left().toString()
          + " " + position.top()
          + " matches with " + face.confidence().toString()
          + "% confidence.");

      }
      List<ComparedFace> uncompared = compareFacesResult.unmatchedFaces();
      System.out.println("There was " + uncompared.size() + " face(s) that did not match");
      System.out.println("Source image rotation: " + compareFacesResult.sourceImageOrientationCorrection());
      System.out.println("target image rotation: " + compareFacesResult.targetImageOrientationCorrection());
      log.info("end compareTwoFaces");
      return PhotoCompareResponse.builder()
        .countFacesNotMatch(String.valueOf(uncompared.size()))
        .sourceImageRotation("" + compareFacesResult.sourceImageOrientationCorrection())
        .targetImageRotation("" + compareFacesResult.targetImageOrientationCorrection())
        .build();

    } catch(RekognitionException e) {
      log.info("error compareTwoFaces");
      System.out.println("Failed to Rekognition image -> " + e.getMessage());
    }
    log.info("null compareTwoFaces");
    return null;
  }

  public ArrayList<WorkItem> detectLabels(byte[] bytes, String key) {
    log.info("start detectLabels");
    var region = Region.US_EAST_1;
    var rekognitionClient = RekognitionClient.builder()
      .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
      .region(region)
      .build();

    try {

      SdkBytes sourceBytes = SdkBytes.fromByteArray(bytes);

      // Create an Image object for the source image.
      var souImage = Image.builder()
        .bytes(sourceBytes)
        .build();

      var detectLabelsRequest = DetectLabelsRequest.builder()
        .image(souImage)
        .maxLabels(10)
        .build();

      var labelsResponse = rekognitionClient.detectLabels(detectLabelsRequest);

      // Write the results to a WorkItem instance.
      var labels = labelsResponse.labels();

      System.out.println("Detected labels for the given photo");
      var list = new ArrayList<WorkItem>();
      WorkItem item ;
      for (Label label: labels) {
        item = new WorkItem();
        item.setKey(key); // identifies the photo
        item.setConfidence(label.confidence().toString());
        item.setName(label.name());
        list.add(item);
      }
      log.info("end detectLabels");
      return list;

    } catch (RekognitionException e) {
      log.info("error detectLabels");
      System.out.println(e.getMessage());
      System.exit(1);
    }
    log.info("null detectLabels");
    return null ;
  }
}
