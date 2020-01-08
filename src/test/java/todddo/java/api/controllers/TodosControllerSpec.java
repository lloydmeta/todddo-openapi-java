package todddo.java.api.controllers;

import fj.data.Either;
import io.micronaut.core.type.Argument;
import io.reactivex.Single;
import java.util.List;
import org.fest.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import todddo.java.api.models.ErrorResponse;
import todddo.java.api.models.TodoData;
import todddo.java.api.models.TodoResponse;
import todddo.java.domain.models.Todo;
import todddo.java.domain.services.TodosService;

public class TodosControllerSpec {
  private TodosController.Mapper mapper = Mappers.getMapper(TodosController.Mapper.class);

  private static TodosService mockService() {
    return Mockito.mock(TodosService.class);
  }

  private static Todo dummy =
      Todo.builder()
          .id(Todo.Id.builder().value(1).build())
          .task(Todo.Task.builder().value("stuff").build())
          .build();

  @Test
  void testCreateTodoEmptyErrFromService() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.create(Mockito.any()))
        .thenReturn(Single.just(Either.left(TodosService.CreateError.EmptyTask)));
    final TodosController subject = new TodosController(mockService, mapper);
    final ErrorResponse err =
        subject
            .createTodo(Single.just(TodoData.builder().task("").build()))
            .blockingGet()
            .getBody(ErrorResponse.class)
            .get();
    Assertions.assertTrue(err.getMessage().contains("lazy"));
    Mockito.verify(mockService, Mockito.times(1)).create(Mockito.any());
  }

  @Test
  void testCreateTodoOkFromService() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.create(Mockito.any()))
        .thenAnswer(
            invocation ->
                Single.just(
                    Either.right(
                        Todo.builder()
                            .id(Todo.Id.builder().value(1).build())
                            .task(invocation.getArgument(0))
                            .build())));
    final TodosController subject = new TodosController(mockService, mapper);
    final String taskStr = "lala";
    final TodoResponse resp =
        subject
            .createTodo(Single.just(TodoData.builder().task(taskStr).build()))
            .blockingGet()
            .getBody(TodoResponse.class)
            .get();
    Assertions.assertEquals(resp.getId(), 1);
    Assertions.assertEquals(resp.getTask(), taskStr);
    Mockito.verify(mockService, Mockito.times(1)).create(Mockito.any());
  }

  @Test
  void testListTodos() {
    final TodosService mockService = mockService();
    final List<Todo> expectedListResult = Collections.list(dummy);
    Mockito.when(mockService.list()).thenReturn(Single.just(expectedListResult));
    final TodosController subject = new TodosController(mockService, mapper);
    @SuppressWarnings("unchecked")
    final List<TodoResponse> resp =
        subject
            .listTodos()
            .blockingGet()
            .getBody(Argument.of(List.class, TodoResponse.class))
            .get();
    Assertions.assertEquals(resp.get(0).getId(), expectedListResult.get(0).getId().getValue());
    Assertions.assertEquals(resp.get(0).getTask(), expectedListResult.get(0).getTask().getValue());
    Mockito.verify(mockService, Mockito.times(1)).list();
  }

  @Test
  void testGetTodoNonExistent() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.get(Mockito.any()))
        .thenReturn(Single.just(Either.left(TodosService.GetError.NoSuchTodo)));
    final TodosController subject = new TodosController(mockService, mapper);
    final ErrorResponse err = subject.getTodo(1).blockingGet().getBody(ErrorResponse.class).get();
    Assertions.assertTrue(err.getMessage().contains("No todo"));
    Mockito.verify(mockService, Mockito.times(1)).get(Mockito.any());
  }

  @Test
  void testGetTodoExistent() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.get(Mockito.any())).thenReturn(Single.just(Either.right(dummy)));
    final TodosController subject = new TodosController(mockService, mapper);
    final TodoResponse resp = subject.getTodo(1).blockingGet().getBody(TodoResponse.class).get();
    Assertions.assertEquals(resp.getTask(), dummy.getTask().getValue());
    Mockito.verify(mockService, Mockito.times(1)).get(Mockito.any());
  }

  @Test
  void testDeleteTodoNonExistent() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.delete(Mockito.any()))
        .thenReturn(Single.just(Either.left(TodosService.DeleteError.NoSuchTodo)));
    final TodosController subject = new TodosController(mockService, mapper);
    final ErrorResponse err =
        subject.deleteTodo(1).blockingGet().getBody(ErrorResponse.class).get();
    Assertions.assertTrue(err.getMessage().contains("No todo"));
    Mockito.verify(mockService, Mockito.times(1)).delete(Mockito.any());
  }

  @Test
  void testDeleteTodoExistent() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.delete(Mockito.any())).thenReturn(Single.just(Either.right(dummy)));
    final TodosController subject = new TodosController(mockService, mapper);
    final TodoResponse resp = subject.deleteTodo(1).blockingGet().getBody(TodoResponse.class).get();
    Assertions.assertEquals(resp.getTask(), dummy.getTask().getValue());
    Mockito.verify(mockService, Mockito.times(1)).delete(Mockito.any());
  }

  @Test
  void UpdateTodoNonEmpty() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.update(Mockito.any()))
        .thenReturn(Single.just(Either.left(TodosService.UpdateError.EmptyTask)));
    final TodosController subject = new TodosController(mockService, mapper);
    final ErrorResponse err =
        subject
            .updateTodo(1, TodoData.builder().task("eh").build())
            .blockingGet()
            .getBody(ErrorResponse.class)
            .get();
    Assertions.assertTrue(err.getMessage().contains("lazy"));
    Mockito.verify(mockService, Mockito.times(1)).update(Mockito.any());
  }

  @Test
  void UpdateTodoNonExistent() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.update(Mockito.any()))
        .thenReturn(Single.just(Either.left(TodosService.UpdateError.NoSuchTodo)));
    final TodosController subject = new TodosController(mockService, mapper);
    final ErrorResponse err =
        subject
            .updateTodo(1, TodoData.builder().task("eh").build())
            .blockingGet()
            .getBody(ErrorResponse.class)
            .get();
    Assertions.assertTrue(err.getMessage().contains("No todo"));
    Mockito.verify(mockService, Mockito.times(1)).update(Mockito.any());
  }

  @Test
  void testUpdateTodoExistent() {
    final TodosService mockService = mockService();
    Mockito.when(mockService.update(Mockito.any())).thenReturn(Single.just(Either.right(dummy)));
    final TodosController subject = new TodosController(mockService, mapper);
    final TodoResponse resp =
        subject
            .updateTodo(1, TodoData.builder().task("eh").build())
            .blockingGet()
            .getBody(TodoResponse.class)
            .get();
    Assertions.assertEquals(resp.getTask(), dummy.getTask().getValue());
    Mockito.verify(mockService, Mockito.times(1)).update(Mockito.any());
  }
}
