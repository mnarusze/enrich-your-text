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

import java.util.HashSet;
import java.util.Set;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;

/**
 *
 * @author mnarusze
 */

public class ContextTerm extends DocumentTerm implements Comparable<ContextTerm> {
    private double avgRelWithOtherCtxTerms;
    private double score;
    private String targetArticleTitle;
    private Integer targetArticleID;
    private Set<Integer> linksTo;

    public ContextTerm(String labelName) {
        super(labelName);
        this.avgRelWithOtherCtxTerms = 0;
        this.score = 0;
    }

    public void setTargetArticle(WikiArticle targetArticle) {
        this.targetArticleID = targetArticle.getId();
        this.targetArticleTitle = targetArticle.getTitle();
        this.linksTo = new HashSet<>(targetArticle.getLinksToInt().keySet());
    }
    
    public void increaseAvgRel(double relatedness) {
        this.avgRelWithOtherCtxTerms += relatedness;
    }
    
    // Determines the value of this particular context term. We should take many
    // factors into account:
    // -> average relatedness with other context terms
    // -> the number of times it was used
    // -> if it was mentioned in the beginning or in the end, then it should be
    //    considered slightly more important
    public void calcScore(int ctxTermsCnt, double textBeginOrEndBonus, double textOccurenceBonus, double textMaxOccurenceBonus, double textBeginPercent, double textEndPercent) {
        this.avgRelWithOtherCtxTerms /= ctxTermsCnt;
        this.score = this.avgRelWithOtherCtxTerms;
        double textOccurenceScore = this.ngram.getOccurenceCount() * textOccurenceBonus;
        textOccurenceScore = Math.min(textMaxOccurenceBonus, textOccurenceScore);
        this.score += textOccurenceScore;
        if (ngram.wasInBeginningOrEnd(textBeginPercent, textEndPercent)) {
            this.score += textBeginOrEndBonus;
        }
    }

    public double getAvgRelWithOtherCtxTerms() {
        return avgRelWithOtherCtxTerms;
    }
    
    public double getScore() {
        return score;
    }
    
    // We want to sort descending so terms are reversed
    @Override
    public int compareTo(ContextTerm o) {
        return Double.compare(o.score, this.score);
    }

    @Override
    public boolean equals(Object obj) {
        return ((ContextTerm)obj).labelName.equals(this.labelName);
    } 

    @Override
    public String toString() {
        return "ContextTerm{" + "targetArticle=" + targetArticleTitle + ",label=" + labelName + '}';
    }

    public String getTargetArticleTitle() {
        return targetArticleTitle;
    }

    public Integer getTargetArticleID() {
        return targetArticleID;
    }

    public Set<Integer> getArticleLinksTo() {
        return linksTo;
    }
    
    public void setNGram(NGram ngram) {
        this.ngram = ngram;
    }
}
