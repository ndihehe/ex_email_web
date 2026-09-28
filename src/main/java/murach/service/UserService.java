package murach.service;

import murach.dao.UserDB;
import murach.model.User;

public class UserService {

    public String registerUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty() ||
                user.getFirstName() == null || user.getFirstName().trim().isEmpty() ||
                user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            return "Please fill in all form fields.";
        }

        if (UserDB.emailExists(user.getEmail())) {
            return "This email address already exists.<br>" +
                    "Please enter another email address.";
        }

       UserDB.insert(user);
        return null;
    }
}
