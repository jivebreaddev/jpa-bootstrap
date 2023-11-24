package persistence.entity.loader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jdbc.JdbcRowMapper;
import jdbc.JdbcTemplate;
import persistence.meta.MetaDataColumn;
import persistence.meta.MetaEntity;
import persistence.sql.dml.builder.read.SelectQueryBuilder;

public class JdbcEntityLoader<T> implements EntityLoader<T> {

  private final JdbcTemplate jdbcTemplate;
  private final MetaEntity<T> metaEntity;
  private final SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder();
  private final JdbcRowMapper<T> jdbcRowMapper;

  public JdbcEntityLoader(MetaEntity<T> metaEntity, JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.metaEntity = metaEntity;
    this.jdbcRowMapper = new JdbcRowMapper<>(metaEntity);
  }

  @Override
  public Optional<T> load(Long id) {
    MetaDataColumn keyColumn = metaEntity.getPrimaryKeyColumn();
    String targetColumn = keyColumn.getDBColumnName();

    String query = selectQueryBuilder.createSelectByFieldQuery(metaEntity.getColumnClauseWithId(),
        metaEntity.getTableName(), targetColumn, id);

    return Optional.ofNullable(jdbcTemplate.queryForObject(query, jdbcRowMapper));
  }

  @Override
  public List<T> findAll() {
    String query = selectQueryBuilder.createSelectQuery(metaEntity.getColumnClauseWithId(),
        metaEntity.getTableName());
    return jdbcTemplate.query(query, jdbcRowMapper);
  }

  @Override
  public List<T> loadByIds(List<Long> ids) {
    MetaDataColumn keyColumn = metaEntity.getPrimaryKeyColumn();
    String targetColumn = keyColumn.getDBColumnName();

    List<String> idValues = ids.stream().map(id -> id.toString()).collect(Collectors.toList());

    String query = selectQueryBuilder.createSelectByFieldsQuery(metaEntity.getColumnClauseWithId(),
        metaEntity.getTableName(), targetColumn, idValues);

    return jdbcTemplate.query(query, jdbcRowMapper);
  }
}
