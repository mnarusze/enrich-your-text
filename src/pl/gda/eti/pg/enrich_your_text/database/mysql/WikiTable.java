package pl.gda.eti.pg.enrich_your_text.database.mysql;

import java.util.ArrayList;

public class WikiTable {

    private final String name;
    private final ArrayList<WikiColumn> columns;
    private final int primaryKey;

    public WikiTable(String name) {
        this.name = name;
        this.primaryKey = 0;
        columns = new ArrayList<>();
    }

    public String getInsertQuery() {
        int columnsToInsert = 0;
        String insertQuery = "INSERT ";

        for (WikiColumn column : columns) {
            if (column.getType().contains("AUTO_INCREMENT")) {
                insertQuery += "IGNORE ";
                break;
            }
        }
        insertQuery += "INTO " + name + "(";
        for (WikiColumn column : columns) {
            if (column.getType().contains("AUTO_INCREMENT")) {
                continue;
            }
            columnsToInsert++;
            insertQuery += column.getName() + ", ";
        }
        insertQuery = insertQuery.substring(0, insertQuery.length() - 2);
        insertQuery += ") VALUES (";
        for (int i = 0; i < columnsToInsert; i++) {
            insertQuery += "?, ";
        }
        insertQuery = insertQuery.substring(0, insertQuery.length() - 2);
        insertQuery += ");";
        return insertQuery;
    }

    public String getCreateQuery() {
        String query = "CREATE TABLE " + name + " (";
        for (int i = 0; i < columns.size(); i++) {
            query += columns.get(i).getName() + " ";
            query += columns.get(i).getType() + ", ";
        }
        query += "PRIMARY KEY (" + getPrimaryKeyName() + ")";
        query += ") ENGINE=MYISAM DEFAULT CHARSET=binary;";
        return query;
    }

    // Returns query string to get ID for a target name
    public String getSelectIdQuery() {
        return "SELECT " + columns.get(0).getName() + " FROM "
                + this.name + " WHERE "
                + columns.get(1).getName() + " = ?";
    }

    // Returns query string to get IDs for a target value from given column
    public String getSelectColumnIdQuery(Integer columnFrom) {
        return "SELECT * FROM "
                + this.name + " WHERE "
                + columns.get(columnFrom).getName() + " = ?";
    }

    // Returns query string to get ID for a target name
    public String getSelectNameQuery() {
        return "SELECT " + columns.get(1).getName() + " FROM "
                + name + " WHERE "
                + columns.get(0).getName() + " = ?";
    }

    public String getDropQuery() {
        return "DROP TABLE IF EXISTS " + name;
    }

    public void addColumn(WikiColumn column) {
        this.columns.add(column);
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public String getPrimaryKeyName() {
        return columns.get(primaryKey).getName();
    }

    public String getName() {
        return name;
    }

    public ArrayList<WikiColumn> getColumns() {
        return columns;
    }

    public String getCountQuery() {
        return "SELECT COUNT(1) FROM " + name + ";";
    }
}
