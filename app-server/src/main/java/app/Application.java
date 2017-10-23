package app;

import config.ConfigManager;
import object.Category;
import object.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import users.UserAdapter;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        ConfigManager.setConfigFiles(args);
        initialize();
        SpringApplication.run(Application.class, args);
    }

    private static void initialize(){
        //create default admin user
        try {
            User u = new User("admin@admin.com", "admin", "admin", "admin");
            u.setUserGroup(User.ADMIN_GROUP);
            UserAdapter.registerUser(u);
        }catch(Exception e){
            e.printStackTrace();
        }
        //Create default category
        try {
            Category.addCategories("[{\"name\":\"default\",\"url\":\"assets/Petition-Icon.png\"}]");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
