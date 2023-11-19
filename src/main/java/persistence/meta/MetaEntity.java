package persistence.meta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import persistence.dialect.h2.H2Dialect;
import persistence.meta.relation.Relation;

public class MetaEntity<T> {

  private static final String DELIMITER = ",";
  private final MetaDataTable metaDataTable;
  private final MetaDataColumns metaDataColumns;
  private final MetaDataColumn primaryKeyColumn;
  private final Class<T> clazz;

  private MetaEntity(MetaDataTable metaDataTable, MetaDataColumns metaDataColumns, Class<T> clazz) {
    this.metaDataTable = metaDataTable;
    this.metaDataColumns = metaDataColumns;
    this.primaryKeyColumn = metaDataColumns.getPrimaryColumn();
    this.clazz = clazz;
  }

  public static <T> MetaEntity<T> of(Class<T> clazz) {
    return new MetaEntity<>(MetaDataTable.of(clazz), MetaDataColumns.of(clazz, new H2Dialect()),
        clazz);
  }

  public String getTableName() {
    return metaDataTable.getName();
  }

  public MetaDataColumn getPrimaryKeyColumn() {
    return primaryKeyColumn;
  }

  public boolean getPrimaryKeyColumnIsNonNull(Object entity) {
    return Objects.nonNull(primaryKeyColumn.getFieldValue(entity));
  }

  public Long getPrimaryKeyColumnValue(Object entity) {
    return (Long) primaryKeyColumn.getFieldValue(entity);
  }

  public String getValueClause(Object entity) {
    List<String> columns = getEntityFields();

    return String.join(DELIMITER, extractValuesFromEntity(columns, entity));
  }

  public String getColumnClause() {
    List<String> columns = getEntityColumns();

    return String.join(DELIMITER, columns);
  }

  public MetaDataColumns getMetaDataColumns() {
    return metaDataColumns;
  }

  public String getColumns() {
    return metaDataColumns.getColumns();
  }

  public T createInstance() {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException e) {
      throw new RuntimeException("생성자 오류", e);
    }
  }

  public List<String> extractValuesFromEntity(List<String> columns, Object entity) {
    List<String> values = new ArrayList<>();

    for (String column : columns) {
      Field field = getField(column, entity);
      field.setAccessible(true);
      values.add(getFieldValue(field, entity));
    }
    return values;
  }

  private Field getField(String column, Object entity) {
    try {
      return entity.getClass().getDeclaredField(column);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFieldValue(Field field, Object entity) {
    try {
      Object value = field.get(entity);
      if (value.getClass().equals(String.class)) {
        return "'" + value.toString() + "'";
      }
      return value.toString();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public String getColumnClauseWithId() {
    List<String> columns = getEntityColumnsWithId();

    return String.join(DELIMITER, columns);
  }

  private List<String> getEntityColumns() {
    return metaDataColumns.getDBColumnsWithoutId();
  }

  private List<String> getEntityFields() {
    return metaDataColumns.getFields();
  }

  public List<String> getEntityColumnsWithId() {
    return metaDataColumns.getColumnsWithId();
  }

  public List<String> getEntityTableColumnsWithId() {
    return metaDataColumns.getColumnsWithId(metaDataTable.getName());
  }

  public Class<T> getClazz() {
    return clazz;
  }

  public boolean hasRelation() {
    return metaDataColumns.hasRelation();
  }

  public boolean isDbGeneratedKey() {
    return primaryKeyColumn.isGenerated();
  }

  public Relation getRelation() {
    return metaDataColumns.getRelation();
  }
}
