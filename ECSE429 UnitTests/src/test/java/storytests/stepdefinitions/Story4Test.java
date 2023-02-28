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
@SelectClasspathResource("story4")
public class Story4Test {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {
    // This is just to assert the server is running properly
    todosClient = HttpClient.newBuilder().build();
  }

  @Given("the server is running...")
  public void testGivenServerRunning() {

    todosClient = HttpClient.newBuilder().build();

    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Given("TODOs are present with the following fields...")
  public void testGivenTodosPresentWithFollowingFields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    HttpResponse response = TodosPoster
        .postWithProjectIfNotPresent(
            "Add submit button",
            "false",
            "Add a big green button to submit form",
            "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
    response = TodosPoster.postWithProjectIfNotPresent(
        "Design search bar",
        "false",
        "Design the search bar",
        "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }

  @When("a project manager changes the description or requirements for a task")
  @Then("the coresponding task has its description field updated, and associations remain the same")
  public void testNormalFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Add submit button");
    String id = todoToDisassociate.getString("id");

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/" + id))
        .PUT(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"Add submit button\"," +
            "    \"description\": \"Add a small red button to submit form\"" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    JSONObject putResponse = new JSONObject(response.body());
    JSONObject updatedTodo = TodosPoster.getTodoByName("Add submit button");

    // Check that the proper json is returned after it is updated
    assertEquals(updatedTodo.getString("title"), putResponse.getString("title"));
    assertEquals(updatedTodo.getString("description"), putResponse.getString("description"));
  }

  @When("a project manager changes the name for a task")
  @Then("the coresponding task has its name field updated, and associations remain the same")
  public void testAlternateFlow() throws IOException, InterruptedException {
    JSONObject todoToDisassociate = TodosPoster.getTodoByName("Design search bar");
    if (!todoToDisassociate.isEmpty()) {
      String id = todoToDisassociate.getString("id");

      HttpRequest todosPutRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL + "/" + id))
          .PUT(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"Design search bar with suggestions\"" +
              "}"))
          .build();
      HttpResponse<String> response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());

      assertEquals(200, response.statusCode());
      JSONObject putResponse = new JSONObject(response.body());
      JSONObject updatedTodo = TodosPoster.getTodoByName("Design search bar with suggestions");

      // Check that the proper json is returned after it is updated
      assertEquals(updatedTodo.getString("title"), putResponse.getString("title"));
      assertEquals(updatedTodo.getString("description"), putResponse.getString("description"));
    }
  }

  @When("a project manager tries to amend a todo with an invalid name")
  @Then("user receives an error and no changes are done to the todo's name")
  public void testErrorFlow() throws IOException, InterruptedException {
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/1234"))
        .PUT(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"file paperwork 2\"," +
            "    \"doneStatus\": false," +
            "    \"description\": \"test\"," +
            "    \"tasksof\": [" +
            "        {" +
            "            \"id\": \"1\"" +
            "        }" +
            "    ]," +
            "    \"categories\": [" +
            "        {" +
            "            \"id\": \"1\"" +
            "        }" +
            "    ]" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(404, response.statusCode());
  }
}
