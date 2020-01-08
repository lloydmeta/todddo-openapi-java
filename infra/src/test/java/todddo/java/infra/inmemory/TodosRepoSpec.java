package todddo.java.infra.inmemory;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import todddo.java.domain.models.Todo;

public class TodosRepoSpec {

  private TodosRepo.Mapper mapper = Mappers.getMapper(TodosRepo.Mapper.class);

  @Test
  public void testCreate() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Task task = Todo.Task.builder().value("do something").build();
    Assertions.assertEquals(subject.create(task).blockingGet().getTask(), task);
  }

  @Test
  public void testGetNonExistent() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Id id = Todo.Id.builder().value(100).build();
    Assertions.assertTrue(subject.get(id).isEmpty().blockingGet());
  }

  @Test
  public void testDGetExistent() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Task task = Todo.Task.builder().value("do something").build();
    final Todo created = subject.create(task).blockingGet();
    Assertions.assertEquals(subject.get(created.getId()).blockingGet(), created);
    Assertions.assertEquals(subject.get(created.getId()).blockingGet(), created);
  }

  @Test
  public void testDeleteNonExistent() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Id id = Todo.Id.builder().value(100).build();
    Assertions.assertTrue(subject.delete(id).isEmpty().blockingGet());
  }

  @Test
  public void testDeleteExistent() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Task task = Todo.Task.builder().value("do something").build();
    final Todo created = subject.create(task).blockingGet();
    Assertions.assertEquals(subject.delete(created.getId()).blockingGet(), created);
    Assertions.assertTrue(subject.delete(created.getId()).isEmpty().blockingGet());
  }

  @Test
  public void testList() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Task task = Todo.Task.builder().value("do something").build();
    final int times = 10;
    for (int i = 0; i < times; ++i) {
      subject.create(task).blockingGet();
    }
    List<Todo> retrieved = subject.list().blockingGet();
    Assertions.assertEquals(retrieved.size(), times);
    retrieved.forEach(
        i -> {
          Assertions.assertEquals(i.getTask(), task);
        });
  }

  @Test
  public void testUpdateNonExistent() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Id id = Todo.Id.builder().value(100).build();
    final Todo.Task task = Todo.Task.builder().value("meh").build();
    final Todo todo = Todo.builder().id(id).task(task).build();
    Assertions.assertTrue(subject.update(todo).isEmpty().blockingGet());
  }

  @Test
  public void testUpdateExistent() {
    final TodosRepo subject = new TodosRepo(mapper);
    final Todo.Id id = Todo.Id.builder().value(100).build();
    final Todo.Task task = Todo.Task.builder().value("meh").build();
    final Todo todo = subject.create(task).blockingGet();
    final Todo.Task updatedTask = Todo.Task.builder().value("meh 2").build();
    final Todo updated = Todo.builder().id(todo.getId()).task(updatedTask).build();

    Assertions.assertEquals(subject.update(updated).blockingGet(), updated);
    Assertions.assertEquals(subject.get(updated.getId()).blockingGet(), updated);
  }
}
