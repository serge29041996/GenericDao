package com.serge.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 * Class for getting access to test database.
 */
public class TestDbConnector {
  private static final String URL_TO_DATABASE = "jdbc:h2:tcp://localhost/~/test";
  private static final String LOGIN = "sa";
  private static final String PASSWORD = "";
  private static final Logger logger = Logger.getLogger(TestDbConnector.class);

  private TestDbConnector() {
  }

  public static Connection getConnection() {
    try {
      Class.forName("org.h2.Driver");
      return DriverManager.getConnection(URL_TO_DATABASE, LOGIN, PASSWORD);
    } catch (SQLException e) {
      logger.error("Unable connect to database", e);
      return null;
    } catch (ClassNotFoundException e) {
      logger.error("Unable find driver class", e);
      return null;
    }
  }
}
