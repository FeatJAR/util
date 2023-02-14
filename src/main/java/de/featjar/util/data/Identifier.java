/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.data;

/**
 * Serves as a unique identifier for a provided object ({@link Provider}) in a
 * {@link Cache}. Is only used for its unique reference, therefore has no
 * attributes.
 *
 * @param <T> the type of the provided object
 */
public class Identifier<T> {

    @Override
    public String toString() {
        return "Identifier [" + System.identityHashCode(this) + "]";
    }
}