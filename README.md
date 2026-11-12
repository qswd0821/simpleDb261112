# simple-db

JDBC 를 직접 감싸서 SQL 을 쉽게 실행하는 유틸리티 `SimpleDb` · `Sql` 구현 과제입니다.

- Java · Gradle(Kotlin DSL) · JDK 21 (`options.release = 21`)
- 구현: `com.ll.simpleDb.SimpleDb`, `com.ll.simpleDb.Sql`, `com.ll.Article`
- 제공 테스트 `SimpleDbTest`(t001~t019)와 의존성은 그대로 두었습니다.

## 구조

| 클래스 | 역할 |
| --- | --- |
| `SimpleDb` | 접속 정보 보관, `run`·`genSql`·`setDevMode`·`startTransaction`·`commit`·`rollback`·`close`. 커넥션은 `ThreadLocal` 로 스레드마다 따로 둔다. |
| `Sql` | `append`/`appendIn` 체이닝과 `?` 바인딩. `appendIn` 은 `?` 를 값 개수만큼 펼친다. `insert`(주키)·`update`/`delete`(행 수)·각종 `select*` 를 제공한다. |
| `RowMapper` | 조회 행(Map)을 Jackson 으로 객체에 매핑한다. 컬럼 이름 = 필드 이름(`isBlind` 포함). |
| `Article` | 테스트용 엔티티 (Lombok `@Getter`). |

## 실행

```bash
docker run --name mysql-1 -p 127.0.0.1:3306:3306 -e TZ=Asia/Seoul -e MYSQL_ROOT_PASSWORD=lldj123414 -d mysql:8.4
docker exec mysql-1 mysqladmin ping -uroot -plldj123414
docker exec mysql-1 mysql -uroot -plldj123414 -e "CREATE DATABASE IF NOT EXISTS simpleDb__test CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
./gradlew test   # Windows: .\gradlew.bat test
```

## 테스트 결과

로컬(JDK 23 으로 빌드, `release 21` / MySQL 컨테이너)에서 `SimpleDbTest` 19개 모두 통과했습니다.
`tests="19" failures="0" errors="0" skipped="0"`
