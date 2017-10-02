package app;

import com.google.gson.reflect.TypeToken;
import data.DataAdapter;
import object.Category;
import object.Event;
import object.User;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilders;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import users.UserAdapter;
import utilities.FileOptions;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@EnableAutoConfiguration
@RequestMapping("/")
@CrossOrigin
public class EventsController {
    /**
     * insert/update/delete event then send the ping to all connected clients.
     *
     * defaults to adding an event.
     *
     * put "eventType":"delete" in json string to delete the event.
     *
     * @param name
     * @return
     * @throws IOException
     */
    @MessageMapping("/event")
    @SendTo("/topic/addEvent")
    public String addEvent(String name) throws Exception {
        HashMap<String, String> map = getMapCheckToken(name);
        if(map.containsValue("eventType")) {
            if (map.get("eventType").equals("add"))
                return addEventByObj(map);
            if (map.get("eventType").equals("update"))
                return updateEventByObj(map);
            if (map.get("eventType").equals("delete"))
                return deleteEventByObj(map);
        }
        return addEventByObj(map);
    }

    public static HashMap<String,String> getMapCheckToken(String name) throws Exception {
        HashMap<String, String> map = parseMap(name);
        if(!UserAdapter.checkToken(map.get("token")))
            throw new Exception("Invalid Token");
        return map;
    }


//    /**
//     * @param name
//     * @return
//     * @throws IOException
//     */
//    @MessageMapping("/updateEvent")
//    @SendTo("/topic/updateEvent")
//    public String updateEvent(String name) throws Exception {
//        return updateEventByObj(name);
//    }
//
//    /**
//     * @param name
//     * @return
//     * @throws IOException
//     */
//    @MessageMapping("/deleteEvent")
//    @SendTo("/topic/deleteEvent")
//    public String deleteEvent(String name) throws Exception {
//        return addEventByObj(name);
//    }

    public static String updateEventByObj(HashMap<String,String> map) throws Exception {
        checkForNullOrEmpty(Arrays.asList("eventId"),map);
        GetResponse v = DataAdapter.getInstance().elasticsearchSearch("app-events", "event", map.get("eventId"));
        if(!v.isExists())
            throw new Exception("Event does not exist with id: "+map.get("eventId"));
        Event e = FileOptions.getGson().fromJson(v.getSourceAsString(),Event.class);
        DataAdapter.updateEvent(e);
        User user = UserAdapter.getUserByToken(map.get("token"));
        return FileOptions.getGson().toJson(getPublicAndUserEvents(user.getUserId()));
    }

    public static String deleteEventByObj(HashMap<String,String> map) throws Exception {
        checkForNullOrEmpty(Arrays.asList("eventId"),map);
        DataAdapter.getInstance().getClient().prepareDelete("app-events","event",map.get("eventId"));
        User user = UserAdapter.getUserByToken(map.get("token"));
        return FileOptions.getGson().toJson(getPublicAndUserEvents(user.getUserId()));
    }

    public static String addEventByObj(HashMap<String,String> map) throws Exception {
        checkForNullOrEmpty(Arrays.asList("eventName","address","lat","lng","category"),map);
        User user = UserAdapter.getUserByToken(map.get("token"));
        if(!Category.getAllCategories().contains(new Category(map.get("category"))))
            throw new Exception("Category is not valid: "+map.get("category"));
        if(!map.containsKey("description") || map.get("description") == null || map.get("description").isEmpty())
            map.put("description","");
        map.put("userIdCreated",user.getUserId());
        Event event = FileOptions.getGson().fromJson(FileOptions.getGson().toJson(map),Event.class);
        event.setLat(Double.parseDouble(map.get("lat")));
        event.setLng(Double.parseDouble(map.get("lng")));
        DataAdapter.writeEvent(event);
        return FileOptions.getGson().toJson(getPublicAndUserEvents(user.getUserId()));
    }

    public static HashMap<String,String> parseMap(String name) {
        HashMap<String,String> map = null;
        try{
            map  = FileOptions.getGson()
                    .fromJson(name,new TypeToken<HashMap<String,String>>(){}.getType());
        } catch (Exception e) {
            map = FileOptions.getGson()
                    .fromJson(name.substring(1, name.length() - 1).replace("\\", ""),
                            new TypeToken<HashMap<String, String>>() {
                            }.getType());
        }
        return map;
    }

    public static Event getEvent(Event event) throws InterruptedException, ExecutionException, IOException {
        SearchHit[] res = DataAdapter.getInstance().getClient().prepareSearch("app-events").setTypes("event").setQuery(
                new BoolQueryBuilder()
                        .must(QueryBuilders.matchQuery("userIdCreated", event.getUserIdCreated()))
                        .must(QueryBuilders.geoShapeQuery("location",
                                ShapeBuilders.newCircleBuilder().center(event.getLng(), event.getLat()).radius(event.getLocation().getRadius())))
        ).get().getHits().getHits();
        if(res != null && res.length > 0)
            return FileOptions.getGson().fromJson(res[0].getSourceAsString(),Event.class);
        return null;
    }

    public static List<Event> getPublicAndUserEvents(String userId) throws InterruptedException, ExecutionException, IOException {
        //query to get only events that are public or created by the userId provided
        return Arrays.asList(
                        DataAdapter.getInstance().getClient().prepareSearch("app-events").setTypes("event").setQuery(
                                new BoolQueryBuilder()
                                        .must(QueryBuilders.matchQuery("userIdCreated",userId))
                                        .should(QueryBuilders.matchQuery("isPublic","true"))
                        ).get().getHits().getHits()
                ).stream().map(a->FileOptions.getGson().fromJson(a.getSourceAsString(),Event.class)).collect(Collectors.toList());
    }

    private static void checkForNullOrEmpty(List<String> ts, HashMap<String, String> map) throws Exception {
        for(String s : ts)
            if(!map.containsKey(s) || map.get(s) == null || map.get(s).isEmpty())
                throw new Exception(s+": is null or empty");
    }

}
