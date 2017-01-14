package pl.gda.eti.pg.enrich_your_text.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class WikiLabel {

    public static final String DB__ID = "_id";
    public static final String DB_LABEL_NAME = "nm";
    public static final String DB_STEMMED_NAME = "snm";
    public static final String DB_SOURCE_ARTICLE = "srcArt";
    public static final String DB_TARGET_ARTICLE = "tgtArt";
    public static final String DB_TARGET_ARTICLES = "tgtArts";
    public static final String DB_ARTICLE_CONN_COUNT = "cnt";
    public static final String DB_USAGE_COUNT = "cnt";

    private final String name;
    private Map<Integer, Integer> targetArticles;
    private Integer usageCount;

    public WikiLabel(String name) {
        this.name = name;
        this.usageCount = 0;
        this.targetArticles = null;
    }

    public WikiLabel(Document object) {        
        this.name = (String) object.get(DB__ID);
        this.usageCount = (Integer) object.get(DB_USAGE_COUNT);
        this.targetArticles = new HashMap<>();
        
        if (object.get(DB_TARGET_ARTICLES) != null) {
            List<Document> targetArticlesDB = (ArrayList) object.get(DB_TARGET_ARTICLES);
            if (targetArticlesDB.size() > 0) {
                for (Document targetArticle : targetArticlesDB) {
                    this.targetArticles.put(targetArticle.getInteger(WikiArticle.DB_ARTICLE_ID), targetArticle.getInteger(WikiLabel.DB_ARTICLE_CONN_COUNT));
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Integer> getTargetArticles() {
        return targetArticles;
    }

    public void incLabelUsage() {
        this.usageCount++;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((WikiLabel)(obj)).name);
    }
}
