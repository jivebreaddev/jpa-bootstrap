package persistence.sql.ddl.builder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.meta.MetaEntity;
import persistence.sql.fixture.PersonFixture;

@DisplayName("1.2 Entity를 DROP DLL로 변환합니다.")
public class DropQueryBuilderTest {
  private static Class<PersonFixture> person;
  private DropQueryBuilder dropQueryBuilder = new DropQueryBuilder();

  @BeforeAll
  static void setup() {
    person = PersonFixture.class;
  }

  @Test
  @DisplayName("1.2.4.1 @Entity, @id가 표기된 class의 TABLE DROP DDL을 생성합니다.")
  public void dropDdlFromEntityClass() {
    MetaEntity<PersonFixture> meta = MetaEntity.of(person);

    String query = dropQueryBuilder.createDropQuery(meta.getTableName());

    assertThat(query).isEqualTo("DROP TABLE USERS;");

  }
}