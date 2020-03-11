package Library;
import javax.crypto.*;
import java.security.*;

public class User {

    private String name;
    private PublicKey publicKey;
    private int id;

    public User(String name, PublicKey publicKey, int id) {
        this.name = name;
        this.publicKey = publicKey;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}