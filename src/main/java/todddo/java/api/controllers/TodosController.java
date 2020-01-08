package todddo.java.api.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.validation.constraints.Positive;
import org.mapstruct.Mapping;
import todddo.java.api.models.ErrorResponse;
import todddo.java.api.models.ResponseBase;
import todddo.java.api.models.TodoData;
import todddo.java.api.models.TodoResponse;
import todddo.java.domain.models.Todo;
import todddo.java.domain.services.TodosService;

@Controller("/todos")
public class TodosController {

  private final TodosService todosService;
  private final Mapper mapper;

  /**
   * We need to inject the mapper in order for Graal native images to work.
   *
   * <p>Runtime explosion occurs if we try to use the canonical Mappers.getMapper.
   */
  public TodosController(@Nonnull TodosService todosService, @Nonnull Mapper mapper) {
    this.todosService = todosService;
    this.mapper = mapper;
  }

  /**
   * Creates a single Todo.
   *
   * @param createReqObsv the Task with which to create a Todo
   * @return the created Todo
   */
  @Post
  @ApiResponse(
      responseCode = "201",
      description = "Task created.",
      content = @Content(schema = @Schema(implementation = TodoResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Invalid Task.",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public Single<HttpResponse<ResponseBase>> createTodo(final @Body Single<TodoData> createReqObsv) {
    return createReqObsv.flatMap(
        createReq ->
            todosService
                .create(mapper.apiToDomainTask(createReq))
                .map(
                    result ->
                        result.either(
                            e -> {
                              switch (e) {
                                case EmptyTask:
                                  return emptyTask(createReq.getTask());
                                default:
                                  return impossible;
                              }
                            },
                            t -> HttpResponse.created(mapper.domainToApi(t)))));
  }

  /**
   * Retrieves all existing Todos.
   *
   * @return a list of Todos.
   */
  @Get
  @ApiResponse(
      responseCode = "200",
      description = "Tasks retrieved",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = TodoResponse.class))))
  public Single<HttpResponse<List<TodoResponse>>> listTodos() {
    return todosService
        .list()
        .map(
            domainTodos ->
                HttpResponse.ok(
                    domainTodos.stream().map(mapper::domainToApi).collect(Collectors.toList())));
  }

  /**
   * Get a single Todo by Id.
   *
   * @param id to get the Todo by
   * @return retrieved Todo
   */
  @Get("/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Task retrieved.",
      content = @Content(schema = @Schema(implementation = TodoResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "No Todo by the given Id.",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public Single<HttpResponse<ResponseBase>> getTodo(final @Positive long id) {
    return todosService
        .get(Todo.Id.builder().value(id).build())
        .map(
            result ->
                result.either(
                    e -> {
                      switch (e) {
                        case NoSuchTodo:
                          return noSuchTodo(id);
                        default:
                          return impossible;
                      }
                    },
                    t -> HttpResponse.ok(mapper.domainToApi(t))));
  }

  /**
   * Delete a single Todo by Id.
   *
   * @param id to delete the Todo by
   * @return deleted Todo
   */
  @Delete("/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Task deleted.",
      content = @Content(schema = @Schema(implementation = TodoResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "No Todo by the given Id.",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "No Todo by the given Id.")
  public Single<HttpResponse<ResponseBase>> deleteTodo(final @Positive long id) {
    return todosService
        .delete(Todo.Id.builder().value(id).build())
        .map(
            result ->
                result.either(
                    e -> {
                      switch (e) {
                        case NoSuchTodo:
                          return noSuchTodo(id);
                        default:
                          return impossible;
                      }
                    },
                    t -> HttpResponse.ok(mapper.domainToApi(t))));
  }

  /**
   * Updates a single Todo by Id.
   *
   * @param id to update a Todo by
   * @param data holding the updated Task
   * @return an updated Task
   */
  @Put("/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Task updated.",
      content = @Content(schema = @Schema(implementation = TodoResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Invalid Task.",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "No Todo by the given id.",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public Single<HttpResponse<ResponseBase>> updateTodo(
      final @Positive long id, final @Body TodoData data) {
    return todosService
        .update(mapper.apiToDomainTodo(Todo.Id.builder().value(id).build(), data))
        .map(
            result ->
                result.either(
                    e -> {
                      switch (e) {
                        case NoSuchTodo:
                          return noSuchTodo(id);
                        case EmptyTask:
                          return emptyTask(data.getTask());
                        default:
                          return impossible;
                      }
                    },
                    t -> HttpResponse.ok(mapper.domainToApi(t))));
  }

  @org.mapstruct.Mapper(
      componentModel =
          "jsr330" /* Needed in order to generate an injectable singleton, runtime death if absent*/)
  interface Mapper {

    @Mapping(source = "data.task", target = "task.value")
    Todo apiToDomainTodo(Todo.Id id, TodoData data);

    @Mapping(source = "request.task", target = "value")
    Todo.Task apiToDomainTask(TodoData request);

    @Mapping(source = "todo.id.value", target = "id")
    @Mapping(source = "todo.task.value", target = "task")
    TodoResponse domainToApi(Todo todo);
  }

  private HttpResponse<ResponseBase> noSuchTodo(long id) {
    return HttpResponse.notFound(
        ErrorResponse.builder().message("No todo with id [" + id + "]").build());
  }

  private HttpResponse<ResponseBase> emptyTask(String task) {
    return HttpResponse.badRequest(
        ErrorResponse.builder()
            .message("The task was empty ['" + task + "']. You can't be *that* lazy.")
            .build());
  }

  private final HttpResponse<ResponseBase> impossible =
      HttpResponse.serverError(
          ErrorResponse.builder()
              .message("The impossible happened. Go buy a lottery ticket")
              .build());
}
