package database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DatabaseTest {

    @Test
    void createTable() {
        Database database = new Database(null);
        assertSame(Result.Status.OK, database.query("create table test1 (INT id, STR name)").getStatus());
        assertSame(Result.Status.OK, database.query("create table test2 (INT id, STR name)").getStatus());

        Result result = database.query("list tables");
        assertSame(Result.Status.OK, result.getStatus());
        assertSame(2, result.getRows().size());
    }

    @Test
    void dateRange() {
        Database database = new Database(null);
        assertSame(Result.Status.OK, database.query("create table test1 (DATE_RANGE dates)").getStatus());

        assertSame(Result.Status.OK, database.query("insert into test1 (dates) values(01-01-2011...01-01-2012)").getStatus());
        assertSame(Result.Status.FAIL, database.query("insert into test1 (dates) values(01-01-2012...01-01-2011)").getStatus());
    }

    @Test
    void cartesianProduct() {
        Database database = new Database(null);
        assertSame(Result.Status.OK, database.query("create table cats (INT id, STR name, STR breed)").getStatus());
        assertSame(Result.Status.OK, database.query("create table dogs (INT id, STR name, DATE birthday)").getStatus());

        assertSame(Result.Status.OK, database.query("insert into cats (id, name, breed) values(1, Tom, British)").getStatus());
        assertSame(Result.Status.OK, database.query("insert into cats (id) values(2)").getStatus());
        assertSame(Result.Status.OK, database.query("insert into cats (name) values(Murka)").getStatus());

        assertSame(Result.Status.OK, database.query("insert into dogs (id, name, birthday) values(1, Sharik, 01-01-2017)").getStatus());
        assertSame(Result.Status.OK, database.query("insert into dogs (id) values(2)").getStatus());

        Result result = database.query("cartesian product cats by dogs");
        assertSame(Result.Status.OK, result.getStatus());
        assertSame(6, result.getRows().size());
    }

    @Test
    void projectTable() {
        Database database = new Database(null);
        assertSame(Result.Status.OK, database.query("create table test1 (INT id, STR name)").getStatus());

        assertSame(Result.Status.OK, database.query("insert into test1 (id, name) values(1, abc)").getStatus());
        assertSame(Result.Status.OK, database.query("insert into test1 (id, name) values(2, xyz)").getStatus());

        Result result = database.query("select id from test1");
        assertSame(Result.Status.OK, result.getStatus());
        assertSame(2, result.getRows().size());
        assertNotNull(result.getRows().iterator().next().getElement("id"));
        assertNull(result.getRows().iterator().next().getElement("name"));

        result = database.query("select name,id from test1");
        assertSame(Result.Status.OK, result.getStatus());
        assertSame(2, result.getRows().size());
        assertNotNull(result.getRows().iterator().next().getElement("id"));
        assertNotNull(result.getRows().iterator().next().getElement("name"));
    }
}