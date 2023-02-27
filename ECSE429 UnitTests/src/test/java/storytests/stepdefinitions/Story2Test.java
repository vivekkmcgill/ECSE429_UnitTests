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
@SelectClasspathResource("story2")
public class Story2Test {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {
    // This is just to assert the server is running properly
    todosClient = HttpClient.newBuilder().build();
  }

  @Given("the server is running.")
  public void testGivenServerRunning() {

    todosClient = HttpClient.newBuilder().build();

    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Given("TODOs are present with the following fields and categories")
  public void testGivenTodosPresentWithFollowingFields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    HttpResponse response = TodosPoster.postIfNotPresent("Complete office work", "false", "complete office work");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
    response = TodosPoster.postIfNotPresent("Water plants", "false", "water your plants");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }

  @Given("categories are present with the following fields")
  public void testGivenCategoriesPresentWithFollowingFields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    assertTrue(TodosPoster.categoryIsPresent("Office"));
    assertTrue(TodosPoster.categoryIsPresent("Home"));
  }

  @When("an employee associates a TODO with a category")
  @Then("the corresponding category is added to the todo's categories list")
  public void testNormalFlow() throws IOException, InterruptedException {
    // Test that a category association is created when an existing task is updated instead of created
    // First get the existing task id
    JSONObject todo = TodosPoster.getTodoByName("Complete office work");
    String id = todo.getString("id");

    // Now create the association
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/" + id + "/categories"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"id\": \"1\"" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, HttpResponse.BodyHandlers.ofString());

    // Assert the post was successful
    assertEquals(201, response.statusCode());

    // Now get the category object and assert that it contains the new association
    // Using a helper method in a separate class
    assertTrue(TodosPoster.categoryHasTodo("1", "Complete office work"));
  }

  @When("an employee creates a task with a project associated to it in the categories array")
  @Then("the corresponding category is added to the todos categories list")
  public void testAlternateFlow() throws IOException, InterruptedException {
    // Post todos and associate to a project with id 1
    HttpResponse response = TodosPoster.postWithCategoryIfNotPresent("Wash dishes", "false", "wash your dishes", "2");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

    JSONObject todo = TodosPoster.getTodoByName("Wash dishes");
    JSONArray categories = todo.getJSONArray("categories");
    assertEquals("2", categories.getJSONObject(0).getString("id"));
  }

  @When("an employee tries to associate a TODO with a non-extsient or deleted category")
  @Then("user receives an error and no associations are added to the todo's categories list")
  public void testErrorFlow() throws IOException, InterruptedException {
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/1/categories"))
        .POST(HttpRequest.BodyPublishers.ofString("{ \"id\": \"1234\"}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, HttpResponse.BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(404, response.statusCode());

    // Now check that no associations were added to the todos unintentionally
    JSONObject test = TodosPoster.getTodoByName("scan paperwork");
    assertEquals(1, test.getJSONArray("categories").length());
  }
}
