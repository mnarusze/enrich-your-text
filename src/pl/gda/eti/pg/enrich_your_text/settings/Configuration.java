/*
 * Copyright (C) 2014 mnarusze
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package pl.gda.eti.pg.enrich_your_text.settings;

import com.mongodb.client.MongoDatabase;
import java.util.prefs.Preferences;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase.WikiDatabaseTypes;
import pl.gda.eti.pg.enrich_your_text.database.mongodb.MongoWikiDB;
import pl.gda.eti.pg.enrich_your_text.gui.EnrichYourText;

/**
 *
 * @author mnarusze
 */

public abstract class Configuration {

    // Database access 
    public static WikiDatabaseTypes currentDatabaseType = WikiDatabaseTypes.MongoDB;
            
    // Paths
    public static final String RESOURCES_MAIN_DIR = "pl/gda/eti/pg/enrich_your_text/resources/";
    public static final String TEST_SETS_RESOURCES_DIR = RESOURCES_MAIN_DIR + "/tests";
    public static final String STOP_WORDS_FILE = RESOURCES_MAIN_DIR + "stopwords.txt";
    
    // Preference names
    private static final String PREFERENCE_WIKI_DUMP_PATH = "WikiDumpPath";
    private static final String PREFERENCE_DATABASE_NAME = "DatabaseName";
    private static final String PREFERENCE_DATABASE_USERNAME = "DatabaseUsername";
    private static final String PREFERENCE_DATABASE_PASSWORD = "DatabasePassword";
    private static final String PREFERENCE_DATABASE_PORT = "DatabasePort";
    
    // Preferences
    public static String getWikiDumpPath() {
        return Preferences.userNodeForPackage(EnrichYourText.class).get(Configuration.PREFERENCE_WIKI_DUMP_PATH, "");
    }
    
    public static String getDatabaseName() {
        return Preferences.userNodeForPackage(EnrichYourText.class).get(Configuration.PREFERENCE_DATABASE_NAME, "");
    }
    
    public static String getDatabaseUsername() {
        return Preferences.userNodeForPackage(EnrichYourText.class).get(Configuration.PREFERENCE_DATABASE_USERNAME, "");
    }
    
    public static String getDatabasePassword() {
        return Preferences.userNodeForPackage(EnrichYourText.class).get(Configuration.PREFERENCE_DATABASE_PASSWORD, "");
    }
    
    public static Integer getDatabasePort() {
        return Preferences.userNodeForPackage(EnrichYourText.class).getInt(Configuration.PREFERENCE_DATABASE_PORT, MongoWikiDB.DEFAULT_PORT);
    }
    
    public static void setWikiDumpPath(String wikiDumpPath) {
        Preferences.userNodeForPackage(EnrichYourText.class).put(Configuration.PREFERENCE_WIKI_DUMP_PATH, wikiDumpPath);
    }
    
    public static void setDatabaseName(String dbName) {
        Preferences.userNodeForPackage(EnrichYourText.class).put(Configuration.PREFERENCE_DATABASE_NAME, dbName);
    }
    
    public static void setDatabaseUsername(String dbUsername) {
        Preferences.userNodeForPackage(EnrichYourText.class).put(Configuration.PREFERENCE_DATABASE_USERNAME, dbUsername);
    }
    
    public static void setDatabasePassword(String dbPassword) {
        Preferences.userNodeForPackage(EnrichYourText.class).put(Configuration.PREFERENCE_DATABASE_PASSWORD, dbPassword);
    }
    
    public static void setDatabasePort(Integer dbPort) {
        Preferences.userNodeForPackage(EnrichYourText.class).putInt(Configuration.PREFERENCE_DATABASE_PORT, dbPort);
    }
}
