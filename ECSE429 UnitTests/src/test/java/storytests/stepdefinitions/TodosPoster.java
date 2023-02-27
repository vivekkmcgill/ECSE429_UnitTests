package storytests.stepdefinitions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TodosPoster {

  private static final String TODOS_BASE_URL = "http://localhost:4567/todos";
  private static HttpClient todosClient = todosClient = HttpClient.newBuilder().build();;

  /**
   * Adds todos if it is not already there
   */
  public static HttpResponse postIfNotPresent(String title, String doneStatus, String description) throws IOException, InterruptedException {
    // Search for the todos with the title to confirm whether they exist
    HttpRequest todosGetRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "?title=" + title.replaceAll(" ", "%20"))).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(response.body());
    JSONArray todosList = responseJson.getJSONArray("todos");

    if (todosList.length() == 0) {
      HttpRequest todoForPut = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL))
          .POST(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"" + title + "\"," +
              "    \"doneStatus\": " + doneStatus + "," +
              "    \"description\": \"" + description + "\"" +
              "}"))
          .build();
      return todosClient.send(todoForPut, HttpResponse.BodyHandlers.ofString());
    }
    return response;
  }

  /**
   * Adds todos if it is not already there and associates with a project
   */
  public static HttpResponse postWithProjectIfNotPresent(String title, String doneStatus, String description, String projectId) throws IOException, InterruptedException {
    // Search for the todos with the title to confirm whether they exist
    HttpRequest todosGetRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "?title=" + title.replaceAll(" ", "%20"))).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(response.body());
    JSONArray todosList = responseJson.getJSONArray("todos");

    if (todosList.length() == 0) {
      HttpRequest todoForPut = HttpRequest.newBuilder()
          .uri(URI.create(TODOS_BASE_URL))
          .POST(HttpRequest.BodyPublishers.ofString("{" +
              "    \"title\": \"" + title + "\"," +
              "    \"doneStatus\": " + doneStatus + "," +
              "    \"description\": \"" + description + "\"," +
              "    \"tasksof\": [" +
              "        {" +
              "            \"id\": \"" + projectId +"\"" +
              "        }" +
              "    ]" +
              "}"))
          .build();
      return todosClient.send(todoForPut, HttpResponse.BodyHandlers.ofString());
    }
    return response;
  }

  public static JSONObject getTodoByName(String title) throws IOException, InterruptedException {
    HttpRequest todosGetRequest = HttpRequest.newBuilder()
        .uri(URI.create(TODOS_BASE_URL + "?title=" + title.replaceAll(" ", "%20"))).build();
    HttpResponse<String> response = todosClient.send(todosGetRequest, HttpResponse.BodyHandlers.ofString());

    // Now attempt to parse the todos list
    JSONObject responseJson = new JSONObject(response.body());
    JSONArray todosList = responseJson.getJSONArray("todos");

    if (todosList.length() != 0) {
      return todosList.getJSONObject(0);
    } else {
      return new JSONObject();
    }
  }

  public static boolean projectHasTodo(String todoName) throws IOException, InterruptedException {
    JSONObject todo = getTodoByName(todoName);
    String id = todo.getString("id");

    HttpRequest projectsGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/" + id + "/tasksof")).build();
    HttpResponse<String> getResponse = todosClient.send(projectsGetRequest, HttpResponse.BodyHandlers.ofString());
    JSONObject responseJson = new JSONObject(getResponse.body());
    JSONArray tasksof = responseJson.getJSONArray("projects");
    JSONArray projectTasks = tasksof.getJSONObject(0).getJSONArray("tasks");

    // Check that the proper structure is returned
    assertEquals("1", tasksof.getJSONObject(0).getString("id"));
    assertEquals("Office Work", tasksof.getJSONObject(0).getString("title"));
    assertEquals("", tasksof.getJSONObject(0).getString("description"));

    for (int i = 0; i < projectTasks.length(); i++) {
      JSONObject projectTask = projectTasks.getJSONObject(i);
      if (projectTask.getString("id").equals(id)) {
        return true;
      }
    }
    return false;
  }
}
