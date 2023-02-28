package storytests.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
@SelectClasspathResource("story5")
public class Story5Test {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {
    // This is just to assert the server is running properly
    todosClient = HttpClient.newBuilder().build();
  }

  @Given("the server is running....")
  public void testGivenServerRunning() {

    todosClient = HttpClient.newBuilder().build();

    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Given("TODOs are present with the following fields....")
  public void testGivenTodosPresentWithFollowingFields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    HttpResponse response = TodosPoster.postWithProjectIfNotPresent(
        "Add submit button",
        "false",
        "Add a big green button to submit form",
        "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
    response = TodosPoster.postWithProjectIfNotPresent(
        "Design search bar",
        "true",
        "Design the search bar",
        "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }

  @When("a project manager changes the doneStatus for a task to true to mark it as done")
  @Then("the task has its doneStatus field updated to true, and associations remain the same")
  public void testNormalFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Add submit button");
    String id = todoToDisassociate.getString("id");

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/" + id))
        .PUT(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"Add submit button\"," +
            "    \"doneStatus\": true" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    JSONObject putResponse = new JSONObject(response.body());
    JSONObject updatedTodo = TodosPoster.getTodoByName("Add submit button");

    // Check that the proper json is returned after it is updated
    assertEquals(updatedTodo.getString("title"), putResponse.getString("title"));
    assertEquals(updatedTodo.getString("doneStatus"), putResponse.getString("doneStatus"));
  }

  @When("a project manager changes the doneStatus for a task to false to re-open it")
  @Then("the task has its doneStatus field updated to false, and associations remain the same")
  public void testAlternateFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Design search bar");
    if (!todoToDisassociate.isEmpty()) {
      String id = todoToDisassociate.getString("id");

      HttpRequest todosPutRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL + "/" + id))
          .PUT(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"Design search bar\"," +
              "    \"doneStatus\": false" +
              "}"))
          .build();
      HttpResponse<String> response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());

      assertEquals(200, response.statusCode());
      JSONObject putResponse = new JSONObject(response.body());
      JSONObject updatedTodo = TodosPoster.getTodoByName("Design search bar");

      // Check that the proper json is returned after it is updated
      assertEquals(updatedTodo.getString("title"), putResponse.getString("title"));
      assertEquals(updatedTodo.getString("doneStatus"), putResponse.getString("doneStatus"));
    }
  }

  @When("a project manager tries to mark a todo named title as done, but provides a non-true\\/false status")
  @Then("user receives an error and no changes are done to the todo's doneStatus")
  public void testErrorFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Add submit button");
    String id = todoToDisassociate.getString("id");

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/" + id))
        .PUT(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"Add submit button\"," +
            "    \"doneStatus\": \"completed\"," +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(400, response.statusCode());
  }
}
