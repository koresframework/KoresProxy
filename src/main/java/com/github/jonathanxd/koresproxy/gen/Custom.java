/*
 *      KoresProxy - Proxy Pattern written on top of Kores! <https://github.com/JonathanxD/KoresProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2019 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.koresproxy.gen;

import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.koresproxy.internals.Util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Specify customization to be applied to proxy class.
 */
public interface Custom {

    /**
     * Returns list with additional properties.
     *
     * @return Additional properties.
     */
    default List<Property> getAdditionalProperties() {
        return Collections.emptyList();
    }

    /**
     * Returns list with value for additional properties (only those that have {@code null} {@link
     * Property#initialize}).
     *
     * @return Returns list with value for additional properties.
     */
    default List<Object> getValueForConstructorProperties() {
        return Collections.emptyList();
    }

    /**
     * Returns whether specification cache of {@code m} should be generated or not. If {@code
     * false}, the cache instance will be assigned to {@code null}.
     *
     * @param m Method to cache.
     * @return Returns whether specification cache of {@code m} should be generated or not.
     */
    default boolean generateSpecCache(Method m) {
        return true;
    }

    /**
     * Returns list of custom generators.
     *
     * @return List of custom generators.
     */
    default List<CustomGen> getCustomGenerators() {
        return Collections.emptyList();
    }

    /**
     * Returns a list of custom handler generators.
     *
     * @return List of custom handler generators.
     */
    default List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections.emptyList();
    }

    /**
     * Property
     */
    class Property {
        /**
         * Specification of property to generate
         */
        private final VariableRef spec;

        /**
         * Initialization of property, {@code null} to initialize in constructor and add as
         * constructor parameter, {@link com.github.jonathanxd.kores.common.Nothing} or normal
         * instruction to initialize with specified value.
         */
        private final Instruction initialize;

        public Property(VariableRef spec, Instruction initialize) {
            this.spec = spec;
            this.initialize = initialize;
        }

        /**
         * Gets field name of {@code property}.
         *
         * @param property Property to get field name.
         * @return Field name of {@code property}.
         */
        public static String getFieldName(Property property) {
            return Util.getAdditionalPropertyFieldName(property.spec);
        }

        /**
         * @see #spec
         */
        public VariableRef getSpec() {
            return this.spec;
        }

        /**
         * @see #initialize
         */
        public Optional<Instruction> getInitialize() {
            return Optional.ofNullable(this.initialize);
        }

        /**
         * Gets field name of {@code this} property.
         *
         * @return Field name of {@code this} property.
         */
        public String getFieldName() {
            return Util.getAdditionalPropertyFieldName(this.spec);
        }

    }

}
