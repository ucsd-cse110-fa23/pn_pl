package backend;

import java.io.*;
import java.net.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides a way to transcribe audio files using the OpenAI API.
 * It sends a POST request to the API endpoint with the audio file and API key,
 * and returns the transcribed text.
 */
public class Whisper {
    private static final String API_ENDPOINT = "https://api.openai.com/v1/audio/transcriptions";
    private static final String MODEL = "whisper-1";

    String apiKey;

    /**
     * Constructor for the Whisper class.
     * 
     * @param apiKey
     */
    public Whisper(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Transcribes the provided audio file using the OpenAI API.
     * 
     * @param audioFile
     * @return The transcribed text.
     * @throws IOException
     * @throws JSONException
     */
    public String transcribeAudio(File audioFile) throws IOException, JSONException {
        HttpURLConnection connection = null;
        // Send a new HTTP client
        try {
            URL url = new URI(API_ENDPOINT).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Set request properties
            String boundary = "Boundary-" + System.currentTimeMillis();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Authorization", "Bearer " + this.apiKey);

            // Attach audio file to request and send it
            OutputStream outputStream = connection.getOutputStream();
            writeParameterToOutputStream(outputStream, "model", MODEL, boundary);
            writeFileToOutputStream(outputStream, audioFile, boundary);
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            outputStream.flush();
            outputStream.close();

            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return handleSuccessResponse(connection);
            } else {
                return handleErrorResponse(connection);
            }
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI for API endpoint", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Writes the specified parameter to the output stream.
     * 
     * @param outputStream
     * @param parameterName
     * @param parameterValue
     * @param boundary
     * @throws IOException
     */
    private void writeParameterToOutputStream(OutputStream outputStream, String parameterName, String parameterValue,
            String boundary) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"" + parameterName + "\"\r\n\r\n").getBytes());
        outputStream.write((parameterValue + "\r\n").getBytes());
    }

    /**
     * Writes the specified file to the output stream.
     * 
     * @param outputStream
     * @param file
     * @param boundary
     * @throws IOException
     */
    private void writeFileToOutputStream(OutputStream outputStream, File file, String boundary) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write(
                ("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
        outputStream.write(("Content-Type: audio/mpeg\r\n\r\n").getBytes());
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Handles the success response from the API.
     * 
     * @param connection
     * @return the transcribed text
     * @throws IOException
     * @throws JSONException
     */
    private String handleSuccessResponse(HttpURLConnection connection) throws IOException, JSONException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        JSONObject responseJson = new JSONObject(response.toString());

        String result = responseJson.getString("text");

        // Ensure that result contains ASCII characters only

        /*
         * Reasoning: When Whisper does not hear anything, it creates random special
         * characters. This is a way to check if Whisper heard anything or not.
         */
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) > 127) {
                return "No ingredients specified or ingredients not recognized.";
            }
        }

        return result;
    }

    /**
     * Handles the error response from the API.
     * 
     * @param connection
     * @return the error response
     * @throws IOException
     * @throws JSONException
     */
    private String handleErrorResponse(HttpURLConnection connection) throws IOException, JSONException {
        StringBuilder errorResponse = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
        }
        return errorResponse.toString();
    }
}
