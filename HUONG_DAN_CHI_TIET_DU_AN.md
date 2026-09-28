# HƯỚNG DẪN CHI TIẾT KIẾN TRÚC DỰ ÁN & GIẢI MÃ CONNECTIONPOOL (BẢN TỰ VIẾT JNDI)

---

## 1. TỔNG QUAN KIẾN TRÚC HỆ THỐNG

Dự án được xây dựng theo mô hình chuẩn của các hệ thống phần mềm doanh nghiệp: **Kiến trúc 3 lớp (3-Tier Architecture)** kết hợp mẫu thiết kế **MVC (Model - View - Controller)** theo giáo trình Murach's Java Servlets and JSP (Chương 12).

```
[ GIAO DIỆN (View) ]
  ├── index.jsp   : Form nhập Email, Họ, Tên
  └── thanks.jsp  : Trang cảm ơn, hiển thị thông tin đã đăng ký thành công
         │
         ▼  (Gửi HTTP Request qua URL /emailList)
[ TẦNG ĐIỀU KHIỂN (Controller) ]
  └── EmailListServlet.java : Tiếp nhận yêu cầu, điều phối luồng, chuyển tiếp giao diện
         │
         ▼  (Gọi phương thức nghiệp vụ)
[ TẦNG DỊCH VỤ NGHIỆP VỤ (Service Layer) ]
  └── UserService.java : Kiểm tra tính hợp lệ dữ liệu (Validation), xử lý logic nghiệp vụ
         │
         ▼  (Gọi thao tác dữ liệu)
[ TẦNG TRUY CẬP DỮ LIỆU (Data Access Object - DAO Layer) ]
  └── UserDB.java : Chứa các câu lệnh SQL (SELECT, INSERT)
         │
         ├───────────────────────────────┐
         ▼                               ▼
[ TIỆN ÍCH DỰ ÁN (Utility Layer) ]   [ ĐỐI TƯỢNG DỮ LIỆU (Model / JavaBean) ]
  ├── ConnectionPool.java              └── User.java : Đại diện cho 1 bản ghi người dùng
  └── DBUtil.java
         │
         ▼ (Mượn kết nối kết nối qua JNDI DataSource)
[ HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU (Database) ]
  └── Microsoft SQL Server (Database: MurachDB, Bảng: Users)
```

---

## 2. TÁC DỤNG CỦA TỪNG FILE VÀ THƯ MỤC TRONG DỰ ÁN

### A. Thư mục Giao diện Web (`src/main/webapp/`)

#### 1. `index.jsp` (Giao Diện Đăng Ký)
* **Tác dụng:** Hiển thị form để người dùng điền thông tin (Email, First Name, Last Name).
* **Đặc điểm kỹ thuật:**
  * Thẻ form gửi dữ liệu về Controller thông qua phương thức `POST`: `<form action="emailList" method="post">`.
  * Gửi kèm tham số ẩn: `<input type="hidden" name="action" value="add">` để Controller nhận diện hành động.
  * Hiển thị thông báo lỗi bằng Expression Language (EL): `<p class="message">${message}</p>`.
  * Tự động điền lại thông tin người dùng vừa nhập nếu có lỗi (Sticky inputs): `value="${user.email}"`.

#### 2. `thanks.jsp` (Giao Diện Xác Nhận)
* **Tác dụng:** Trang hiển thị lời cảm ơn sau khi người dùng đăng ký tài khoản thành công.
* **Đặc điểm kỹ thuật:**
  * Lấy đối tượng `user` từ `HttpServletRequest` thông qua cú pháp EL: `${user.email}`, `${user.firstName}`, `${user.lastName}`.
  * Có nút "Return" (gửi action `join`) để người dùng có thể quay lại trang đăng ký ban đầu.

#### 3. `styles/main.css` (Định Kiểu Giao Diện)
* **Tác dụng:** Định nghĩa giao diện, màu sắc, khoảng cách, căn chỉnh form và định dạng chữ đỏ nổi bật cho các thông báo lỗi (`.message`).

---

### B. Thư mục Cơ sở dữ liệu (`src/main/resources/database/`)

#### 4. `user.sql` (Kịch Bản Tạo Bảng)
* **Tác dụng:** Chứa mã lệnh SQL DDL khởi tạo cơ sở dữ liệu `MurachDB` và bảng `Users` trên Microsoft SQL Server:
  * `UserID INT IDENTITY(1,1) PRIMARY KEY`: Khóa chính tự động tăng.
  * `Email NVARCHAR(100) UNIQUE NOT NULL`: Hòm thư điện tử, không được trùng lặp.
  * `FirstName NVARCHAR(50) NOT NULL`: Họ của người dùng.
  * `LastName NVARCHAR(50) NOT NULL`: Tên của người dùng.

---

### C. Thư mục Mã nguồn Java (`src/main/java/murach/`)

#### 5. `model/User.java` (JavaBean / Thực Thể Dữ Liệu)
* **Tác dụng:** Đại diện cho một đối tượng người dùng trong hệ thống (ánh xạ 1-1 với một dòng trong bảng `Users`).
* **Đặc điểm kỹ thuật:**
  * Tuân thủ chuẩn **JavaBean**:
    * Triển khai giao diện `java.io.Serializable` (cho phép tuần tự hóa để lưu vào Session nếu cần).
    * Có constructor không tham số mặc định và constructor đầy đủ tham số.
    * Các thuộc tính (`email`, `firstName`, `lastName`) đều có phạm vi truy cập `private`.
    * Cung cấp đầy đủ các phương thức `getter` và `setter` công khai.

#### 6. `controller/EmailListServlet.java` (Bộ Điều Khiển Trung Tâm - Controller)
* **Tác dụng:** Tiếp nhận yêu cầu HTTP từ trình duyệt gửi lên, điều phối xử lý và quyết định chuyển hướng trang tiếp theo.
* **Đặc điểm kỹ thuật:**
  * Đánh dấu bằng Annotation `@WebServlet("/emailList")`.
  * **Chỉ làm việc với tầng `UserService`**, tuyệt đối không gọi trực tiếp tầng DAO (`UserDB`), đảm bảo đúng chuẩn phân tầng lỏng (Loose Coupling).
  * Điều phối 2 hành động chính (`action`):
    * `join`: Điều hướng người dùng về trang `index.jsp`.
    * `add`: Lấy dữ liệu từ form, tạo đối tượng `User`, chuyển cho `UserService` xử lý, sau đó điều hướng sang `thanks.jsp` (nếu thành công) hoặc quay lại `index.jsp` kèm thông báo lỗi (nếu thất bại).

#### 7. `service/UserService.java` (Tầng Dịch Vụ Nghiệp Vụ - Business Logic)
* **Tác dụng:** Chứa toàn bộ các quy tắc và logic nghiệp vụ của ứng dụng:
  * Kiểm tra dữ liệu rỗng: Không cho phép để trống Email, Họ hoặc Tên.
  * Kiểm tra tài khoản tồn tại: Gọi `UserDB.emailExists(email)`. Nếu email đã được đăng ký, trả về thông báo lỗi: *"This email address already exists. Please enter another email address."*
  * Đăng ký người dùng: Nếu mọi điều kiện hợp lệ, gọi `UserDB.insert(user)` để lưu dữ liệu xuống database.
* **Ý nghĩa kiến trúc:** Tách biệt hoàn toàn phần logic khỏi Controller. Sau này nếu muốn đổi quy định mật khẩu, kiểm tra định dạng email,... chỉ cần sửa tại `UserService` mà không cần chạm vào Servlet hay DAO.

#### 8. `dao/UserDB.java` (Tầng Thao Tác CSDL - Data Access Object)
* **Tác dụng:** Đảm nhận việc giao tiếp trực tiếp với Microsoft SQL Server bằng câu lệnh SQL chuẩn:
  * `emailExists(email)`: Dùng `PreparedStatement` chạy câu lệnh `SELECT Email FROM Users WHERE Email = ?`. Nếu `rs.next() == true` tức là email đã có trong hệ thống.
  * `insert(user)`: Dùng `PreparedStatement` chạy `INSERT INTO Users (Email, FirstName, LastName) VALUES (?, ?, ?)`.
* **Cơ chế:** Mượn kết nối từ `ConnectionPool.getInstance().getConnection()` và luôn đóng tài nguyên trong khối `finally` bằng `DBUtil` và `pool.freeConnection(connection)`.

#### 9. `util/DBUtil.java` (Tiện Ích Đóng Tài Nguyên JDBC)
* **Tác dụng:** Cung cấp các phương thức `static` để đóng an toàn `Statement`, `PreparedStatement` và `ResultSet`.
* **Ý nghĩa:** Tự động kiểm tra `null` và bắt ngoại lệ `SQLException`, giúp code trong tầng DAO luôn ngắn gọn, sạch đẹp, không bị lỗi nuốt tài nguyên.

---

### D. File Cấu hình Dự án: `pom.xml`
* **Tác dụng:** Khai báo cấu hình dự án Maven và các thư viện cần thiết:
  * `javax.servlet-api: 4.0.1`: Thư viện lõi viết Servlet.
  * `javax.servlet.jsp-api: 2.3.3`: Thư viện xử lý JSP.
  * `jstl: 1.2`: Thư viện thẻ JavaServer Pages Standard Tag Library.
  * `mssql-jdbc: 12.4.2.jre11`: Trình điều khiển kết nối chính thức của Microsoft cho SQL Server.

---

## 3. LUỒNG THỰC THI TOÀN BỘ CHƯƠNG TRÌNH (STEP-BY-STEP DATA FLOW)

```
[ Trình duyệt Web (Browser) ]
       │
       │ 1. Người dùng bấm nút "Join Now"
       │    HTTP POST gửi tới /emailList (action=add, email, firstName, lastName)
       ▼
[ EmailListServlet.java ]
       │
       │ 2. Đọc các tham số request.getParameter(...)
       │ 3. Tạo JavaBean: User user = new User(email, firstName, lastName);
       │ 4. Chuyển tiếp tới tầng Service: userService.registerUser(user);
       ▼
[ UserService.java ]
       │
       │ 5. Kiểm tra tính hợp lệ: dữ liệu có bị rỗng hay không?
       │ 6. Nếu dữ liệu đầy đủ -> Gọi DAO kiểm tra trùng lặp: UserDB.emailExists(email)
       ▼
[ UserDB.java ]
       │
       │ 7. Mượn kết nối từ hồ:
       │    ConnectionPool pool = ConnectionPool.getInstance();
       │    Connection connection = pool.getConnection();
       │ 8. Thực thi PreparedStatement truy vấn bảng Users trong SQL Server
       │ 9. Khối finally:
       │    - DBUtil.closeResultSet(rs);
       │    - DBUtil.closePreparedStatement(ps);
       │    - pool.freeConnection(connection); // TRẢ KẾT NỐI VỀ HỒ
       ▼
[ UserService.java ]
       │
       │ 10. Tiếp nhận kết quả từ UserDB:
       │     - Nếu email đã tồn tại -> Trả về chuỗi thông báo lỗi.
       │     - Nếu email chưa có -> Gọi tiếp UserDB.insert(user) để lưu vào SQL Server.
       │     - Nếu lưu thành công -> Trả về chuỗi rỗng "".
       ▼
[ EmailListServlet.java ]
       │
       │ 11. Đánh giá chuỗi phản hồi từ Service:
       │     - NẾU CÓ LỖI:
       │         request.setAttribute("message", errorMessage);
       │         request.setAttribute("user", user);
       │         Forward về -> /index.jsp
       │     - NẾU THÀNH CÔNG:
       │         request.setAttribute("user", user);
       │         Forward sang -> /thanks.jsp
       ▼
[ Trình duyệt Web (Browser) ]
       Hiển thị kết quả tương ứng cho người dùng.
```

---

## 4. GIẢI MÃ BẢN CHẤT FILE `ConnectionPool.java` (BẢN TỰ VIẾT JNDI CHUẨN SLIDE 12 MURACH)

File mã nguồn: `src/main/java/murach/util/ConnectionPool.java`

Dưới đây là **nguyên văn 100% mã nguồn** của class `ConnectionPool` đang có trong dự án của bạn:

```java
package murach.util;

import java.sql.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ConnectionPool {

    private static ConnectionPool pool = null;
    private static DataSource dataSource = null;

    private ConnectionPool() {
        try {
            InitialContext ic = new InitialContext();
            dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/murach");
        } catch (NamingException e) {
            System.out.println(e);
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
    }

    public void freeConnection(Connection c) {
        try {
            c.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
```

---

### 4.1. Tại sao phải viết class `ConnectionPool` theo cách này? (Vấn đề thực tế)

Nếu không có `ConnectionPool`, mỗi khi người dùng bấm nút:
1. Java phải mở kết nối mạng socket TCP/IP đến SQL Server qua cổng 1433.
2. Hai bên trao đổi chứng thực bảo mật và cấp quyền cho tài khoản `sa`.
3. SQL Server tạo một tiến trình mới và cấp phát RAM cho kết nối này.
4. Chạy xong 1 câu SQL (mất 2ms) thì lại ngắt kết nối.

Toàn bộ quá trình tạo mới một kết nối tốn tới **100 - 300 mili-giây**. Khi có hàng trăm người truy cập cùng lúc, máy chủ sẽ bị treo hoặc sập database do hết kết nối vật lý.

Class `ConnectionPool` tự viết này sinh ra để đóng vai trò làm **người quản lý mượn - trả kết nối**:
* Nó không tự mở kết nối thủ công bằng `DriverManager`.
* Nó giao quyền quản lý các kết nối vật lý cho Web Server (Tomcat) thông qua đối tượng `DataSource`.
* Mã nguồn Java của chúng ta chỉ làm 2 việc: **Xin mượn kết nối** (`getConnection`) và **Trả lại kết nối** (`freeConnection`).

---

### 4.2. Mổ xẻ chi tiết từng khối mã nguồn

#### ① Mẫu thiết kế Singleton Pattern (Đảm bảo duy nhất 1 hồ chứa)
```java
private static ConnectionPool pool = null;
private static DataSource dataSource = null;

// Khóa hàm khởi tạo
private ConnectionPool() { ... }

// Cửa ngõ lấy đối tượng duy nhất
public static synchronized ConnectionPool getInstance() {
    if (pool == null) {
        pool = new ConnectionPool();
    }
    return pool;
}
```

* **Constructor có phạm vi `private`:**
  * Giúp **ngăn cản tuyệt đối** các class khác tùy tiện gõ `new ConnectionPool()`.
  * Nếu ai cũng `new` được, mỗi Servlet hay DAO sẽ tự tạo ra một hồ chứa riêng, làm mất đi ý nghĩa của việc chia sẻ tài nguyên kết nối dùng chung.
* **Hàm `getInstance()`:**
  * Là phương thức tĩnh (`static`), điểm truy cập toàn cục duy nhất để lấy đối tượng `ConnectionPool`.
  * **Cơ chế Lazy Initialization:**
    * Lần đầu tiên trong đời ứng dụng có một luồng gọi `ConnectionPool.getInstance()`, biến `pool` lúc này đang là `null` $ightarrow$ nó mới thực hiện `new ConnectionPool()`.
    * Từ lần thứ hai trở đi, biến `pool` đã có sẵn giá trị $ightarrow$ phương thức lập tức trả về đối tượng cũ mà **không bao giờ khởi tạo lại**.
* **Từ khóa `synchronized`:**
  * Đảm bảo **an toàn đa luồng (Thread-Safety)**.
  * Nếu có 10 người dùng bấm Submit cùng 1 phần triệu giây đầu tiên khi server mới bật, từ khóa này bắt các luồng phải xếp hàng đi qua lần lượt, ngăn chặn triệt để nguy cơ 2 luồng cùng nhìn thấy `pool == null` rồi sinh đôi 2 hồ chứa.

---

#### ② Cơ chế tra cứu danh bạ JNDI (`InitialContext` & `lookup`)
```java
private ConnectionPool() {
    try {
        InitialContext ic = new InitialContext();
        dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/murach");
    } catch (NamingException e) {
        System.out.println(e);
    }
}
```

* **JNDI (Java Naming and Directory Interface) là gì?**
  * JNDI là chuẩn của Java EE đóng vai trò như một **cuốn danh bạ nội bộ** được máy chủ ứng dụng (Tomcat) quản lý.
  * Thay vì ghi cứng (hardcode) URL cơ sở dữ liệu, username, password vào mã nguồn Java (vừa mất an toàn vừa khó bảo trì khi đổi server), ta cấu hình các thông số này ở phía máy chủ Tomcat (trong file `context.xml`).
  * Class `ConnectionPool` chỉ việc "mở danh bạ tra cứu" để xin hồ chứa `DataSource`.
* **Ý nghĩa từng dòng lệnh:**
  * `InitialContext ic = new InitialContext();`: Tạo đối tượng ngữ cảnh để bắt đầu tra cứu trong danh bạ JNDI của Tomcat.
  * `ic.lookup("java:/comp/env/jdbc/murach");`:
    * `java:/comp/env`: Tiền tố bắt buộc trong chuẩn Java EE đại diện cho không gian tên môi trường (Environment Naming Context) của ứng dụng Web hiện tại.
    * `jdbc/murach`: Tên định danh (Resource Name) của hồ chứa kết nối được đăng ký với Tomcat.
  * Ép kiểu kết quả trả về thành `(DataSource)` và gán vào biến tĩnh `dataSource`.

---

#### ③ Phương thức mượn kết nối: `getConnection()`
```java
public Connection getConnection() {
    try {
        return dataSource.getConnection();
    } catch (SQLException e) {
        System.out.println(e);
        return null;
    }
}
```
* Khi tầng DAO (`UserDB`) cần thực thi SQL, nó gọi `pool.getConnection()`.
* Hàm này gọi `dataSource.getConnection()`. Lúc này, Tomcat sẽ lấy **một kết nối rảnh rỗi đang nuôi sẵn trong hồ**, chuyển trạng thái của nó sang **"Đang bận" (In-use)** và trao cho bạn dùng.
* Nếu toàn bộ các kết nối trong hồ đều đang có người dùng, luồng này sẽ tự động xếp hàng chờ đến khi có kết nối được trả lại.

---

#### ④ Phương thức trả kết nối: `freeConnection(Connection c)`
```java
public void freeConnection(Connection c) {
    try {
        c.close();
    } catch (SQLException e) {
        System.out.println(e);
    }
}
```
* **Bản chất cực kỳ quan trọng của lệnh `c.close()` ở đây:**
  * Đối tượng `Connection` mà Tomcat cấp phát cho bạn thực chất không phải là kết nối nguyên thủy trần trụi, mà là một **Lớp vỏ bọc ủy quyền (Connection Proxy Wrapper)**.
  * Khi bạn gọi `c.close()`, nó **KHÔNG HỀ NGẮT KẾT NỐI VẬT LÝ VỚI SQL SERVER**.
  * Thay vào đó, lớp bọc ủy quyền này sẽ chặn lệnh đóng lại và gửi thông báo cho Tomcat: *"Tôi đã dùng xong, hãy đưa kết nối này từ trạng thái Bận trở về trạng thái Rảnh rỗi (Idle) trong hồ để người tiếp theo mượn"*.
* **Tại sao phải luôn đặt trong khối `finally`?**
  * Dù câu lệnh SQL chạy thành công hay bị lỗi văng Exception, khối `finally` luôn được đảm bảo thực thi.
  * Nhờ đó, kết nối **luôn luôn được trả về hồ chứa**, không bao giờ bị hiện tượng rò rỉ kết nối (Connection Leak) làm cạn kiệt tài nguyên hệ thống.

---

### 4.3. Bảng tóm tắt so sánh

| Tiêu chí | Kết nối truyền thống (`DriverManager`) | `ConnectionPool` tự viết (JNDI DataSource) |
| :--- | :--- | :--- |
| **Thời gian tạo kết nối** | Chậm (100 - 300 ms cho mỗi truy vấn) | Siêu nhanh (< 1 ms vì đã được mở sẵn) |
| **Bản chất hàm `.close()`** | Ngắt kết nối vật lý, hủy socket với SQL Server | Trả kết nối về trạng thái rảnh rỗi trong hồ |
| **Nơi lưu thông tin CSDL** | Hardcode thẳng trong mã nguồn Java | Khai báo tập trung ở Web Server (`context.xml`) |
| **Khả năng chịu tải** | Rất kém, dễ crash SQL Server khi đông người | Rất cao, luồng tự động xếp hàng chờ mượn |
