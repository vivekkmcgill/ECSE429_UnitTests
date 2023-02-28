package storytests.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Suite
@SelectClasspathResource("story3")
public class Story3Test {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {
    // This is just to assert the server is running properly
    todosClient = HttpClient.newBuilder().build();
  }

  @Given("the server is running..")
  public void testGivenServerRunning() {

    todosClient = HttpClient.newBuilder().build();

    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Given("TODOs are present with the following fields.")
  public void testGivenTodosPresentWithFollowingFields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    HttpResponse response = TodosPoster
        .postWithProjectIfNotPresent(
            "Add submit button",
            "false",
            "Add a button to submit form",
            "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
    response = TodosPoster
        .postWithProjectIfNotPresent(
            "Design search bar",
            "false",
            "Design the search bar",
            "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }

  @When("a project manager removes a task from a project associated to it")
  @Then("the coresponding project is removed from the todo's tasksof list")
  public void testNormalFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Add submit button");
    System.out.println(todoToDisassociate.toString());
    String id = todoToDisassociate.getString("id");

    String getTodoTasksOfUrl = TODOS_BASE_URL + "/" + id + "/tasksof";

    // Before state - The todos with id has a tasksof association of id 1
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(getTodoTasksOfUrl)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, getResponse.statusCode());

    // Call helper method to remove task from project
    HttpResponse response = TodosPoster.dissasociateFromProject("1", id);

    // Now try to get the object to ensure it doesn't exist anymore
    HttpResponse<String> newGetResponse = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());
    JSONObject responseJson = new JSONObject(newGetResponse.body());
    JSONArray todos = responseJson.getJSONArray("projects");
    assertEquals(0, todos.length());
  }

  @When("a project manager deletes a TODO that is associated with a project")
  @Then("the coresponding project no longer has the todo in its tasks field")
  public void testAlternateFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Design search bar");
    String id = todoToDisassociate.getString("id");

    String getTodoTasksOfUrl = TODOS_BASE_URL + "/" + id + "/tasksof";

    // Before state - The todos with id has a tasksof association of id 1
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(getTodoTasksOfUrl)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, getResponse.statusCode());
    assertTrue(TodosPoster.projectHasTodo("Design search bar"));

    // Call helper method to remove task from project
    HttpResponse response = TodosPoster.deleteTodo(id);
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

    // Now try to get the object to ensure it doesn't exist anymore
    todosGetRequest = HttpRequest.newBuilder().uri(URI.create("http://localhost:4567/projects/1")).build();
    HttpResponse<String> newGetResponse = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());
    JSONObject responseJson = new JSONObject(newGetResponse.body());
    JSONArray projects = responseJson.getJSONArray("projects");
    JSONArray tasks = projects.getJSONObject(0).getJSONArray("tasks");
    assertFalse(TodosPoster.todoInProject(tasks, id));

  }

  @When("a project manager tries to remove a todo associated to a project, but forgets that it was already removed earlier")
  @Then("user receives an error and no changes are done to the project's tasks list")
  public void testErrorFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("scan paperwork");
    String id = todoToDisassociate.getString("id");

    String getTodoTasksOfUrl = TODOS_BASE_URL + "/" + id + "/tasksof";

    // Check that the project doesn't have the task
    assertFalse(TodosPoster.projectHasTodo(id));

    // Before state - The todos with id has a tasksof association of id 1
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(getTodoTasksOfUrl)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, getResponse.statusCode());
    assertTrue(TodosPoster.projectHasTodo("scan paperwork"));

    // Call helper method to remove task from project even though it isn't associated to it
    HttpResponse response = TodosPoster.dissasociateFromProject("1", id);
    assertTrue(response.statusCode() == 404);

    // Check that the project still doesn't have the task
    assertFalse(TodosPoster.projectHasTodo(id));
  }
}
