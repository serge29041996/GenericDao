package com.serge.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Realization generic dao.
 */
public class AbstractDao<T, ID> implements GenericDao<T, ID> {
  protected final Connection connection;
  private static final Logger logger = LogManager.getLogger(AbstractDao.class);
  private final Class<T> entityClass = ((Class<T>) ((ParameterizedType) getClass()
      .getGenericSuperclass())
      .getActualTypeArguments()[0]);
  private final String tableName = entityClass.getAnnotation(Table.class)
      .name()
      .toUpperCase();
  private final Field[] classFields = entityClass.getDeclaredFields();

  protected AbstractDao(Connection connection) {
    this.connection = connection;
  }

  @Override
  public T save(T t) {
    try {
      List<Object> parametersValuesList = getValueOfFields(t)
          .orElseThrow(IllegalAccessException::new);
      Field fieldId = findFieldWithId().orElseThrow(IllegalAccessException::new);
      String insertQuery = formatInsertQuery();
      PreparedStatement preparedStatement = connection.prepareStatement(insertQuery,
          Statement.RETURN_GENERATED_KEYS);
      for (int i = 1; i < parametersValuesList.size(); i++) {
        preparedStatement.setObject(i, parametersValuesList.get(i));
      }
      preparedStatement.execute();
      ResultSet resultSet = preparedStatement.getGeneratedKeys();
      if (resultSet.next()) {
        fieldId.set(t, resultSet.getObject(1));
        return t;
      } else {
        throw new NullPointerException("Unable save object");
      }
    } catch (IllegalAccessException e) {
      logger.error("Unable access to the field ", e);
      throw new NullPointerException(e.getMessage());
    } catch (SQLException e) {
      logger.error("Unable save object ", e);
      throw new NullPointerException("Unable save object " + e.getMessage());
    }
  }

  @Override
  public T get(ID id) {
    try {
      StringBuilder partQuery = formatSelectQuery();
      partQuery.append(" ");
      partQuery.append("WHERE ID = ?");
      PreparedStatement ps = connection.prepareStatement(partQuery.toString());
      ps.setObject(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return readEntityFromResultSet(rs);
      } else {
        throw new NullPointerException("No object in database");
      }
    } catch (SQLException e) {
      logger.error("Unable get object ", e);
      throw new NullPointerException("Unable get object " + e.getMessage());
    }
  }

  @Override
  public T update(T t) {
    try {
      List<Object> parametersValueList = getValueOfFields(t)
          .orElseThrow(IllegalAccessException::new);
      String updateQuery = formatUpdateQuery();
      PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
      for (int i = 1; i < parametersValueList.size(); i++) {
        preparedStatement.setObject(i, parametersValueList.get(i));
      }
      preparedStatement.setObject(parametersValueList.size(), parametersValueList.get(0));
      if (preparedStatement.executeUpdate() > 0) {
        return t;
      } else {
        throw new NullPointerException("Unable update object");
      }
    } catch (SQLException e) {
      logger.error("Unable update object ", e);
      throw new NullPointerException("Unable save object " + e.getMessage());
    } catch (IllegalAccessException e) {
      logger.error("Unable access to the field ", e);
      throw new NullPointerException(e.getMessage());
    }
  }

  @Override
  public void delete(ID id) {
    try {
      StringBuilder partQuery = new StringBuilder("DELETE FROM ");
      partQuery.append(tableName);
      partQuery.append(" WHERE ID = ?");
      PreparedStatement preparedStatement = connection.prepareStatement(partQuery.toString());
      preparedStatement.setObject(1, id);
      preparedStatement.execute();
    } catch (SQLException e) {
      logger.error("Unable delete object ", e);
    }
  }

  @Override
  public List<T> getAll() {
    try {
      StringBuilder partQuery = formatSelectQuery();
      PreparedStatement preparedStatement = connection.prepareStatement(partQuery.toString());
      ResultSet resultSet = preparedStatement.executeQuery();
      List<T> objects = new ArrayList<>();
      while (resultSet.next()) {
        objects.add(readEntityFromResultSet(resultSet));
      }
      return objects;
    } catch (SQLException e) {
      logger.error("Unable get all students ", e);
      return new ArrayList<>();
    }
  }

  private Optional<Field> findFieldWithId() {
    for (Field currentField : classFields) {
      if (currentField.getAnnotation(Id.class) != null) {
        return Optional.of(currentField);
      }
    }
    return Optional.empty();
  }

  private Optional<List<Object>> getValueOfFields(T t) {
    try {
      List<Object> valueFields = new ArrayList<>();
      for (Field currentField : classFields) {
        currentField.setAccessible(true);
        valueFields.add(currentField.get(t));
      }
      return Optional.of(valueFields);
    } catch (IllegalAccessException e) {
      logger.error("Unable access to the field ", e);
      return Optional.empty();
    }
  }

  private String formatInsertQuery() {
    StringBuilder firstPartQuery = new StringBuilder("INSERT INTO ");
    StringBuilder secondPartQuery = new StringBuilder("VALUES(");
    firstPartQuery.append(tableName);
    firstPartQuery.append("(");
    for (int i = 0; i < classFields.length; i++) {
      Field classField = classFields[i];
      if (classField.getAnnotation(Id.class) == null) {
        addColumnName(firstPartQuery, classField);
        secondPartQuery.append("?");
        addCommaToQuery(firstPartQuery, i);
        addCommaToQuery(secondPartQuery, i);
      }
    }
    firstPartQuery.append(") ");
    secondPartQuery.append(")");
    firstPartQuery.append(secondPartQuery);
    return firstPartQuery.toString();
  }

  private void addColumnName(StringBuilder partQuery, Field fieldWithColumn) {
    Column nameColumnAnnotation = fieldWithColumn.getAnnotation(Column.class);
    partQuery.append(nameColumnAnnotation.name().toUpperCase());
  }

  private StringBuilder formatSelectQuery() {
    StringBuilder partQuery = new StringBuilder("SELECT ID,");
    addColumnNamesAndAdditionalCharactersForQuery(partQuery, "");
    partQuery.append(" FROM ");
    partQuery.append(tableName);
    return partQuery;
  }

  private Optional<Constructor> getNoArgsConstructor() {
    Constructor[] constructors = entityClass.getDeclaredConstructors();
    for (Constructor constructor : constructors) {
      if (constructor.getParameterTypes().length == 0) {
        return Optional.of(constructor);
      }
    }
    return Optional.empty();
  }

  private T readEntityFromResultSet(ResultSet resultSet) {
    Optional<Constructor> noArgConstructor = getNoArgsConstructor();
    Constructor gettingNoArgConstructor = null;
    try {
      gettingNoArgConstructor = noArgConstructor
          .orElseThrow(InstantiationException::new);
      gettingNoArgConstructor.setAccessible(true);
      Object findObject = gettingNoArgConstructor.newInstance();
      for (int i = 0; i < classFields.length; i++) {
        Field currentField = classFields[i];
        currentField.setAccessible(true);
        currentField.set(findObject, resultSet.getObject((i + 1)));
      }
      return (T) findObject;
    } catch (IllegalAccessException e) {
      logger.error("No args constructor is not accessible ", e);
      throw new NullPointerException("Constructor is not accessible " + e.getMessage());
    } catch (InstantiationException e) {
      logger.error("Cannot initialize object ", e);
      return null;
    } catch (InvocationTargetException e) {
      logger.error("Constructor throw exception ", e);
      return null;
    } catch (SQLException e) {
      logger.error("Unable get object ", e);
      return null;
    }
  }

  private String formatUpdateQuery() {
    StringBuilder partQuery = new StringBuilder("UPDATE ");
    partQuery.append(tableName);
    partQuery.append(" ");
    partQuery.append("SET ");
    addColumnNamesAndAdditionalCharactersForQuery(partQuery, " = ?");
    partQuery.append(" WHERE ID = ?");
    return partQuery.toString();
  }

  private void addCommaToQuery(StringBuilder query, int currentIndexField) {
    if (currentIndexField < classFields.length - 1) {
      query.append(",");
    }
  }

  private void addColumnNamesAndAdditionalCharactersForQuery(StringBuilder query,
      String additionalCharacters) {
    for (int i = 0; i < classFields.length; i++) {
      Field classField = classFields[i];
      if (classField.getAnnotation(Id.class) == null) {
        addColumnName(query, classField);
        query.append(additionalCharacters);
        addCommaToQuery(query, i);
      }
    }
  }
}
