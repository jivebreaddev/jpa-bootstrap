package persistence.sql.dml.builder.read;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
TODO 이제 추상화할 수 있는 부분이 보이네요;;;
SELECT 절, WHERE 절다 분리가 가능해보이네요
하나의 클래스에 밀어넣는게 냄새가 나기 시작하네요.
 */
public class JoinQueryBuilder {
  private static final String PERIOD = ".";
  private static final String DELIMITER = ",";
  private String selectClause;
  private String tableName;
  private List<String> tablesToJoin;
  private List<String> foreignKey;
  private String whereColumn;
  private List<String> whereValues;

  public JoinQueryBuilder select(List<String> selectColumnsTable, List<String> selectColumnsTableToJoin){

    String selectColumns = String.join(DELIMITER, selectColumnsTable);
    String selectColumnsJoin = String.join(DELIMITER, selectColumnsTableToJoin);

    selectClause = String.join(DELIMITER, selectColumns, selectColumnsJoin);

    return this;
  }
  public JoinQueryBuilder from(String table){
    tableName = table;
    return this;
  }
  public JoinQueryBuilder join(List<String> tablesToJoin){
    this.tablesToJoin = tablesToJoin;

    return this;
  }

  public JoinQueryBuilder on(List<String> foreignKey){
    if (tablesToJoin.size() != foreignKey.size()){
      throw new RuntimeException("JOIN ON FK 가 필요합니다.");
    }
    List<String> tableForeignKeys = new ArrayList<>();

    for (int i = 0; i < foreignKey.size(); i++) {
      tableForeignKeys.add(String.join(PERIOD, tablesToJoin.get(i), foreignKey.get(i)));
    }

    this.foreignKey = tableForeignKeys;

    return this;
  }

  public JoinQueryBuilder where(String targetName, List<String> targets){
    this.whereColumn = String.join(PERIOD, List.of(targetName));
    this.whereValues = targets;

    return this;
  }

  public JoinQuery build(){
    return new JoinQuery(selectClause, tableName, Collections.unmodifiableList(tablesToJoin), Collections.unmodifiableList(foreignKey),
        whereColumn, Collections.unmodifiableList(whereValues));
  }
}
