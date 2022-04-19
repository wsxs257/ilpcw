package uk.ac.ed.inf;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.Gson;


/**
 * This class is used to create HttpClient
 * And use the method to Send HTTP request to access the words folders and files in the
 * Webserver.And it also provide the method to parsing the response.
 */
public class Words {
    String webURL;

    Words(String webURL){
        this.webURL = webURL;
    }

    /**
     * private class for parsing the Json response
     */
    private static class Details{
        String country;
        Square square;
        private static class Square{
            private static class Southwest{
                String lng;
                String lat;
            }
            private static class Northeast{
                String lng;
                String lat;
            }
        }
        String nearestPlace;
        Coordinates coordinates;
        public static class Coordinates{
            double lng;
            double lat;
        }
        String words;
        String language;
        String map;
    }

    //creat the client;
    private static final HttpClient client = HttpClient.newHttpClient();
    //"http://localhost:"+port+"/words/"+word1+"/"+word2+"/"+word3+"/"+"details.json."

    /**construct the HTTP request
     *
     * @return the constructed HTTP request
     */
    private HttpRequest getWordsRequest(){
        return HttpRequest.newBuilder().uri(URI.create(webURL)).build();
    }

    /**get the response from the web server
     *
     * @return the response body
     */
    private String getWordsResponse(){
        String response_body = " ";
        try {
            HttpResponse<String> response = client.send(getWordsRequest(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                response_body = response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response_body;
    }

    /** return the longitude of one what3word location after parsing the response body.
     *
     * @return longitude of one what3word location
     */
    public double getLng(){
        Details details =
                new Gson().fromJson(getWordsResponse(), Details.class);
        return details.coordinates.lng;
    }

    /** return the latitude of one what3word location after parsing the response body.
     *
     * @return latitude of one what3word location
     */
    public double getLat(){
        Details details =
                new Gson().fromJson(getWordsResponse(), Details.class);
        return details.coordinates.lat;
    }


}
