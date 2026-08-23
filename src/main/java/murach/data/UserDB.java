package murach.data;

import murach.business.User;
import java.util.ArrayList;
import java.util.List;

public class UserDB {

    // danh sách lưu tạm trong bộ nhớ, dùng static để dùng chung cho cả app
    private static List<User> users = new ArrayList<>();

    public static void insert(User user) {
        users.add(user);
        System.out.println("Đã thêm user: " + user);
    }

    public static List<User> getAll() {
        return users;
    }
}