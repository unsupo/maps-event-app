package data;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class DatabaseAdapter {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
//        Jedis jedis = new Jedis("localhost");
//        jedis.set("test","testing");
//        System.out.println(jedis.get("test"));
//        TransportClient client = new PreBuiltTransportClient(Settings.builder()
//                .put(ClusterName.CLUSTER_NAME_SETTING.getKey(),"elasticsearch_jarndt").build())
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
////                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));
//
////          on shutdown
//
////        IndexResponse response = client.prepareIndex("twitter", "tweet", "1")
////                .setSource(jsonBuilder()
////                        .startObject()
////                        .field("user", "kimchy")
////                        .field("postDate", new Date())
////                        .field("message", "trying out Elasticsearch")
////                        .endObject()
////                )
////                .get();
//        GetResponse response = client.prepareGet("twitter", "tweet", "1").get();
//
//        System.out.println(response.toString());

        System.out.println(DataAdapter.getInstance().elasticsearchInsert("twitter", "tweet", "1","{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}"));



//        em.out.println(DataAdapter.getInstance().getClient().prepareGet("twitter", "tweet", "1").get());
    }




}
