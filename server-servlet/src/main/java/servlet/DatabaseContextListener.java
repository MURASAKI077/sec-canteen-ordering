package servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DatabaseContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            int migrated = PasswordUtil.migrateLegacyPasswords();
            event.getServletContext().log("Migrated " + migrated + " legacy passwords to BCrypt");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to migrate legacy passwords", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        DatabaseUtil.close();
    }
}
