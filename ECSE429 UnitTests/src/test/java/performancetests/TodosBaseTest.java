package performancetests;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.github.javafaker.Faker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TodosBaseTest {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient;

  @BeforeAll
  static void setup() throws IOException, InterruptedException {

    todosClient = HttpClient.newBuilder().build();

    // Check to see if the service is running
    try {
      HttpRequest testConnection = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
      todosClient.send(testConnection, HttpResponse.BodyHandlers.ofString());
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
            "    \"title\": \"test known id 4\"," +
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
    todosClient.send(todoForPut, BodyHandlers.ofString());
    todosClient.send(todoForDelete, BodyHandlers.ofString());
  }

  @AfterAll
  static void cleanup() throws IOException, InterruptedException {
    // Restore the state of the application
    HttpRequest todosDeleteRequest;
    for (int i = 3; i <= 5; i++) {
      todosDeleteRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL + "/" + i))
          .DELETE()
          .build();
      todosClient.send(todosDeleteRequest, BodyHandlers.ofString());
    }

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

  // A helper function to filter the todos array by title
  private JSONObject findTodoByTitle(String title, JSONArray todoList) {
    try {
      for (int i = 0; i < todoList.length(); i++) {
        JSONObject todo = todoList.getJSONObject(i);
        String todoTitle = todo.getString("title");
        if (todoTitle.equals(title)) {
          return todo;
        }
      }
    } catch (JSONException e) {
      System.out.println("No todo with title could be found");
      return new JSONObject();
    }
    return new JSONObject();
  }

  @Test
  void test_GetTodosInitialData() throws IOException, InterruptedException {
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // First assert that the correct success code is returned
    assertEquals(200, response.statusCode());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(response.body());
    JSONArray todosList = responseJson.getJSONArray("todos");
    JSONObject firstTodo = findTodoByTitle("scan paperwork", todosList);

    // Check that the proper structure is returned
    assertEquals("1", firstTodo.getString("id"));
    assertEquals("scan paperwork", firstTodo.getString("title"));
    assertEquals("", firstTodo.getString("description"));
  }

  @Test
  void test_PutAssertMethodNotAllowed() throws IOException, InterruptedException {
    // Get the todos initial data before updating

    // Should not be able to call the base url with put
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
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
    HttpResponse<String> response = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    // First assert that the correct error code is returned
    assertEquals(405, response.statusCode());
  }

  @Test
  void test_DeleteAssertMethodNotAllowed() throws IOException, InterruptedException {
    // Get the todos initial data before updating

    // Should not be able to call the base url with delete
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
        .DELETE()
        .build();
    HttpResponse<String> response = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    // First assert that the correct error code is returned
    assertEquals(405, response.statusCode());
  }

  @Test
  void test_PostTodosInvalidBody_MalformedJson() throws IOException, InterruptedException {
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
        .POST(HttpRequest.BodyPublishers.ofString("{{" +
            "    \"title\": \"test todo\"," +
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
    HttpResponse<String> response = todosClient.send(todosPostRequest, BodyHandlers.ofString());

    // Assert that the correct error code is returned for a malformed Json
    assertEquals(400, response.statusCode());
  }

  @Test
  void test_PostTodosValidBody() throws IOException, InterruptedException {
    Faker faker = new Faker();

    String name = faker.name().fullName();
    boolean doneStatus = faker.bool().bool();
    String description = faker.business().toString();

    // Everything in the payload is valid except the done status is a string boolean
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"test todo post\"," +
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
    HttpResponse<String> response = todosClient.send(todosPostRequest, BodyHandlers.ofString());

    // Assert the post was successful
    assertEquals(201, response.statusCode());

    // Now get the object and compare to the putted object
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject getResponseJson = new JSONObject(getResponse.body());
    JSONArray todosList = getResponseJson.getJSONArray("todos");
    JSONObject newTodo = findTodoByTitle("test todo post", todosList);

    // Check that the proper json is returned after it is updated
    assertNotNull(newTodo);
    assertEquals("test todo post", newTodo.getString("title"));
    assertEquals("test description 2", newTodo.getString("description"));
  }

  @Test
  void test_PostTodosInvalidBody_DoneStatusStringNotBoolean() throws IOException, InterruptedException {
    // Everything in the payload is valid except the done status is a string boolean
    HttpRequest todosPostRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"test todo\"," +
            "    \"doneStatus\": \"true\"," +
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
    HttpResponse<String> response = todosClient.send(todosPostRequest, BodyHandlers.ofString());

    // The application returns an error and does not cast the boolean string
    assertEquals(400, response.statusCode());
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
  void test_PutTodosValidBody() throws IOException, InterruptedException {
    // Get the todos initial data before updating

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/2"))
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
    HttpResponse<String> response = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    // First assert that the correct success code is returned
    assertEquals(200, response.statusCode());

    // Now attempt to parse the todos list
    JSONObject putResponse = new JSONObject(response.body());

    // Now get the object and compare to the putted object
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject getResponseJson = new JSONObject(getResponse.body());
    JSONArray todosList = getResponseJson.getJSONArray("todos");
    JSONObject updatedTodo = findTodoByTitle("file paperwork 2", todosList);

    // Check that the proper json is returned after it is updated
    assertEquals(updatedTodo.getString("id"), putResponse.getString("id"));
    assertEquals(updatedTodo.getString("title"), putResponse.getString("title"));
    assertEquals(updatedTodo.getString("description"), putResponse.getString("description"));

  }

  @Test
  void test_PutTodosInvalidId() throws IOException, InterruptedException {
    // Get the todos initial data before updating

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
    HttpResponse<String> response = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    // Make sure that the correct error code is returned
    assertEquals(404, response.statusCode());

  }

  @Test
  void test_DeleteTodosValidTodo() throws IOException, InterruptedException {
    // Get the todos initial data before deleting

    String todoUrl = TODOS_BASE_URL + "/3";

    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(todoUrl)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());
    assertEquals(200, getResponse.statusCode());

    // Request delete of the todos
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(todoUrl))
        .DELETE()
        .build();
    HttpResponse<String> deleteResponse = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    // First assert that the correct success code is returned
    assertEquals(200, deleteResponse.statusCode());

    // Now try to get the object to ensure it doesn't exist anymore
    HttpRequest newGetRequest = HttpRequest.newBuilder().uri(URI.create(todoUrl)).build();
    HttpResponse<String> newGetResponse = todosClient.send(newGetRequest, BodyHandlers.ofString());

    assertEquals(404, newGetResponse.statusCode());

  }

  @Test
  void test_DeleteTodosInvalidTodo() throws IOException, InterruptedException {
    // Get the todos initial data before deleting

    String todoUrl = TODOS_BASE_URL + "/1234";

    // Request delete of the todos
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(todoUrl))
        .DELETE()
        .build();
    HttpResponse<String> deleteResponse = todosClient.send(todosPutRequest, BodyHandlers.ofString());

    assertEquals(404, deleteResponse.statusCode());

  }

  @Test
  void test_PostTodoAmendsProperly() throws IOException, InterruptedException {
    // Posting a todos with id should behave like put

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/4"))
        .POST(HttpRequest.BodyPublishers.ofString("{" +
            "    \"title\": \"file paperwork abc\"," +
            "    \"doneStatus\": true," +
            "    \"description\": \"test abc\"," +
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

    // First assert that the correct success code is returned
    assertEquals(200, response.statusCode());

    // Now attempt to parse the todos list
    JSONObject putResponse = new JSONObject(response.body());

    // Now get the object and compare to the putted object
    HttpRequest todosGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL)).build();
    HttpResponse<String> getResponse = todosClient.send(todosGetRequest, BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject getResponseJson = new JSONObject(getResponse.body());
    JSONArray todosList = getResponseJson.getJSONArray("todos");
    JSONObject updatedTodo = findTodoByTitle("file paperwork abc", todosList);

    // Check that the proper json is returned after it is updated
    assertEquals(updatedTodo.getString("id"), putResponse.getString("id"));
    assertEquals(updatedTodo.getString("title"), putResponse.getString("title"));
    assertEquals(updatedTodo.getString("description"), putResponse.getString("description"));

  }

  @Test
  void test_PostTodosInvalidId() throws IOException, InterruptedException {
    // Get the todos initial data before updating

    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "/1234"))
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

  @Test
  void test_PostTodosTestPerformance() throws IOException, InterruptedException {
    ArrayList<Long> postTimes = new ArrayList<>();

     postTimes.add(testTodosPostPerformance(1));
     postTimes.add(testTodosPostPerformance(10));
     postTimes.add(testTodosPostPerformance(50));
     postTimes.add(testTodosPostPerformance(100));
     postTimes.add(testTodosPostPerformance(1000));
     postTimes.add(testTodosPostPerformance(5000));
     postTimes.add(testTodosPostPerformance(10000));
     postTimes.add(testTodosPostPerformance(50000));
     postTimes.add(testTodosPostPerformance(100000));

    System.out.println(postTimes);
  }

  @Test
  void test_DeleteTodosTestPerformance() throws IOException, InterruptedException {
    ArrayList<Long> deleteTimes = new ArrayList<>();

     deleteTimes.add(testTodosDeletePerformance(1));
     deleteTimes.add(testTodosDeletePerformance(10));
     deleteTimes.add(testTodosDeletePerformance(50));
     deleteTimes.add(testTodosDeletePerformance(100));
     deleteTimes.add(testTodosDeletePerformance(1000));
     deleteTimes.add(testTodosDeletePerformance(5000));
     deleteTimes.add(testTodosDeletePerformance(10000));

    System.out.println(deleteTimes);
  }

  @Test
  void test_PutTodosTestPerformance() throws IOException, InterruptedException {
    ArrayList<Long> putTimes = new ArrayList<>();

    putTimes.add(testTodosPutPerformance(1));
    putTimes.add(testTodosPutPerformance(10));
    putTimes.add(testTodosPutPerformance(50));
    putTimes.add(testTodosPutPerformance(100));
    putTimes.add(testTodosPutPerformance(1000));
    putTimes.add(testTodosPutPerformance(5000));
    putTimes.add(testTodosPutPerformance(10000));

    System.out.println(putTimes);
  }

  // Helper method to post and delete multiple todos and track the time taken
  private long testTodosPostPerformance(int numTodos) throws IOException, InterruptedException {
    Faker faker = new Faker();
    HttpRequest todosPostRequest;
    String name; // Name will be a random name
    boolean doneStatus; // Boolean will be random every time
    String description; // Description will be random
    long startTime, endTime, totalTime = 0;

    // Repeat n times: Keep creating random todos and measure the time taken
    for (int i = 0; i < numTodos; i++) {
      name = faker.name().firstName();
      doneStatus = faker.bool().bool();
      description = faker.address().fullAddress();

      todosPostRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL))
          .POST(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"" + name + "\"," +
              "    \"doneStatus\": " + doneStatus + "," +
              "    \"description\": \"" + description + "\"," +
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


      // Only measure the time to send the actual REST request and receive a response
      startTime = System.nanoTime();
      todosClient.send(todosPostRequest, BodyHandlers.ofString());
      endTime = System.nanoTime();

      totalTime += endTime - startTime;

      // Restore the state
      TodosPoster.deleteTodo(name);
    }

    return totalTime;
  }

  // Helper method to post and delete multiple todos and track the time taken
  private long testTodosDeletePerformance(int numTodos) throws IOException, InterruptedException {
    Faker faker = new Faker();
    HttpRequest todosPostRequest;
    HttpRequest todosDeleteRequest;
    String name; // Name will be a random name
    boolean doneStatus; // Boolean will be random every time
    String description; // Description will be random
    long startTime, endTime, totalTime = 0;

    // Repeat n times: Keep creating random todos
    for (int i = 0; i < numTodos; i++) {
      name = Integer.toString(i);
      doneStatus = faker.bool().bool();
      description = faker.address().fullAddress();

      todosPostRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL))
          .POST(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"" + name + "\"," +
              "    \"doneStatus\": " + doneStatus + "," +
              "    \"description\": \"" + description + "\"," +
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

      todosClient.send(todosPostRequest, BodyHandlers.ofString());
    }

    // Repeat n times: Keep deleting todos and measure the time taken
    for (int i = 0; i < numTodos; i++) {

      String todoId = TodosPoster.getTodoByName(Integer.toString(i)).getString("id");

      String todoUrl = TODOS_BASE_URL + "/" + todoId;

      // Request delete of the todos
      todosDeleteRequest = HttpRequest.newBuilder()
          .uri(URI.create(todoUrl))
          .DELETE()
          .build();

      // Only measure the time to send the actual REST request and receive a response
      startTime = System.nanoTime();
      todosClient.send(todosDeleteRequest, HttpResponse.BodyHandlers.ofString());
      endTime = System.nanoTime();

      totalTime += endTime - startTime;
    }

    return totalTime;
  }

  // Helper method to post and delete multiple todos and track the time taken
  private long testTodosPutPerformance(int numTodos) throws IOException, InterruptedException {
    Faker faker = new Faker();
    HttpRequest todosPostRequest;
    HttpRequest todosPutRequest;
    String name; // Name will be a random name
    String id;
    boolean doneStatus; // Boolean will be random every time
    String description; // Description will be random
    long startTime, endTime, totalTime = 0;

    // Repeat n times: Keep creating random todos
    for (int i = 0; i < numTodos; i++) {
      name = Integer.toString(i);
      doneStatus = faker.bool().bool();
      description = faker.address().fullAddress();

      todosPostRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL))
          .POST(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"" + name + "\"," +
              "    \"doneStatus\": " + doneStatus + "," +
              "    \"description\": \"" + description + "\"," +
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

      todosClient.send(todosPostRequest, BodyHandlers.ofString());
    }

    // Repeat n times: Keep deleting todos and measure the time taken
    for (int i = 0; i < numTodos; i++) {

      JSONObject todo = TodosPoster.getTodoByName(Integer.toString(i));
      id = todo.getString("id");
      // Change the done status
      doneStatus = !todo.getBoolean("doneStatus");

      // Request update of the todos
      todosPutRequest = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL + "/" + id))
          .PUT(HttpRequest.BodyPublishers.ofString("{" +
              "    \"doneStatus\": " + doneStatus + "," +
              "    \"description\": \"banana\"" +
              "}"))
          .build();

      // Only measure the time to send the actual REST request and receive a response
      startTime = System.nanoTime();
      HttpResponse response = todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());
      endTime = System.nanoTime();

      totalTime += endTime - startTime;

      // Restore the state
      TodosPoster.deleteTodo(Integer.toString(i));
    }

    return totalTime;
  }
}
