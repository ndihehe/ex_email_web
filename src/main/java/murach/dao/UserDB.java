package murach.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import murach.model.User;
import murach.util.DBUtil;

import javax.persistence.*;

public class UserDB {

    public static boolean emailExists(String email) {
        EntityManagerFactory emf = DBUtil.getEmFactory();
        EntityManager em = emf.createEntityManager();

        String query = "SELECT u FROM User u WHERE u.Email = :email";
        TypedQuery<User> q = em.createQuery(query, User.class);
        q.setParameter("email", email);
        q.setMaxResults(1);

        try {
            java.util.List<User> list = q.getResultList();
            return list != null && !list.isEmpty();
        } finally {
            em.close();
        }
    }

    public static void insert(User user) {
            EntityManagerFactory emf = DBUtil.getEmFactory();
            EntityManager em = emf.createEntityManager();

            EntityTransaction tran = em.getTransaction();

            tran.begin();
            try {
                em.persist(user);
                tran.commit();
            }
            catch (Exception e) {
                System.out.println(e);
                tran.rollback();
            }
            finally {
                em.close();
            }
    }
}
