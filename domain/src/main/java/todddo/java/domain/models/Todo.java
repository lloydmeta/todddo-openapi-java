package todddo.java.domain.models;

import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Todo {

  @Value
  @Builder
  public static class Task {
    @NonNull String value;
  }

  @NonNull Id id;
  @NonNull Task task;

  @Value
  @Builder
  public static class Id {
    long value;
  }

  public interface Repo {

    @Nonnull
    Single<Todo> create(@Nonnull final Task task);

    @Nonnull
    Maybe<Todo> get(@Nonnull final Id id);

    @Nonnull
    Maybe<Todo> delete(@Nonnull final Id id);

    @Nonnull
    Single<List<Todo>> list();

    @Nonnull
    Maybe<Todo> update(@Nonnull final Todo todo);
  }
}
