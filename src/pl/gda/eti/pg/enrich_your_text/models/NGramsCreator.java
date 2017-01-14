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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;

/**
 *
 * @author mnarusze
 */
public class NGramsCreator {

    private final int n;
    private final String text;

    private final int[] indexes;
    private int index = -1;
    private int found = 0;

    public NGramsCreator(String text, int n) {
        this.text = text;
        this.n = n;
        indexes = new int[n];
    }

    private boolean seek() {
        if (index >= text.length()) {
            return false;
        }
        push();
        while (++index < text.length()) {
            if (text.charAt(index) == ' ') {
                found++;
                if (found < n) {
                    push();
                } else {
                    return true;
                }
            }
        }
        return true;
    }

    private void push() {
        for (int i = 0; i < n - 1; i++) {
            indexes[i] = indexes[i + 1];
        }
        indexes[n - 1] = index + 1;
    }

    public List<String> list() {
        List<String> ngrams = new ArrayList<>();
        while (seek()) {
            ngrams.add(get());
        }
        return ngrams;
    }

    private String get() {
        return text.substring(indexes[0], index);
    }
    
        
    public static Boolean isUnwantedKeyword(String keyword) {
        // If just a number - unwanted
        try {
            Double.parseDouble(keyword);
            return true;
        } catch (Exception ex) {
            return false;
        }        
    }
}
