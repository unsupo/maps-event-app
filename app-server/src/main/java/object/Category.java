package object;

import com.google.gson.reflect.TypeToken;
import config.ConfigManager;
import data.DataAdapter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utilities.FileOptions;
import utilities.database.Database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Category {
    private static Logger logger = LogManager.getLogger(Category.class);

    public static List<Category> getAllCategories() throws IOException, SQLException, ClassNotFoundException {
        List<Map<String, Object>> query = Database.newDatabaseConnection().executeQuery("select * from categories");
        List<Category> categories = new ArrayList<>();
        for(Map<String, Object> map : query)
            categories.add(
                    FileOptions.getGson().fromJson(FileOptions.getGson().toJson(map),Category.class)
            );
        return categories;
    }

    public static void addCategories(String categories) throws IOException, SQLException, ClassNotFoundException {
        List<Category> cats = FileOptions.getGson().fromJson(categories,
                new TypeToken<List<Category>>(){}.getType());
        cats.addAll(getAllCategories());
        cats = new ArrayList<>(new HashSet<>(cats));
        DataAdapter.writeCategories(cats);
    }

    private String name, url;
    private byte[] imageBytes;

    protected Category(){}

    public Category(String name){
        this.name = name;
    }

    public Category(String name, String url) throws IOException {
        this.name = name;

        if(url.contains("res:"))
            this.imageBytes = FileOptions.getImageBytesFromStream(url.replaceFirst("res:",""));

        this.url = url;
    }


    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() throws IOException {
        if(url.contains("res:"))
            this.imageBytes = FileOptions.getImageBytesFromStream(url.replaceFirst("res:",""));
        return url;
    }

    public void setUrl(String url) throws IOException {
        if(url.contains("res:"))
            this.imageBytes = FileOptions.getImageBytesFromStream(url.replaceFirst("res:",""));
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return name != null ? name.equals(category.name) : category.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
