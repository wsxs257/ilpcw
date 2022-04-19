package uk.ac.ed.inf;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URI;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This class is used to create HttpClient
 * And use the method to Send HTTP request to access the menus folder in the
 * Webserver.And it also provide the method to parsing the response.
 *
 */
public class Menus {
    String port;
    Menus(String port){
        this.port = port;
    }

    // creat the client;
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Construct the httpRequest  of menus
     * @return built HttpRequest
     */
    private HttpRequest sendRequest(){
        return HttpRequest.newBuilder().uri(URI.create("http://localhost:"+port+"/menus/menus.json")).build();
    }

    /**
     *  Send the request to the server Get the response
     * @return Response String
     * @throws IOException connection wrong| InterruptedException
     */
    private String getResponse(){
        String response_body = " ";
        try {
            HttpResponse<String> response = client.send(sendRequest(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                response_body = response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response_body;
    }

    /**
     * set the class to convert the json response to java class
     */
    private static class Detailed_Menu{
        String name;
        String location;
        List<Item> menu;
        private static class Item{
            String item;
            int pence;
        }
    }

    /**
     *  Get the cost/price of given items by parsing the response body
     * @param dishes list of items names
     * @return total cost of given items
     * @throws IllegalArgumentException if item is not on the menus
     * @throws NullPointerException if input is null
     */
    public int getDeliveryCost(ArrayList<String> dishes){
        int deliveryCost = 50;//delivery fee
        //convert the response body to list of java class
        try{
            Type listType =
                    new TypeToken<ArrayList<Detailed_Menu>>() {}.getType();
            ArrayList<Detailed_Menu> menusList =
                    new Gson().fromJson(getResponse(), listType);
            //iterate each menus in menuList and iterate each item on menus to
            //check if the item is on the menus
            for (Detailed_Menu detailed_menu:menusList){
                for (Detailed_Menu.Item item :detailed_menu.menu){
                    for(String dish:dishes){
                        if(item.item.equals(dish)){
                            deliveryCost = deliveryCost+item.pence;
                        }
                    }
                }
            }
        }catch (IllegalArgumentException|NullPointerException e){
            e.printStackTrace();
        }
        return deliveryCost;
    }

    /**
     * get the what3word type location for given items from web server
     * by parsing the response body
     * @param dishes list of items
     * @return list of what3word locations correspond to where the item from
     */
    public ArrayList<String> get3wordLocation(ArrayList<String> dishes){
        ArrayList<String> storeLocations = new ArrayList<>();
        //convert the response body to list of java class
        try{
            Type listType =
                    new TypeToken<ArrayList<Detailed_Menu>>() {}.getType();
            ArrayList<Detailed_Menu> menusList =
                    new Gson().fromJson(getResponse(), listType);
            //iterate each menus in menuList and iterate each item on menus to
            //check if the item is on the menus
            for (Detailed_Menu detailed_menu:menusList){
                for (Detailed_Menu.Item item :detailed_menu.menu){
                    for(String dish:dishes){
                        if(item.item.equals(dish)){
                            if(storeLocations.contains(detailed_menu.location)){
                                continue;
                            }else {
                                storeLocations.add(detailed_menu.location);
                            }
                        }
                    }
                }
            }
        }catch (IllegalArgumentException|NullPointerException e){
            e.printStackTrace();
        }
        return storeLocations;
    }
}