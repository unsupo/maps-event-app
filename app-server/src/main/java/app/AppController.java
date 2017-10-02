package app;

import com.google.gson.reflect.TypeToken;
import data.DataAdapter;
import object.Category;
import object.Event;
import object.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import users.UserAdapter;
import utilities.FileOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static app.EventsController.*;

@RestController
@EnableAutoConfiguration
@RequestMapping("/")
@CrossOrigin
public class AppController {
    private final String ruid = "jkasdjflllk34280234dfsljk0808923rpiojpj";
    private static final String TABLE_NAME = "app-data";

    public static HashMap<String,Event> getAllEvents(){
        return events;
    }

    public static String getAllOutputEvents() throws IOException {
        HashMap<String,Event> events = new HashMap<>();
        getAllEvents().forEach((a,b)->{
            if(!b.isPublic())
                return;
            events.put(a,b);
        });

        String b = FileOptions.getGson().toJson(events);
        DataAdapter.write(TABLE_NAME,b);
        return b;
    }

    public static String addInputEvent(String name, String address, String description, String userId) throws IOException {
        if(description == null)
            description = "";
        if(name!=null && !name.isEmpty()
                && address != null && !address.isEmpty()
                && !events.containsKey(name))
            getAllEvents().put(name, new Event(name,address,description,userId));
        return getAllOutputEvents();
    }

    static private HashMap<String,Event> events = new HashMap<>();

    /**
     * // TODO: 9/18/17 remove hashmap of events instead use redis and query results from elasticsearch or database
     * @throws IOException
     */
    public AppController() throws IOException {
        String data = DataAdapter.read(TABLE_NAME);
        if(data != null)
            events = FileOptions.getGson().fromJson(
                        data,
                        new TypeToken<HashMap<String,Event>>(){}.getType()
                    );
    }

//    @RequestMapping("/")
//    @ResponseBody
//    public String greeting(@RequestHeader(value="name", defaultValue = "World") String name){
//        return "Hello "+name;
//    }

    /**
     * get all events
     *
     * @throws IOException
     */
    @RequestMapping(value = "/getEvents")
    public String getEventsRequestMapping(@RequestHeader(value = "userId", defaultValue = ruid) String userId) throws Exception {
        return FileOptions.getGson().toJson(getPublicAndUserEvents(userId));
    }


    /**
     * Add an event
     *
     * @param name
     * @throws IOException
     */
    @RequestMapping("/events/addEvent")
    public String addEventRequestMapping(@RequestHeader(value = "eventObj") String name) throws Exception {
        return addEventByObj(getMapCheckToken(name));
    }
    /**
     * Add an event
     * // TODO: 9/14/17 Should need a token that is validated against a user
     * // TODO: 9/18/17 add to database
     * THIS ISN"T USED
     *
     * @param name
     * @throws IOException
     */
    @RequestMapping("/events/updateEvent")
    public String updateEventRequestMapping(@RequestHeader(value = "eventObj") String name) throws Exception {
        return updateEventByObj(getMapCheckToken(name));
    }
    /**
     * Add an event
     * // TODO: 9/14/17 Should need a token that is validated against a user
     * // TODO: 9/18/17 add to database
     * THIS ISN"T USED
     *
     * @param name
     * @throws IOException
     */
    @RequestMapping("/events/deleteEvent")
    public String deleteEventRequestMapping(@RequestHeader(value = "eventObj") String name) throws Exception {
        return deleteEventByObj(getMapCheckToken(name));
    }

//
//    /**
//     * Add an event
//     * // TODO: 9/14/17 Should need a token that is validated against a user
//     * // TODO: 9/18/17 add to database
//     * THIS ISN"T USED
//     *
//     * @param name
//     * @param address
//     * @param description
//     * @throws IOException
//     */
//    @RequestMapping("/addEvent")
//    public void addEvent(@RequestHeader(value = "eventName") String name,
//                         @RequestHeader(value = "address") String address,
//                         @RequestHeader(value = "description") String description,
//                         @RequestHeader(value = "userId") String userId) throws IOException {
//        addInputEvent(name,address,description, userId);
//    }

    /**
     * I expect these to be cached in the ui (localstorage) and only update the cache on request
     * // TODO: 9/18/17 get from database
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getCategories",method = RequestMethod.GET)
    public List<Category> getCategories() throws IOException, SQLException, ClassNotFoundException {
        try {
            return Category.getAllCategories();
        }catch (NullPointerException npe){
            return null;
        }
    }

    /**
     * Add categories.  This accepts a json string of a list of category objects
     * ie:
     * [
     *      {name:'a',url:'b'},
     *      {name:'c',url:'d'},
     *      {name:'e',url:'f'}
     * ]
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/addCategories",method = RequestMethod.POST)
    public void addCategories(@RequestHeader(value = "categories") String categories,
                              @RequestHeader(value = "adminToken") String adminToken) throws Exception {
        if(!categories.startsWith("["))
            categories = "["+categories;
        if(!categories.endsWith("]"))
            categories = categories+"]";
        User user = UserAdapter.getUserByToken(adminToken);
        if(!user.getUserGroup().equals(User.ADMIN_GROUP))
            throw new Exception("User is not an Admin");
        Category.addCategories(categories);
    }

    @RequestMapping(value = "/image",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@RequestParam(required = false, defaultValue = "../images/Markers/Firsbee_Marker.jpg", value = "imagePath") String imagePath,
                          @RequestParam(required = false, defaultValue = "resource", value = "type") String type) throws IOException {
        byte[] bytes = null;
        if(type == null || type.isEmpty() || type.equals("resource"))
            bytes = FileOptions.getImageBytesFromStream(imagePath);
        else
            bytes = FileOptions.getImageBytesFromPath(imagePath);
        return ResponseEntity.ok().contentLength(bytes.length).contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

}
