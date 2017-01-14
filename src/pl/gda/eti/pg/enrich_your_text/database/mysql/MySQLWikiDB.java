package pl.gda.eti.pg.enrich_your_text.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase;
import pl.gda.eti.pg.enrich_your_text.extraction.WikipediaExtractor;
import pl.gda.eti.pg.enrich_your_text.models.WikiArticle;
import pl.gda.eti.pg.enrich_your_text.models.WikiLabel;
import pl.gda.eti.pg.enrich_your_text.models.WikiLink;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;

public class MySQLWikiDB extends WikiDatabase {

    private String mySQLConnectionString;

    private Integer commitsCounter;
    private Connection connection;
    private ArrayList<WikiTable> tables;
    protected String lastError;
    
    private static final int MAX_SQL_UPDATES_BEFORE_COMMIT = 5000;
    public final static Integer DEFAULT_PORT = 3306;

    @Override
    public Boolean initializeDatabase(WikipediaExtractor.ExtractionStep startingStep, Boolean forceIfExists, Boolean addIndexes) {
        commitsCounter = 0;
        try {
            Statement statement = connection.createStatement();
            statement = connection
                    .prepareStatement("INSERT INTO () VALUES (?, ?);");
            statement.executeQuery(getTable(WikiCollections.ARTICLES)
                    .getDropQuery());
            statement.executeQuery(getTable(WikiCollections.ARTICLES)
                    .getCreateQuery());
            statement.executeQuery(getTable(WikiCollections.LABELS)
                    .getDropQuery());
            statement.executeQuery(getTable(WikiCollections.LABELS)
                    .getCreateQuery());
            statement.executeQuery(getTable(WikiCollections.LABELS_STEMMED)
                    .getDropQuery());
            statement.executeQuery(getTable(WikiCollections.LABELS_STEMMED)
                    .getCreateQuery());
            statement.executeQuery("SET character_set_client = utf8");
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private Connection getConnection(String dbName, Integer port) throws SQLException {

        Connection conn;
        Properties connectionProperties = new Properties();

        mySQLConnectionString = "jdbc:mysql://localhost:" + port + "/" + dbName;

        // .put("user", username);
        // connectionProperties.put("password", password);

        conn = DriverManager.getConnection(mySQLConnectionString,
                connectionProperties);

        System.out.println("Connected to database");
        return conn;
    }

    public MySQLWikiDB() {

    }

    private void updateUpdatesCounter() {
        commitsCounter++;
        if (commitsCounter > MAX_SQL_UPDATES_BEFORE_COMMIT) {
            commitsCounter = 0;
            saveDatabase();
        }
    }

    private WikiTable getTable(WikiCollections table) {
        return tables.get(table.ordinal());
    }

    @Override
    public void saveDatabase() {
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveWikiLinks(WikiArticle article, Boolean stem) {
        try {
            PreparedStatement statement;
            ArrayList<WikiLink> links;

            // Articles + their IDs
            statement = connection.prepareStatement(getTable(
                    WikiCollections.ARTICLES).getInsertQuery());
            statement.setInt(1, article.getId());
            statement.setString(2, article.getTitle());
            statement.executeUpdate();
            updateUpdatesCounter();

            // Labels + their IDs
            statement = connection.prepareStatement(getTable(
                    WikiCollections.LABELS).getInsertQuery());
            links = article.getWikiLinks();
            for (int i = 0; i < links.size(); i++) {
                statement.setString(1, links.get(i).getLabelName());
                statement.executeUpdate();
                updateUpdatesCounter();
            }
        } catch (Exception ex) {
            System.out.println("Exception for " + article.getTitle() + " ["
                    + article.getId() + "] : " + ex.getMessage());
        }
    }

    @Override
    public Long getCountTable(WikiCollections table) {
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(getTable(table)
                    .getCountQuery());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return 0l;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0l;
        }
    }

    @Override
    public Boolean openDatabase(String dbName) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            System.out.println("Could not connect to database with "
                    + mySQLConnectionString + " : " + ex.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void closeDatabase() {
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public WikiLabel getWikiLabelForGivenNGram(String ngram, WikiCollections labelsType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Wikipedia loadWikipedia() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveWikipedia(Wikipedia wikipedia) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WikiArticle getArticleByID(Integer targetArticle) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WikiArticle findRealArticleByName(String labelName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void aggregateLabels() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLinksFromTo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveWikiArticle(WikiArticle article, Boolean update) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getDatabaseNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public String getLastError()
    {
        return lastError;
    }

    @Override
    public boolean connectToDatabase(Integer databasePort) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean removeDatabase(String dbName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
