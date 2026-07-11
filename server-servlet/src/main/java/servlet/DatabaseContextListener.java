package servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DatabaseContextListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        DatabaseUtil.close();
    }
}
