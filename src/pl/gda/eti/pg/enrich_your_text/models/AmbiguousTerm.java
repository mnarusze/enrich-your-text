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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author mnarusze
 */
public class AmbiguousTerm extends DocumentTerm {
    private Meaning chosenMeaning;
    private final List<Meaning> possibleMeanings;

    public AmbiguousTerm(String labelName) {
        super(labelName);
        this.possibleMeanings = new ArrayList<>();
    }

    public Meaning getChosenMeaning() {
        return chosenMeaning;
    }

    public void setChosenMeaning(Meaning chosenMeaning) {
        this.chosenMeaning = chosenMeaning;
        this.possibleMeanings.clear();
    }

    public void addNewMeaning(Meaning newMeaning) {
        if (newMeaning == null) {
            System.err.println("Null meaning detected when adding to term " + this.labelName);
            return;
        }

        if (!possibleMeanings.contains(newMeaning)) {
            possibleMeanings.add(newMeaning);
        }
    }

    public List<Meaning> getPossibleMeanings() {
        return possibleMeanings;
    }

    @Override
    public boolean equals(Object obj) {
        return this.labelName.equalsIgnoreCase(((AmbiguousTerm)(obj)).labelName);
    }
    
    public static class RelatednessComparator implements Comparator<AmbiguousTerm> {
        @Override
        public int compare(AmbiguousTerm o1, AmbiguousTerm o2) {
            return Double.compare(o2.getChosenMeaning().getRelatedness(), o1.getChosenMeaning().getRelatedness());
        }
    }
}
