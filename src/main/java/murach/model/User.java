package murach.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private int id;
    private String Email;
    private String firstName;
    private String lastName;

    public User() {
    }

    public User(String firstName, String lastName, String Email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.Email = Email;
    }

    public  int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

}
