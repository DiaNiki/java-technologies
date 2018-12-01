package database;

public enum QueryRegex {
    // DELETE FROM <tablename> [WHERE column1=value1, column2=value2,...]
    DELETE_ROWS("^\\s*DELETE\\s+FROM\\s+([^\\s]+)(?:\\s+WHERE\\s+([^\\s]+))?\\s*$"),
    // INSERT INTO <tablename> (column1, column2, ...) VALUES (value1, value2, ...)
    INSERT_ROW("^\\s*INSERT\\s+INTO\\s+([^\\s]+)\\s+\\(([^\\)]+)\\)\\s+VALUES\\s*\\(([^\\)]+)\\)s*$"),
    // UPDATE <tablename> SET column1=value1, column2=value2, .. [WHERE column1.1=value1.1, column1.2=value1.2]
    UPDATE_ROWS("^\\s*UPDATE\\s+([^\\s]+)\\s+SET\\s*([^\\s]+)(?:\\s+WHERE\\s+([^\\s]+))?\\s*$"),
    // SELECT (column1, column2,...) FROM <tablename> [WHERE column1=value1, column2=value2,...]
    SELECT_ROWS("^\\s*SELECT\\s+(.+)\\s+FROM\\s+([^\\s]+)(?:\\s+WHERE\\s+([^\\s]+))?\\s*$"),

    // CREATE TABLE <tablename> (TYPE1 column1, TYPE2 column2,...)
    CREATE_TABLE("^\\s*CREATE\\s+TABLE\\s+([^\\s]+)\\s+\\(([^\\)]+)\\)\\s*$"),
    // DROP TABLE <tablename>
    DROP_TABLE("^\\s*DROP\\s+TABLE\\s+([^\\s]+)\\s*$"),
    // LIST TABLES
    LIST_TABLES("^\\s*(LIST\\s+TABLES)\\s*$"),

    // CARTESIAN PRODUCT <tablename1> BY <tablename2>
    CARTESIAN_PRODUCT("^\\s*CARTESIAN\\s+PRODUCT\\s+([^\\s]+)\\s+BY\\s+([^\\s]+)\\s*$");

    private String regex;
    public final String SUFFIX = "/i";

    QueryRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex + SUFFIX;
    }
}
