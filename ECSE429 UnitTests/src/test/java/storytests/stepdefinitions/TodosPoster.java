package storytests.stepdefinitions;

import org.json.JSONArray;
import org.json.JSONException;
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

  /**
   * Adds todos if it is not already there and associates with a category
   */
  public static HttpResponse postWithCategoryIfNotPresent(String title, String doneStatus, String description, String categoryId) throws IOException, InterruptedException {
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
              "    \"categories\": [" +
              "        {" +
              "            \"id\": \"" + categoryId +"\"" +
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

    if (!todo.has("id")) {
      return false;
    }

    String id = todo.getString("id");

    HttpRequest projectsGetRequest = HttpRequest.newBuilder().uri(URI.create(TODOS_BASE_URL + "/" + id + "/tasksof")).build();
    HttpResponse<String> getResponse = todosClient.send(projectsGetRequest, HttpResponse.BodyHandlers.ofString());
    JSONObject responseJson = new JSONObject(getResponse.body());
    JSONArray tasksof = responseJson.getJSONArray("projects");
    JSONArray projectTasks;
    try {
      projectTasks = tasksof.getJSONObject(0).getJSONArray("tasks");
    } catch (JSONException e) {
      return false;
    }


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

  public static boolean categoryHasTodo(String categoryId, String todoName) throws IOException, InterruptedException {
    JSONObject todo = getTodoByName(todoName);
    JSONArray categories = todo.getJSONArray("categories");

    for (int i = 0; i < categories.length(); i++) {
      JSONObject category = categories.getJSONObject(i);
      if (category.getString("id").equals(categoryId)) {
        return true;
      }
    }
    return false;
  }

  public static boolean categoryIsPresent(String categoryName) throws IOException, InterruptedException {
    HttpRequest projectsGetRequest = HttpRequest.newBuilder().uri(URI.create("http://localhost:4567/categories")).build();
    HttpResponse<String> getResponse = todosClient.send(projectsGetRequest, HttpResponse.BodyHandlers.ofString());
    JSONObject responseJson = new JSONObject(getResponse.body());
    JSONArray categories = responseJson.getJSONArray("categories");

    for (int i = 0; i < categories.length(); i++) {
      JSONObject projectTask = categories.getJSONObject(i);
      if (projectTask.getString("title").equals(categoryName)) {
        return true;
      }
    }

    return false;
  }

  public static HttpResponse dissasociateFromProject(String projectId, String todoId) throws IOException, InterruptedException {
    String todoUrl = TODOS_BASE_URL + "/" + todoId + "/tasksof/" + projectId;

    // Request delete of the todos
    HttpRequest todosDeleteRequest = HttpRequest.newBuilder()
        .uri(URI.create(todoUrl))
        .DELETE()
        .build();
    HttpResponse<String> deleteResponse = todosClient.send(todosDeleteRequest, HttpResponse.BodyHandlers.ofString());

    return deleteResponse;
  }

  public static HttpResponse deleteTodo(String todoId) throws IOException, InterruptedException {

    String todoUrl = TODOS_BASE_URL + "/" + todoId;

    // Request delete of the todos
    HttpRequest todosPutRequest = HttpRequest.newBuilder()
        .uri(URI.create(todoUrl))
        .DELETE()
        .build();
    return todosClient.send(todosPutRequest, HttpResponse.BodyHandlers.ofString());
  }

  public static boolean todoInProject(JSONArray tasks, String todoId) throws IOException, InterruptedException {

    for (int i = 0; i < tasks.length(); i++) {
      JSONObject projectTask = tasks.getJSONObject(i);
      if (projectTask.getString("id").equals(todoId)) {
        return true;
      }
    }

    return false;
  }
}
