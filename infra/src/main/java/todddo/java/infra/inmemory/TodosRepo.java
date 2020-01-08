package todddo.java.infra.inmemory;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.mapstruct.Mapping;
import todddo.java.domain.models.Todo;

@Singleton
public class TodosRepo implements Todo.Repo {

  private final Mapper mapper;
  private final AtomicLong idGenerator = new AtomicLong(1L);

  public TodosRepo(@Nonnull final Mapper mapper) {
    this.mapper = mapper;
  }

  @org.mapstruct.Mapper(
      componentModel =
          "jsr330" /* Needed in order to generate an injectable singleton, runtime death if absent*/)
  interface Mapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "persistedTodo.task", target = "task")
    Todo persistedToDomainTodo(Todo.Id id, PersistedTodo persistedTodo);

    @Mapping(source = "task", target = "task")
    PersistedTodo domainTaskToPersisted(Todo.Task task);
  }

  @Value
  @Builder
  static class PersistedTodo {

    @NonNull Task task;

    @Value
    @Builder
    static class Task {
      @NonNull String value;
    }
  }

  private ConcurrentHashMap<Todo.Id, PersistedTodo> map = new ConcurrentHashMap<>();

  @Override
  @Nonnull
  public Single<Todo> create(@Nonnull final Todo.Task task) {
    return Single.fromCallable(
            () -> {
              final PersistedTodo persistable = mapper.domainTaskToPersisted(task);
              final Todo.Id id = Todo.Id.builder().value(idGenerator.getAndIncrement()).build();
              map.put(id, persistable);
              return mapper.persistedToDomainTodo(id, persistable);
            })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation());
  }

  @Nonnull
  @Override
  public Maybe<Todo> get(@Nonnull final Todo.Id id) {
    return Maybe.fromCallable(
            () ->
                Optional.ofNullable(map.get(id))
                    .map(
                        persisted -> {
                          return mapper.persistedToDomainTodo(id, persisted);
                        })
                    .orElse(null))
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation());
  }

  @Nonnull
  @Override
  public Maybe<Todo> delete(@Nonnull final Todo.Id id) {
    return Maybe.fromCallable(
            () ->
                Optional.ofNullable(map.remove(id))
                    .map(persisted -> mapper.persistedToDomainTodo(id, persisted))
                    .orElse(null))
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation());
  }

  @Nonnull
  @Override
  public Single<List<Todo>> list() {
    return Single.fromCallable(
            () ->
                map.entrySet().stream()
                    .map(entry -> mapper.persistedToDomainTodo(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparingLong(t -> t.getId().getValue()))
                    .collect(Collectors.toList()))
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation());
  }

  @Nonnull
  @Override
  public Maybe<Todo> update(@Nonnull final Todo todo) {

    return Maybe.fromCallable(
            () -> {
              final PersistedTodo p = mapper.domainTaskToPersisted(todo.getTask());
              return Optional.ofNullable(map.replace(todo.getId(), p))
                  .map(ignoredPreviousValue -> todo)
                  .orElse(null);
            })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation());
  }
}
