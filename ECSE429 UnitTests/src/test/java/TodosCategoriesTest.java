import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static org.junit.jupiter.api.Assertions.*;

public class TodosCategoriesTest {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {

    todosClient = HttpClient.newBuilder().build();

    // Check to see if the service is running
    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, BodyHandlers.ofString());
    } catch (ConnectException e) {
      fail();
    }

    // Post a todos to be deleted
    HttpRequest todoForPut = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"test known id 3\"," +
            "    \"doneStatus\": true," +
            "    \"description\": \"test description 2\"," +
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
    HttpRequest todoForDelete = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"test categories 4\"," +
            "    \"doneStatus\": true," +
            "    \"description\": \"test categories 4\"" +
            "}"))
        .build();
    todosClient.send(todoForPut, BodyHandlers.ofString());
    todosClient.send(todoForDelete, BodyHandlers.ofString());
  }

  @AfterAll
  static void cleanup() throws IOException, InterruptedException {
    // Restore the state of the application
    HttpRequest todosDeleteRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/3"))
        .DELETE()
        .build();
    todosClient.send(todosDeleteRequest, BodyHandlers.ofString());

    todosDeleteRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/4"))
        .DELETE()
        .build();
    todosClient.send(todosDeleteRequest, BodyHandlers.ofString());

    HttpRequest todoForPut = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/2"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"file paperwork\"," +
            "    \"doneStatus\": false," +
            "    \"description\": \"\"," +
            "    \"tasksof\": [" +
            "        {" +
            "            \"id\": \"1\"" +
            "        }" +
            "    ]," +
            "}"))
        .build();
    todosClient.send(todoForPut, BodyHandlers.ofString());
    todoForPut = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/1"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"scan paperwork\"," +
            "    \"doneStatus\": false," +
            "    \"description\": \"\"," +
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
    todosClient.send(todoForPut, BodyHandlers.ofString());
  }

  @Test
  void test_GetTodosInitialCategories() throws IOException, InterruptedException {
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/1/categories")).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // First assert that the correct success code is returned
    assertEquals(200, response.statusCode());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(response.body());
    JSONArray categories = responseJson.getJSONArray("categories");

    // Check that the proper structure is returned
    assertEquals("1", categories.getJSONObject(0).getString("id"));
    assertEquals("Office", categories.getJSONObject(0).getString("title"));
    assertEquals("", categories.getJSONObject(0).getString("description"));
  }

  @Test
  void test_PostTodosInvalidBody_MalformedJson() throws IOException, InterruptedException {
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/3/categories"))
        .POST(HttpRequest.BodyPublishers.ofString("{{" +
            "    \"id\": \"1\"," +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, BodyHandlers.ofString());

    // Assert that the correct error code is returned for a malformed Json
    assertEquals(400, response.statusCode());
  }

  @Test
  void test_PostTodosValidBody() throws IOException, InterruptedException {
    // Everything in the payload is valid except the done status is a string boolean
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/2/categories"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"id\": \"1\"" +
            "}"))
        .build();
    HttpResponse<String> response = todosClient.send(todosPostRequest, BodyHandlers.ofString());

    // Assert the post was successful
    assertEquals(201, response.statusCode());

    // Now get the object and compare to the putted object
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/2/categories")).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(getResponse.body());
    JSONArray categories = responseJson.getJSONArray("categories");

    // Check that the proper structure is returned
    assertEquals("1", categories.getJSONObject(0).getString("id"));
    assertEquals("Office", categories.getJSONObject(0).getString("title"));
    assertEquals("", categories.getJSONObject(0).getString("description"));
  }

  @Test
  void test_PostEnsureIdempotent() throws IOException, InterruptedException {
    // Everything in the payload is valid except the done status is a string boolean
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/4/categories"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"id\": \"1\"" +
            "}"))
        .build();

    // Send multiple times and assert there are no side effects
    todosClient.send(todosPostRequest, BodyHandlers.ofString());
    todosClient.send(todosPostRequest, BodyHandlers.ofString());
    HttpResponse<String> response = todosClient.send(todosPostRequest, BodyHandlers.ofString());

    // Assert the post was successful
    assertEquals(201, response.statusCode());

    // Now get the object and check that the categories length is only 1
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/4")).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(getResponse.body());
    JSONArray todos = responseJson.getJSONArray("todos").getJSONObject(0).getJSONArray("categories");
    assertEquals(1, todos.length());

  }

  // ID //

  @Test
  void test_GetTodosIdInitialData() throws IOException, InterruptedException {
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/1")).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // First assert that the correct success code is returned
    assertEquals(200, response.statusCode());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(response.body());
    JSONArray todosList = responseJson.getJSONArray("todos");
    JSONObject firstTodo = todosList.getJSONObject(0);

    // Check that the proper structure is returned
    assertEquals("1", firstTodo.getString("id"));
    assertEquals("scan paperwork", firstTodo.getString("title"));
    assertEquals("", firstTodo.getString("description"));
  }

  @Test
  void test_GetTodosInvalidId() throws IOException, InterruptedException {
    // Get the todos initial data before updating

    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/1234")).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(404, response.statusCode());

  }

  @Test
  void test_DeleteCategoriesValidId() throws IOException, InterruptedException {
    // Get the todos initial data before deleting

    String todoUrl = TODOS_BASE_URL + "/3/categories/1";
    String getTodoCategoriesUrl = TODOS_BASE_URL + "/3/categories";

    // Before state - The todos with id 3 has a category association of id 1
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(getTodoCategoriesUrl)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());
    assertEquals(200, getResponse.statusCode());

    // Request delete of the todos
    HttpRequest todosDeleteRequest = HttpRequest.newBuilder()
        .uri(URI.create(todoUrl))
        .DELETE()
        .build();
    HttpResponse<String> deleteResponse = todosClient.send(todosDeleteRequest, BodyHandlers.ofString());

    // First assert that the correct success code is returned
    assertEquals(200, deleteResponse.statusCode());

    // Now try to get the object to ensure it doesn't exist anymore
    HttpResponse<String> newGetResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());
    JSONObject responseJson = new JSONObject(newGetResponse.body());
    JSONArray todos = responseJson.getJSONArray("categories");
    assertEquals(0, todos.length());

  }

  @Test
  void test_DeleteTodosInvalidTodo() throws IOException, InterruptedException {
    // Get the todos initial data before deleting

    String todoUrl = TODOS_BASE_URL + "2/categories/1234";

    // Request delete of the todos
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(todoUrl))
        .DELETE()
        .build();
    HttpResponse<String> deleteResponse = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    assertEquals(404, deleteResponse.statusCode());

  }

  @Test
  void test_PostTodosInvalidId() throws IOException, InterruptedException {
    // Get the todos initial data before updating

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/1234/categories"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
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
    HttpResponse<String> response = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(404, response.statusCode());

  }
}
