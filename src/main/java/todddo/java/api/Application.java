package todddo.java.api;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(title = "todddo-openapi-java", version = "0.1", description = "Todos API"))
public class Application {

  public static void main(String[] args) {
    Micronaut.run(Application.class);
  }
}
