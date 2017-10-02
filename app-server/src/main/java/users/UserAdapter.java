package users;

import com.google.gson.reflect.TypeToken;
import data.DataAdapter;
import object.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import utilities.FileOptions;
import utilities.PasswordEncryptDecrypt;
import utilities.google.GoogleLoginValidator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserAdapter {
    public static void main(String[] args) throws Exception {
        User u = new User("testing@gmail.com","test","fname","lname");
//        UserAdapter.registerUser(u);
//        System.out.println(u.getSessionId());
//
//        GetResponse v = DataAdapter.getInstance().elasticsearchSearch("app-users", "user", "cd2dfcb90c894797bcc45ecbb5b451eb");
//        if(v.isExists())
//            try {
//                System.out.println(FileOptions.getGson().fromJson(v.getSourceAsString(), User.class).toString());
//            }catch (Exception e){
//                System.out.println(v.getSource().toString());
//            }

//        System.out.println(UserAdapter.isExistingUser(u));
        u.setUserGroup(User.ADMIN_GROUP);
        ExecutorService service = DataAdapter.writeUser(u);
        try {
//            System.out.println("attempt to shutdown executor");
            service.shutdown();
            service.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
//            System.err.println("tasks interrupted");
        } finally {
//            if (!service.isTerminated()) {
////                System.err.println("cancel non-finished tasks");
//            }
            service.shutdownNow();
        }
        System.exit(1);

//        4655fb4090484ae491cd6a11f569c5af
//        Set<byte[]> keys = DataAdapter.getInstance().getJedis().keys("*".getBytes());
//
//        Iterator<byte[]> it = keys.iterator();
//
//        while(it.hasNext()){
//
//            byte[] data = (byte[])it.next();
//
//            System.out.println(new String(data, 0, data.length));
//        }
    }

    private static final String USER_DATA = "user-data";
    public static int expireTimeInSeconds = 60*60*24; //1 day is the experation

    private UserAdapter() throws IOException {
        this.users = FileOptions.getGson().fromJson(
                DataAdapter.read(USER_DATA),
                new TypeToken<HashSet<User>>(){}.getType());
        if(this.users == null)
            this.users = new HashSet<>();
    }
    private static UserAdapter instance;
    public static UserAdapter getInstance() throws IOException {
        if(instance == null)
            instance = new UserAdapter();
        return instance;
    }

    private HashSet<User> users = new HashSet<>();

    public static void putUserToken(String token, String userId) throws IOException, ExecutionException, InterruptedException {
        DataAdapter.getInstance().getJedis().setex(token,expireTimeInSeconds, userId);
    }
    public static boolean checkToken(String token) throws IOException, ExecutionException, InterruptedException {
        return DataAdapter.getInstance().getJedis().exists(token);
    }

    private static boolean userExists(User user) {
        // TODO: 9/18/17 check if user exists in database

        return false;
    }

    public static String registerUser(User user) throws Exception {
        if(isValid(user)) {
//            getUsers().add(user);
            putUserToken(user.getSessionId(),user.getUserId());
            DataAdapter.writeUser(user);
            return user.getSessionId();
        }else
            throw new Exception("User is Invalid");
    }

    private static boolean isValid(User user) throws Exception {
        //TODO password strength
        if(!EmailValidator.getInstance().isValid(user.getEmailAddress()))
            throw new Exception("Email is invalid: \""+user.getEmailAddress());
        if(getExistingUser(user.getEmailAddress())!=null)
            throw new Exception("User with email: \""+user.getEmailAddress()+"\" already exists");
        return true;
    }

    public static HashSet<User> getUsers() throws IOException {
        // TODO: 9/18/17 Implement elasticsearch
        return getInstance().users;
    }

    public static User getUserByID(String userId) throws Exception {
        GetResponse result = DataAdapter.getInstance().elasticsearchSearch("app-users", "user", userId);
        if(!result.isExists())
            throw new Exception("Something went wrong, no user with token");
        return FileOptions.getGson().fromJson(result.getSourceAsString(),User.class);
    }
    public static User getUser(User user) throws Exception {
        return getUserByID(user.getUserId());
    }
    public static User getUserByToken(String token) throws Exception {
        // TODO: 9/18/17 get user from token.  First get userid from redis then get user from elasticsearch
        if(!checkToken(token))
            throw new Exception("Token doesn't exit");
        String userId = DataAdapter.getInstance().getJedis().get(token);
        // TODO: 9/18/17 check if user is logged in
        return getUserByID(userId);
    }


    public static User getExistingUser(String emailAddress) throws Exception {
        try {
            SearchHit[] results = DataAdapter.getInstance().getClient().prepareSearch("app-users").setTypes("user").setQuery(
                    QueryBuilders.matchQuery("emailAddress", emailAddress)).get().getHits().getHits();
            if (results.length == 0)
                return null;
            return FileOptions.getGson().fromJson(results[0].getSourceAsString(), User.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static User getUser(String email, String password) throws Exception {
        User matchedUser = getExistingUser(email);
        if(matchedUser == null)
            throw new Exception("No user found with email: \""+email+"\" please register and try again");
        if(!PasswordEncryptDecrypt.decrypt(matchedUser.getPassword()).equals(password))
            throw new Exception("Invalid password for email: "+email);
        // TODO: 9/18/17 check tokens in redis for an already logged in user
//        if(!usersToken.values().contains(matchedUser))
//            usersToken.put(matchedUser.getSessionId(),matchedUser);
        matchedUser.setDateLastLogin(new Date());
        DataAdapter.writeUser(matchedUser);
        putUserToken(matchedUser.getSessionId(),matchedUser.getUserId());
        return matchedUser;
    }

    public static User googleUser(String token) throws Exception {
        User gUser = GoogleLoginValidator.getUserFromToken(token);

        User tmp = getExistingUser(gUser.getEmailAddress());
        if(tmp != null){
            tmp.setVerified(gUser.isVerified());
            if(tmp.getImageUrl() == null)
                tmp.setImageUrl(gUser.getImageUrl());
            if(tmp.getLastName() == null)
                tmp.setLastName(gUser.getLastName());
            if(tmp.getFirstName() == null)
                tmp.setFirstName(gUser.getFirstName());
            if(tmp.getUserId() == null)
                tmp.setUserId(gUser.getUserId());
            if(tmp.getLocale() == null)
                tmp.setLocale(gUser.getLocale());
            tmp.setSessionId(token);
            gUser = tmp;
        }else {
            tmp.setVerified(gUser.isVerified());
            tmp.setImageUrl(gUser.getImageUrl());
            tmp.setLastName(gUser.getLastName());
            tmp.setFirstName(gUser.getFirstName());
            tmp.setUserId(gUser.getUserId());
            tmp.setLocale(gUser.getLocale());
            tmp.setSessionId(token);
            gUser = tmp;
        }
        putUserToken(token,gUser.getUserId());
        DataAdapter.writeUser(gUser);
        return gUser;
    }
}
