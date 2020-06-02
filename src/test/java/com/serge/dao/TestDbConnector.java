package com.serge.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class for getting access to test database.
 */
public class TestDbConnector {
  private static final String URL_TO_DATABASE = "jdbc:h2:tcp://localhost/~/test";
  private static final String LOGIN = "sa";
  private static final String PASSWORD = "";
  private static final Logger logger = LogManager.getLogger(TestDbConnector.class);

  private TestDbConnector() {
  }

  public static Connection getConnection() {
    try {
      Class.forName("org.h2.Driver");
      return DriverManager.getConnection(URL_TO_DATABASE, LOGIN, PASSWORD);
    } catch (SQLException e) {
      logger.error("Unable connect to database", e);
      throw new NullPointerException("Unable connect to database " + e.getMessage());
    } catch (ClassNotFoundException e) {
      logger.error("Unable find driver class", e);
      throw new NullPointerException("Unable find driver class " + e.getMessage());
    }
  }
}
