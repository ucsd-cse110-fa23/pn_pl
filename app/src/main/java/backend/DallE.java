package backend;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * This class represents a Dall-E model that generates an image from a recipe
 * title.
 */
public class DallE {

    private static final String API_ENDPOINT = "https://api.openai.com/v1/images/generations";
    private static final String MODEL = "dall-e-2";
    String apiKey;

    /**
     * Constructor for the DallE class.
     * 
     * @param apiKey
     */
    public DallE(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Generates an image from the provided recipe title using the Dall-E model.
     * 
     * @param recipeTitle
     * @return The generated image.
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    public String generateImage(String recipeTitle)
            throws IOException, InterruptedException, URISyntaxException {
        // Set request parameters
        String prompt = recipeTitle;
        int n = 1;

        // Create a request body which you will pass into request object
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("n", n);
        requestBody.put("size", "256x256");

        // Create the HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create the request object
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(API_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", String.format("Bearer %s", apiKey))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        // Send the request and receive the response
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        // Process the response
        String responseBody = response.body();

        JSONObject responseJson = new JSONObject(responseBody);

        JSONArray dataArray = responseJson.getJSONArray("data");

        String generatedImageURL = dataArray.getJSONObject(0).getString("url");

        // Convert image URL to hex
        URL imageURL = new URL(generatedImageURL);
        ReadableByteChannel rbc = Channels.newChannel(imageURL.openStream());
        FileOutputStream fos = new FileOutputStream("temp.png");
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        File imageFile = new File("temp.png");
        String imageHex = HexUtils.fileToHex(imageFile);
        imageFile.delete();

        return imageHex;
    }
}
