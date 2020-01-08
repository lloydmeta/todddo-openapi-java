package todddo.java.domain.services;

import fj.data.Either;
import io.reactivex.Single;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import todddo.java.domain.models.Todo;

@Singleton
public class TodosService {

  private final Todo.Repo repo;

  public TodosService(@Nonnull Todo.Repo repo) {
    this.repo = repo;
  }

  @Nonnull
  public Single<Either<CreateError, Todo>> create(@Nonnull final Todo.Task task) {
    if (task.getValue().trim().isEmpty()) {
      return Single.just(Either.left(CreateError.EmptyTask));
    } else {
      return repo.create(task).map(Either::right);
    }
  }

  @Nonnull
  public Single<Either<GetError, Todo>> get(@Nonnull final Todo.Id id) {
    return repo.get(id)
        .map(Either::<GetError, Todo>right)
        .switchIfEmpty(Single.just(Either.left(GetError.NoSuchTodo)));
  }

  @Nonnull
  public Single<Either<DeleteError, Todo>> delete(@Nonnull final Todo.Id id) {
    return repo.delete(id)
        .map(Either::<DeleteError, Todo>right)
        .switchIfEmpty(Single.just(Either.left(DeleteError.NoSuchTodo)));
  }

  @Nonnull
  public Single<List<Todo>> list() {
    return repo.list();
  }

  @Nonnull
  public Single<Either<UpdateError, Todo>> update(@Nonnull final Todo todo) {
    if (todo.getTask().getValue().trim().isEmpty()) {
      return Single.just(Either.left(UpdateError.EmptyTask));
    } else {
      return repo.update(todo)
          .map(Either::<UpdateError, Todo>right)
          .switchIfEmpty(Single.just(Either.left(UpdateError.NoSuchTodo)));
    }
  }

  interface Error {}

  public enum CreateError implements Error {
    EmptyTask
  }

  public enum GetError implements Error {
    NoSuchTodo
  }

  public enum DeleteError implements Error {
    NoSuchTodo
  }

  public enum UpdateError implements Error {
    EmptyTask,
    NoSuchTodo
  }
}
