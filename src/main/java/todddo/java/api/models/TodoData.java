package todddo.java.api.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Resist the temptation to remove any of these annotations. This is the bare minimum for allowing
 * Jackson and Lombok to work with Graal Native.
 */
@Value
@Builder(builderClassName = "TodoDataBuilder")
@JsonDeserialize(builder = TodoData.TodoDataBuilder.class)
@Introspected
public class TodoData {

  @NonNull String task;

  @JsonPOJOBuilder(withPrefix = "")
  @Introspected
  public static class TodoDataBuilder {
    public TodoDataBuilder() {}
  }
}
