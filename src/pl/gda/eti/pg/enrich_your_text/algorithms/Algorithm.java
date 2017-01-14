/*
 * Copyright (C) 2016 mnarusze
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
package pl.gda.eti.pg.enrich_your_text.algorithms;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mnarusze
 */
public abstract class Algorithm {
    private final String description;
    private final String name;
    protected Map<String, AlgorithmParameter> parameters;
    protected static String ALGORITHM_ID = "Algorithm";
    
    // This should be overridden by abstract children of this class!
    public static String ALGORITHM_TYPE_PRETTY = "Algorithm";

    protected Algorithm(String description, String name) {
        this.description = description;
        this.name = name;
        this.parameters = new HashMap<>();
    }

    public Map<String, AlgorithmParameter> getParameters() {
        return parameters;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
