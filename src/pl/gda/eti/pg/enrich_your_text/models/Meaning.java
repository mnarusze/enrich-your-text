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
package pl.gda.eti.pg.enrich_your_text.models;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mnarusze
 */
public class Meaning {
    private Double relatedness;
    private Double commonness;
    
    private final Integer articleID;
    private final String articleTitle;
    private final Set<Integer> linksTo;

    public Meaning(WikiArticle targetArticle, Double commonness) {
        this.articleID = targetArticle.getId();
        this.articleTitle = targetArticle.getTitle();
        this.linksTo = new HashSet<>(targetArticle.getLinksToInt().keySet());
        
        this.relatedness = 0.0;
        this.commonness = commonness;
    }

    public Double getRelatedness() {
        return relatedness;
    }

    public Double getCommonness() {
        return commonness;
    }

    public void setTopicProximity(Double relatedness) {
        this.relatedness = relatedness;
    }

    public void setCommonness(Double commonness) {
        this.commonness = commonness;
    }

    // Sorts meanings by relatedness in decreasing order
    public static class RelatednessComparator implements Comparator<Meaning> {
        @Override
        public int compare(Meaning o1, Meaning o2) {
            return Double.compare(o2.getRelatedness(), o1.getRelatedness());
        }
    }
    
    // Sorts meanings by commonness in decreasing order
    public static class CommonnessComparator implements Comparator<Meaning> {
        @Override
        public int compare(Meaning o1, Meaning o2) {
            return Double.compare(o2.getCommonness(), o1.getCommonness());
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.articleID.equals(((Meaning)obj).articleID);
    }

    public Integer getArticleID() {
        return articleID;
    }

    public Set<Integer> getArticleLinksTo() {
        return linksTo;
    }

    public String getArticleTitle() {
        return articleTitle;
    }
}
