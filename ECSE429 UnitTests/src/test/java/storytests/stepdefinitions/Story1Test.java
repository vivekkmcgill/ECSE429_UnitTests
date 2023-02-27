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
@SelectClasspathResource("story1")
public class Story1Test {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {
    // This is just to assert the server is running properly
    todosClient = HttpClient.newBuilder().build();
  }

  @Given("the server is running")
  public void testGivenServerRunning() {

    todosClient = HttpClient.newBuilder().build();

    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Given("TODOs are present with the following fields")
  public void testGivenTodosPresentWithFollowingFields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    HttpResponse response = TodosPoster.postIfNotPresent("Add submit button", "false", "Add a button to submit form");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }
  @When("a project manager creates a task with a project associated to it in the tasksof array")
  @Then("the coresponding project is added to the todo's tasksof list")
  public void testWhenProjectManagerCreatesTaskWithProjectAssociated() throws IOException, InterruptedException {
    // Post todos and associate to a project with id 1
    HttpResponse response = TodosPoster.postWithProjectIfNotPresent("Add name field", "true", "Add a name field to form", "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

    JSONObject todo = TodosPoster.getTodoByName("Add name field");
    JSONArray tasksof = todo.getJSONArray("tasksof");
    assertEquals("1", tasksof.getJSONObject(0).getString("id"));
  }

  @When("a project manager associates a TODO with a project")
  @Then("the coresponding project is associated to the todo's tasksof list")
  public void testWhenProjectManagerAssociatesTodoWithProject() throws IOException, InterruptedException {
    // Test that a project association is created when an existing task is updated instead of created
    // First get the existing task id
    JSONObject todo = TodosPoster.getTodoByName("Add submit button");
    String id = todo.getString("id");

    // Now create the association
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/" + id + "/tasksof"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"id\": \"1\"" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, HttpResponse.BodyHandlers.ofString());

    // Assert the post was successful
    assertEquals(201, response.statusCode());

    // Now get the project object and assert that it contains the new association
    // Using a helper method in a separate class
    assertTrue(TodosPoster.projectHasTodo("Add submit button"));
  }

  @When("a project manager tries to associate a TODO with a non-extsient or deleted project")
  @Then("user receives an error and no associations are added to the todo's tasksof list")
  public void user_receives_an_error_and_no_associations_are_added_to_the_todo_s_tasksof_list() throws IOException, InterruptedException {
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/1/tasksof"))
        .POST(HttpRequest.BodyPublishers.ofString("{ \"id\": \"1234\"}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, HttpResponse.BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(404, response.statusCode());

    // Now check that no associations were  added to the todos unintentionally
    JSONObject test = TodosPoster.getTodoByName("scan paperwork");
    assertEquals(1, test.getJSONArray("tasksof").length());
  }
}
