package pe.aws.photo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(path = "/aws-analyzer")
public class PhotoController {

  @Autowired
  S3Service s3Client;

  @Autowired
  AnalyzePhotos photos;

  // Generates a report that analyzes photos in a given bucket.
  @GetMapping(path = "/photo", produces = "application/json")
  public List<List<WorkItem>> report() {
    log.info("start report");
    var bucketName = "apptesis";
    // Get a list of key names in the given bucket.
    return s3Client.ListBucketObjects(bucketName).stream()
      //.limit(3)
      .map(key -> Pair.of(key, s3Client.getObjectBytes (bucketName, key)))
      .map(pair -> photos.detectLabels(pair.right(), pair.left()))
      .collect(Collectors.toList());
  }

  @GetMapping(path = "/photo/compare/{sourceImage}/{targetImage}", produces = "application/json")
  public PhotoCompareResponse compare(@PathVariable String sourceImage, @PathVariable String targetImage) {
    log.info("start report");
    var bucketName = "apptesis";
    var similarityThreshold = 70F;
    // Get a list of key names in the given bucket.
    return Stream.of(Pair.of(s3Client.getObjectBytes(bucketName, sourceImage),
        s3Client.getObjectBytes(bucketName, targetImage)))
      .peek(x -> log.info("source: " + Arrays.toString(x.left())))
      .peek(x -> log.info("source: " + Arrays.toString(x.right())))
      .map(pair -> photos.compareTwoFaces(similarityThreshold, pair.left(), pair.right()))
      .findAny().get();
  }

}
