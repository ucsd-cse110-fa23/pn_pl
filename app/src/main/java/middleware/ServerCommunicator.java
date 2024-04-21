package middleware;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;


public class ServerCommunicator {

    private String server_url;

    public ServerCommunicator() {
        try {
            server_url = Files.readString(Paths.get("server_url.txt"));
        } catch (Exception e) {
            server_url = "http://localhost:8100";
        }
    }

    public String getURL() {
        return server_url;
    }

    /**
     * Sends a request to the server and returns the response.
     * 
     * @param path
     * @param query
     * @param method
     * @return the response from the server
     */
    public String sendRequest(String path, String query, String method) throws Exception {
        String urlString = server_url + path;
        if (query != null) {
            urlString += "?" + query;
        }
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();
        in.close();
        return response;
    }
}
