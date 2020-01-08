package todddo.java.api.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(builderClassName = "TodoResponseBuilder")
@JsonDeserialize(builder = TodoResponse.TodoResponseBuilder.class)
@Introspected
public class TodoResponse extends ResponseBase {

  long id;

  @NonNull String task;

  @JsonPOJOBuilder(withPrefix = "")
  @Introspected
  public static class TodoResponseBuilder {
    public TodoResponseBuilder() {}
  }
}
