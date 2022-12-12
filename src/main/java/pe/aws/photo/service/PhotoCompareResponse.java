package pe.aws.photo.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PhotoCompareResponse {

  private String countFacesNotMatch;
  private String sourceImageRotation;
  private String targetImageRotation;
}
