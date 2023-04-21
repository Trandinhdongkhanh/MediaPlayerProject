package Model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Song implements Serializable {
    private String tittle;
    private String single;
    private String image;
    private String resource;
    private String mKey;

    public Song() {
    }

    public Song(String title, String single, String image, String resource) {
        if (title.trim().equals("")) {
            title = "No Name";
        }
        this.tittle = title;
        this.single = single;
        this.image = image;
        this.resource = resource;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String title) {
        this.tittle = title;
    }

    public String getSingle() {
        return single;
    }

    public void setSingle(String single) {
        this.single = single;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }
}
