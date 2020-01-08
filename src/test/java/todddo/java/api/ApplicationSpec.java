package todddo.java.api;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import todddo.java.api.models.TodoData;
import todddo.java.api.models.TodoResponse;

@MicronautTest
public class ApplicationSpec {

  @Inject EmbeddedServer server;

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void testCreateEmptyTodo() {
    try {
      client
          .toBlocking()
          .exchange(
              HttpRequest.POST("/todos", TodoData.builder().task("   ").build()),
              TodoResponse.class);
    } catch (HttpClientResponseException e) {
      Assertions.assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      Assertions.assertTrue(e.getMessage().contains("lazy"));
    }
  }

  @Test
  void testCreateNonEmptyTodo() {
    final TodoResponse resp =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/todos", TodoData.builder().task("something").build()),
                TodoResponse.class);
    Assertions.assertEquals(resp.getTask(), "something");
  }

  @Test
  void testGetNonExistent() {
    try {
      client.toBlocking().retrieve(HttpRequest.GET("/todos/9999999999"), TodoResponse.class);
    } catch (HttpClientResponseException e) {
      Assertions.assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }
  }

  @Test
  void testGetExistent() {
    final TodoResponse created =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/todos", TodoData.builder().task("something").build()),
                TodoResponse.class);
    final TodoResponse retrieved =
        client
            .toBlocking()
            .retrieve(HttpRequest.GET("/todos/" + created.getId()), TodoResponse.class);
    Assertions.assertEquals(created, retrieved);
  }

  @Test
  void testDeleteNonExistent() {
    try {
      client.toBlocking().retrieve(HttpRequest.DELETE("/todos/9999999999"), TodoResponse.class);
    } catch (HttpClientResponseException e) {
      Assertions.assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }
  }

  @Test
  void testDeleteExistent() {
    final TodoResponse created =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/todos", TodoData.builder().task("something").build()),
                TodoResponse.class);
    final TodoResponse deleted =
        client
            .toBlocking()
            .retrieve(HttpRequest.DELETE("/todos/" + created.getId()), TodoResponse.class);
    Assertions.assertEquals(created, deleted);
    try {
      client
          .toBlocking()
          .retrieve(HttpRequest.DELETE("/todos/" + created.getId()), TodoResponse.class);
    } catch (HttpClientResponseException e) {
      Assertions.assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }
  }

  @Test
  void testUpdateBlank() {
    final TodoData data = TodoData.builder().task("").build();
    try {
      client.toBlocking().retrieve(HttpRequest.PUT("/todos/9999999999", data), TodoResponse.class);
    } catch (HttpClientResponseException e) {
      Assertions.assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
    }
  }

  @Test
  void testUpdateNonExistent() {
    final TodoData data = TodoData.builder().task("meh").build();
    try {
      client.toBlocking().retrieve(HttpRequest.PUT("/todos/9999999999", data), TodoResponse.class);
    } catch (HttpClientResponseException e) {
      Assertions.assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
    }
  }

  @Test
  void testUpdateExistent() {
    final TodoResponse created =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/todos", TodoData.builder().task("something").build()),
                TodoResponse.class);
    final TodoData data = TodoData.builder().task("meh").build();
    final TodoResponse updated =
        client
            .toBlocking()
            .retrieve(HttpRequest.PUT("/todos/" + created.getId(), data), TodoResponse.class);
    Assertions.assertEquals(updated.getTask(), data.getTask());
  }

  @Test
  void testList() {
    final int times = 10;
    for (int i = 0; i < times; ++i) {
      client
          .toBlocking()
          .retrieve(
              HttpRequest.POST("/todos", TodoData.builder().task("something").build()),
              TodoResponse.class);
    }
    @SuppressWarnings("unchecked")
    final List<TodoResponse> list =
        client
            .toBlocking()
            .retrieve(HttpRequest.GET("/todos"), Argument.of(List.class, TodoResponse.class));
    Assertions.assertTrue(list.size() >= times);
  }

  @Test
  void getSwaggerUI() {
    final HttpResponse<String> r = client.toBlocking().exchange("/swagger-ui", String.class);
    Assertions.assertEquals(r.getStatus(), HttpStatus.OK);
  }
}
