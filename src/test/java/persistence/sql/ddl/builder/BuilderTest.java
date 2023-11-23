package persistence.sql.ddl.builder;

import database.DatabaseServer;
import database.H2;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import persistence.entity.entry.EntityEntry;
import persistence.entity.entry.JdbcEntityEntry;
import persistence.entity.persistentcontext.JdbcPersistenceContext;
import persistence.entity.persistentcontext.PersistenceContext;
import persistence.meta.MetaEntity;
import persistence.meta.model.AnnotationBinder;
import persistence.meta.model.ComponentScanner;
import persistence.meta.model.MetaModel;
import persistence.sql.dml.builder.InsertQueryBuilder;
import persistence.sql.dml.builder.read.SelectQueryBuilder;
import domain.fixture.PersonFixtureStep3;
import domain.fixture.PersonInstances;

public class BuilderTest {

  public static DatabaseServer server;
  public static Class<PersonFixtureStep3> person;
  public static JdbcTemplate jdbcTemplate;
  public static MetaEntity<PersonFixtureStep3> meta;
  public static CreateQueryBuilder createQueryBuilder;
  public static InsertQueryBuilder insertQueryBuilder;
  public static SelectQueryBuilder selectQueryBuilder;
  public static Connection connection;
  public static PersistenceContext persistenceContext;
  public static EntityEntry entityEntry;
  public static String DELIMITER = ",";
  public static MetaModel metaModel;
  @BeforeAll
  static void setup() throws SQLException {
    person = PersonFixtureStep3.class;
    meta = MetaEntity.of(person);

    server = new H2();
    server.start();
    connection = server.getConnection();
    jdbcTemplate = new JdbcTemplate(connection);
    entityEntry = new JdbcEntityEntry();
    insertQueryBuilder = new InsertQueryBuilder();
    createQueryBuilder = new CreateQueryBuilder();
    selectQueryBuilder = new SelectQueryBuilder();
    String query = createQueryBuilder.createIfNotExistsCreateQuery(meta.getTableName(), meta.getColumns());
    jdbcTemplate.execute(query);

    ComponentScanner componentScanner = new ComponentScanner();

    try {
      metaModel = new AnnotationBinder(componentScanner).buildMetaModel(jdbcTemplate, "domain");
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void insert() {
    persistenceContext = new JdbcPersistenceContext();

    String queryFirst = insertQueryBuilder.createInsertQuery(meta.getTableName(),
        meta.getColumnClause(), meta.getValueClause(PersonInstances.첫번째사람));
    String querySecond = insertQueryBuilder.createInsertQuery(meta.getTableName(),
        meta.getColumnClause(), meta.getValueClause(PersonInstances.두번째사람));
    jdbcTemplate.execute(queryFirst);
    jdbcTemplate.execute(querySecond);
  }

  @AfterEach
  void delete() {
    persistenceContext = new JdbcPersistenceContext();

    String queryFirst = "truncate table USERS RESTART IDENTITY" ;
    jdbcTemplate.execute(queryFirst);
  }

  @AfterAll
  static void dropTable() {

    String queryFirst = "DROP TABLE USERS" ;
    jdbcTemplate.execute(queryFirst);
    server.stop();
  }
}
