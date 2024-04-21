package backend;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The ChatGPT class is responsible for interacting with the OpenAI GPT-3 API.
 * It uses the API to generate text based on a given prompt.
 */
public class ChatGPT {

    private static final String API_ENDPOINT = "https://api.openai.com/v1/completions";
    private static final String MODEL = "text-davinci-003";

    String apiKey;

    /**
     * Constructor for the ChatGPT class.
     * 
     * @param apiKey The API key for the OpenAI GPT-3 API.
     */
    public ChatGPT(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Generates text using the OpenAI GPT-3 API.
     *
     * @param prompt    The prompt to base the generated text on.
     * @param maxTokens The maximum number of tokens in the generated text.
     * @return The generated text.
     * @throws IOException If an I/O error occurs when sending the request.
     */
    public String generateText(String prompt, int maxTokens) throws IOException {
        // Create a JSON object to hold the request parameters
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 1.0);

        // Send a new HTTP client
        HttpClient client = HttpClient.newHttpClient();
        String responseBody = "";

        try {
            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // Send HTTP request and get response
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());
            responseBody = response.body();
        } catch (IOException | InterruptedException e) {
            // If an error occurs, throw an IOException
            e.printStackTrace();
            throw new IOException("Error sending ChatGPT request");
        }

        // Parse the response body and return the generated text
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray choices = responseJson.getJSONArray("choices");
        return choices.getJSONObject(0).getString("text");
    }

}
