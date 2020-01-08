package todddo.java.domain.services;

import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import todddo.java.domain.models.Todo;

public class TodosServiceSpec {

  private static Todo.Repo mockRepo() {
    return Mockito.mock(Todo.Repo.class);
  }

  @Test
  public void emptyTaskFailsCreate() {
    final Todo.Repo repo = mockRepo();
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(
        service.create(Todo.Task.builder().value("").build()).blockingGet().left().value(),
        TodosService.CreateError.EmptyTask);
    Mockito.verifyNoInteractions(repo);
  }

  @Test
  public void nonEmptyTaskSuccessfullyCreates() {
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.create(Mockito.any()))
        .thenAnswer(
            invocation ->
                Single.just(
                    Todo.builder()
                        .id(Todo.Id.builder().value(1).build())
                        .task(invocation.getArgument(0))
                        .build()));
    final TodosService service = new TodosService(repo);
    final Todo.Task arg = Todo.Task.builder().value("do somefing").build();
    final Todo expected = Todo.builder().id(Todo.Id.builder().value(1).build()).task(arg).build();

    Assertions.assertEquals(service.create(arg).blockingGet().right().value(), expected);
    Mockito.verify(repo, Mockito.times(1)).create(arg);
  }

  @Test
  public void getByUnsavedIdFails() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.get(Mockito.eq(id))).thenAnswer(invocation -> Maybe.empty());
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(
        service.get(id).blockingGet().left().value(), TodosService.GetError.NoSuchTodo);
    Mockito.verify(repo, Mockito.times(1)).get(id);
  }

  @Test
  public void getBySavedIdSucceeds() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Task task = Todo.Task.builder().value("do somefing").build();
    final Todo expected = Todo.builder().id(id).task(task).build();
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.get(Mockito.eq(id))).thenAnswer(invocation -> Maybe.just(expected));
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(service.get(id).blockingGet().right().value(), expected);
    Mockito.verify(repo, Mockito.times(1)).get(id);
  }

  @Test
  public void deleteByUnsavedIdFails() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.delete(Mockito.eq(id))).thenAnswer(invocation -> Maybe.empty());
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(
        service.delete(id).blockingGet().left().value(), TodosService.DeleteError.NoSuchTodo);
    Mockito.verify(repo, Mockito.times(1)).delete(id);
  }

  @Test
  public void deleteBySavedIdSucceeds() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Task task = Todo.Task.builder().value("do somefing").build();
    final Todo expected = Todo.builder().id(id).task(task).build();
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.delete(Mockito.eq(id))).thenAnswer(invocation -> Maybe.just(expected));
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(service.delete(id).blockingGet().right().value(), expected);
    Mockito.verify(repo, Mockito.times(1)).delete(id);
  }

  @Test
  public void listReturnsAllSavedTodos() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Task task = Todo.Task.builder().value("do somefing").build();
    final Todo expected = Todo.builder().id(id).task(task).build();
    final List<Todo> expectedList = Collections.singletonList(expected);
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.list()).thenReturn(Single.just(expectedList));
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(service.list().blockingGet(), expectedList);
    Mockito.verify(repo, Mockito.times(1)).list();
  }

  @Test
  public void updatewithEmptyTaskFails() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Task task = Todo.Task.builder().value("").build();
    final Todo expected = Todo.builder().id(id).task(task).build();
    final Todo.Repo repo = mockRepo();
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(
        service.update(expected).blockingGet().left().value(), TodosService.UpdateError.EmptyTask);
    Mockito.verifyNoInteractions(repo);
  }

  @Test
  public void updateByUnsavedIdFails() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Task task = Todo.Task.builder().value("do somefing").build();
    final Todo expected = Todo.builder().id(id).task(task).build();
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.update(Mockito.eq(expected))).thenAnswer(invocation -> Maybe.empty());
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(
        service.update(expected).blockingGet().left().value(), TodosService.UpdateError.NoSuchTodo);
    Mockito.verify(repo, Mockito.times(1)).update(expected);
  }

  @Test
  public void updateBySavedIdSucceeds() {
    final Todo.Id id = Todo.Id.builder().value(1).build();
    final Todo.Task task = Todo.Task.builder().value("do somefing").build();
    final Todo expected = Todo.builder().id(id).task(task).build();
    final Todo.Repo repo = mockRepo();
    Mockito.when(repo.update(Mockito.eq(expected))).thenAnswer(invocation -> Maybe.just(expected));
    final TodosService service = new TodosService(repo);
    Assertions.assertEquals(service.update(expected).blockingGet().right().value(), expected);
    Mockito.verify(repo, Mockito.times(1)).update(expected);
  }
}
