package todddo.java.api.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(builderClassName = "ErrorResponseBuilder")
@JsonDeserialize(builder = ErrorResponse.ErrorResponseBuilder.class)
@Introspected
public class ErrorResponse extends ResponseBase {

  @NonNull String message;

  @JsonPOJOBuilder(withPrefix = "")
  @Introspected
  public static class ErrorResponseBuilder {
    public ErrorResponseBuilder() {}
  }
}
