import java.util.List;
import java.util.Map;

public class Guitar {
    private int id;
    private String name;
    private String price;
    private List<String> images;
    private Map<String, String> features;
    private String description;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }
    public Map<String, String> getFeatures() {
        return features;
    }
    public void setFeatures(Map<String, String> features) {
        this.features = features;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
