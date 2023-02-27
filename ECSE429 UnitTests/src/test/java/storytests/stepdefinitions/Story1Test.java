package storytests.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.bs.A;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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
  public void the_server_is_running() {

    todosClient = HttpClient.newBuilder().build();

    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Given("TODOs are present with the following fields")
  public void todos_are_present_with_the_following_fields(Map<String, List<String>> dataTable) throws IOException, InterruptedException {
    HttpResponse response = TodosPoster.postIfNotPresent("Add submit button", "false", "Add a button to submit form");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }
  @When("a project manager creates a task with a project associated to it in the tasksof array")
  public void a_project_manager_creates_a_task_with_a_project_associated_to_it_in_the_projects_array() throws IOException, InterruptedException {
    // Post todos and associate to a project with id 1
    HttpResponse response = TodosPoster.postWithProjectIfNotPresent("Add name field", "true", "Add a name field to form", "1");
    assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
  }

  @Then("the coresponding project is added to the todo's tasksof list")
  public void the_coresponding_project_is_added_to_the_todo_s_list() throws IOException, InterruptedException {
    JSONObject todo = TodosPoster.getTodoByName("Add name field");
    JSONArray tasksof = todo.getJSONArray("tasksof");
    assertEquals("1", tasksof.getJSONObject(0).getString("id"));
  }

  @When("a project manager associates a TODO with a project")
  public void a_project_manager_associates_a_todo_with_a_project() throws IOException, InterruptedException {
    // Everything in the payload is valid except the done status is a string boolean
    JSONObject todo = TodosPoster.getTodoByName("");
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/2/tasksof"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"id\": \"1\"" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, HttpResponse.BodyHandlers.ofString());

    // Assert the post was successful
    assertEquals(201, response.statusCode());

    // Now get the object and compare to the putted object
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/2/tasksof")).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(getResponse.body());
    JSONArray tasksof = responseJson.getJSONArray("projects");

    // Check that the proper structure is returned
    assertEquals("1", tasksof.getJSONObject(0).getString("id"));
    assertEquals("Office Work", tasksof.getJSONObject(0).getString("title"));
    assertEquals("", tasksof.getJSONObject(0).getString("description"));
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
