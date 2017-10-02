package data;

import app.EventsController;
import config.ConfigManager;
import object.Category;
import object.Event;
import object.User;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import redis.clients.jedis.Jedis;
import users.UserAdapter;
import utilities.FileOptions;
import utilities.database.Database;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class DataAdapter {
    private static DataAdapter instance;

    public static DataAdapter getInstance() throws IOException, ExecutionException, InterruptedException {
        if (instance == null)
            instance = new DataAdapter();
        return instance;
    }

    // TODO: 9/18/17 Option to write to database
    public static final String FILE = "file-type";

    public static String getType() throws IOException {
        String data = ConfigManager.getConfig("data");
        if (data == null || data.equals("file"))
            return FILE;
        return FILE;
    }

    public static void write(String tableName, String dataToWrite) throws IOException {
        FileOptions.runConcurrentProcess(() -> {
            if (FILE.equals(getType()))
                FileOptions.writeToFileAppend(FileOptions.getDefaultDir() + "/" + tableName, dataToWrite);
            //cache
            getInstance().getJedis().set(tableName, dataToWrite);

            return null;
        });
    }

    public static String read(String tableName) throws IOException {
        //TODO aggregate events so you don't get everything
        String file = FileOptions.getDefaultDir() + "/" + tableName + ".json";
        if (FILE.equals(getType()) && new File(file).exists())
            return FileOptions.readFileIntoString(file);
        return null;
    }

    public static ExecutorService writeUser(User user) {
        //Elasticsearch stores user for quick searches
        return FileOptions.runConcurrentProcessNonBlocking(() -> {
            try {
                try {
                    SearchHit[] v = getInstance().elasticsearchGetQuery("app-users", "user", "emailAddress", user.getEmailAddress());
                    if (v != null && v.length > 0)
                        user.setUserId(v[0].getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getInstance().elasticsearchInsert("app-users", "user", user.getUserId(), FileOptions.getGson().toJson(user));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //postgres stores it for source of truth
            try {
                Database con = Database.newDatabaseConnection();
                List<Map<String, Object>> v = con.executeQuery("select * from users where email_address = '" + user.getEmailAddress() + "';");
                if (v.isEmpty() || v.get(0) == null || v.get(0).isEmpty())
                    con.executeQuery( //add new user to database
                            "insert into USERS(user_id,first_name,last_name,password,email_address," +
                                    "user_group,date_created,date_last_login,is_verified,image_url)" +
                                    " values(?,?,?,?,?,?,?,?,?,?)" +
                                    "   ON CONFLICT (user_id)\n" +
                                    "  DO UPDATE SET\n" +
                                    "    first_name = EXCLUDED.first_name,\n" +
                                    "    last_name = EXCLUDED.last_name,\n" +
                                    "    password = EXCLUDED.password,\n" +
                                    "    email_address = EXCLUDED.email_address,\n" +
                                    "    user_group = EXCLUDED.user_group,\n" +
                                    "    date_created = EXCLUDED.date_created,\n" +
                                    "    date_last_login = EXCLUDED.date_last_login,\n" +
                                    "    is_verified = EXCLUDED.is_verified,\n" +
                                    "    image_url = EXCLUDED.image_url;",
                            Arrays.asList(user.getUserId(), user.getFirstName(), user.getLastName(), user.getPassword(),
                                    user.getEmailAddress(), user.getUserGroup(), user.getDateCreated(), user.getDateLastLogin(),
                                    user.isVerified(), user.getImageUrl()
                            )
                    );
                else
                    con.executeQuery("update USERS set first_name = ?, last_name = ?, password = ?, user_group = ?, date_created = ?," +
                                    "date_last_login=?,is_verified=?,image_url=? where email_address = ?;",
                            Arrays.asList(user.getFirstName(), user.getLastName(), user.getPassword(),
                                    user.getUserGroup(), user.getDateCreated(), user.getDateLastLogin(),
                                    user.isVerified(), user.getImageUrl(), user.getEmailAddress()
                            ));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static void writeEvent(Event event) {
        //Elasticsearch stores user for quick searches
        Event ev = event;
        try {
            ev = EventsController.getEvent(event);
            // TODO: 9/19/17 check if event by this user already exists. still to do: start_time
            if (ev == null) {
                getInstance().elasticsearchInsert("app-events", "event", event.getEventId(), FileOptions.getGson().toJson(event));
                ev = event;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Event event1 = ev;

        FileOptions.runConcurrentProcessNonBlocking(() -> {
            try {
                databaseEvent(event1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static void updateEvent(Event ev) throws InterruptedException, ExecutionException, IOException {
        getInstance().elasticsearchInsert("app-events", "event", ev.getEventId(), FileOptions.getGson().toJson(ev));
        FileOptions.runConcurrentProcessNonBlocking(() -> {
            try {
                databaseEvent(ev);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private static void databaseEvent(Event ev) throws SQLException, ParseException, IOException, ClassNotFoundException {
        Database con = Database.newDatabaseConnection();
        List<Map<String, Object>> v = con.executeQuery(
                "select * from events where USER_ID_CREATED = ? and lat = ? and lng = ? and start_time" + (ev.getStartTime() == null ? " is " : " = ") + "?;",
                Arrays.asList(ev.getUserIdCreated(), ev.getLat(), ev.getLng(), ev.getStartTime())
        );
        if (v.isEmpty() || v.get(0) == null || v.get(0).isEmpty())
            con.executeQuery(
                    "insert into EVENTS(EVENT_ID,EVENT_NAME,ADDRESS,DESCRIPTION,CATEGORY," +
                            "USER_ID_CREATED,LAT,LNG,IS_PUBLIC,START_TIME,END_TIME)" +
                            " values(?,?,?,?,?,?,?,?,?,?,?)" +
                            "   ON CONFLICT (EVENT_ID)\n" +
                            "  DO UPDATE SET\n" +
                            "    EVENT_NAME = EXCLUDED.EVENT_NAME,\n" +
                            "    ADDRESS = EXCLUDED.ADDRESS,\n" +
                            "    DESCRIPTION = EXCLUDED.DESCRIPTION,\n" +
                            "    CATEGORY = EXCLUDED.CATEGORY,\n" +
                            "    USER_ID_CREATED = EXCLUDED.USER_ID_CREATED,\n" +
                            "    LAT = EXCLUDED.LAT,\n" +
                            "    LNG = EXCLUDED.LNG,\n" +
                            "    START_TIME = EXCLUDED.START_TIME,\n" +
                            "    END_TIME = EXCLUDED.END_TIME,\n" +
                            "    IS_PUBLIC = EXCLUDED.IS_PUBLIC;",
                    Arrays.asList(
                            ev.getEventId(), ev.getEventName(), ev.getAddress(), ev.getDescription(), ev.getCategory(),
                            ev.getUserIdCreated(), ev.getLat(), ev.getLng(), ev.isPublic(), ev.getStartTime(), ev.getEndTime()
                    )
            );
        else
            for (Map<String, Object> q : v)
                con.executeQuery(
                        "update events set event_name = ?, address = ?, description = ?, category = ?, user_id_created = ?, is_public = ?" +
                                ", end_time = ? where event_id = ?;",
                        Arrays.asList(ev.getEventName(), ev.getAddress(), ev.getDescription(), ev.getCategory(), ev.getUserIdCreated(),
                                ev.isPublic(), ev.getEndTime(), q.get("event_id"))
                );
    }

    public static void writeCategories(List<Category> cats) {
        //postgres stores it for source of truth
        FileOptions.runConcurrentProcessNonBlocking(() -> {
            for (Category category : cats)
                try {
                    Database.newDatabaseConnection().executeQuery(
                            "insert into CATEGORIES(NAME,URL)" +
                                    " values(?,?)" +
                                    "   ON CONFLICT (NAME)\n" +
                                    "  DO UPDATE SET\n" +
                                    "    URL = EXCLUDED.URL;",
                            Arrays.asList(
                                    category.getName(), category.getUrl()
                            )
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            return null;
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////
    private DataAdapter() throws IOException, ExecutionException, InterruptedException {
        jedis = new Jedis("localhost");
        client = new PreBuiltTransportClient(Settings.builder()
                .put("cluster.name", ConfigManager.getConfig("elasticsearch.nodename")).build())
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        setMappings();
    }

    private void setMappings() throws ExecutionException, InterruptedException {
        List<String> indexes = new ArrayList<>();
        indexes = Arrays.asList(getClient().admin().indices().getIndex(new GetIndexRequest()).get().getIndices());
        if (!indexes.contains("app-users"))
            getClient().admin().indices().prepareCreate("app-users").addMapping("user", "{\"user\":{\n" +
                    "        \"dynamic_templates\": [\n" +
                    "            { \"notanalyzed\": {\n" +
                    "                  \"match\":              \"*\", \n" +
                    "                  \"match_mapping_type\": \"string\",\n" +
                    "                  \"mapping\": {\n" +
                    "                      \"type\":        \"string\",\n" +
                    "                      \"index\":       \"not_analyzed\"\n" +
                    "                  }\n" +
                    "               }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "       }}", XContentType.JSON).get();
        if (!indexes.contains("app-events"))
            getClient().admin().indices().prepareCreate("app-events").addMapping("event", "{\"event\":{\n" +
                    "      \"properties\": {\n" +
                    "        \"location\": {\n" +
                    "          \"type\": \"geo_shape\"\n" +
                    "        }}," +
                    "        \"dynamic_templates\": [\n" +
                    "            { \"notanalyzed\": {\n" +
                    "                  \"match\":              \"*\", \n" +
                    "                  \"match_mapping_type\": \"string\",\n" +
                    "                  \"mapping\": {\n" +
                    "                      \"type\":        \"string\",\n" +
                    "                      \"index\":       \"not_analyzed\"\n" +
                    "                  }\n" +
                    "               }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "       }}", XContentType.JSON).get();

    }

    private Jedis jedis;
    private TransportClient client;

    public Jedis getJedis() {
        return jedis;
    }

    public TransportClient getClient() {
        return client;
    }

    public IndexResponse elasticsearchInsert(String index, String type, String id, String json) {
        return client.prepareIndex(index, type, id).setSource(json, XContentType.JSON).get();
    }

    public List<SearchHit> elasticsearchSearch(String index, String type) {
        TransportClient client = getClient();
        // build your query here -- no need for setFrom(int)
        SearchRequestBuilder requestBuilder = client.prepareSearch(index)
                .setTypes(type)
                .setQuery(QueryBuilders.matchAllQuery());

        SearchHitIterator hitIterator = new SearchHitIterator(requestBuilder);
        List<SearchHit> searchHits = new ArrayList<>();
        while (hitIterator.hasNext())
            searchHits.add(hitIterator.next());
        return searchHits;
    }

    public GetResponse elasticsearchSearch(String index, String type, String id) {
        return getClient().prepareGet(index, type, id).get();
    }

    public SearchHit[] elasticsearchGetQuery(String index, String type, String field, String value) {
        SearchHit[] results = getClient().prepareSearch(index).setTypes(type).setQuery(
                QueryBuilders.matchQuery(field, value)).get().getHits().getHits();
        if (results.length == 0)
            return null;
        return results;
    }
}
