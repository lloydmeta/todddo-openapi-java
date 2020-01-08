package todddo.java.domain.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TodoSpec {

  @Test
  public void buildThrowsWhenNull() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          Todo.builder().id(null).task(null).build();
        });
  }
}
