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

/**
 *
 * @author mnarusze
 * @param <T>
 */
public class AlgorithmParameter<T extends Comparable<T>> implements Comparable<AlgorithmParameter<T>> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;
    private final T min, max;
    final Class<T> type;
    
    public AlgorithmParameter(String name, String description, T defaultValue, T min, T max, Class<T> type) {
        this.name = name;
        this.description = description;
        this.value = this.defaultValue = defaultValue;
        this.type = type;
        this.min = min;
        this.max = max;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
    
    public void setDefaultValue() {
        this.value = defaultValue;
    }
    
    public Class<T> getType() {
        return type;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    @Override
    public int compareTo(AlgorithmParameter<T> t) {
        return getValue().compareTo(t.getValue());
    }
}
