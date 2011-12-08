/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder;

import junit.framework.TestCase;
import org.apache.xbean.finder.archive.ClassesArchive;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Basic assertions:
 * <p/>
 * - getDeclaredAnnotations should not include meta-annotations
 * - meta-annotations can be recursive
 * - the most top-level value is the one returned from getAnnotation()
 *
 * @version $Rev$ $Date$
 */
public class RootMetaAnnotatedClassTest extends TestCase {

    public void test() throws Exception {

    }
    public void _test() throws Exception {
        AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Square.class, Circle.class, Triangle.class, Fake.class, Store.class, Farm.class, None.class)).link();

        Map<Class<?>, Annotated<Class<?>>> map = new HashMap<Class<?>, Annotated<Class<?>>>();

        List<Annotated<Class<?>>> metas = finder.findMetaAnnotatedClasses(Color.class);
        for (Annotated<Class<?>> meta : metas) {
            Annotated<Class<?>> oldValue = map.put(meta.get(), meta);
            assertNull("no duplicates allowed", oldValue);
        }

        // MetaAnnotation classes themselves are not included
        assertNull(map.get(Red.class));
        assertNull(map.get(Crimson.class));

        // Check the negative scenario
        assertFalse(map.containsKey(None.class));

        // Check the positive scenarios

        { // Circle
            Annotated<Class<?>> target = map.get(Circle.class);
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("white", target.getAnnotation(Color.class).value());
        }

        { // Square
            Annotated<Class<?>> target = map.get(Square.class);
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("red", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Red.class));
            assertTrue(target.getAnnotation(Red.class) != null);
            assertTrue(contains(Red.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, target.getAnnotations()));
        }

        { // Triangle
            Annotated<Class<?>> target = map.get(Triangle.class);
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("red", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Red.class));
            assertTrue(target.getAnnotation(Red.class) != null);
            assertTrue(!contains(Red.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Red.class, target.getAnnotations()));

            assertTrue(target.isAnnotationPresent(Crimson.class));
            assertTrue(target.getAnnotation(Crimson.class) != null);
            assertTrue(contains(Crimson.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Crimson.class, target.getAnnotations()));
        }

        { // Fake -- should not get more than we asked for
            Annotated<Class<?>> target = map.get(Fake.class);
            assertNull(target);

            List<Annotated<Class<?>>> list = finder.findMetaAnnotatedClasses(NotMeta.class);
            assertEquals(1, list.size());

            target = list.get(0);
            assertNotNull(target);

            assertTrue(!target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) == null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(!contains(Color.class, target.getAnnotations()));
        }


        { // Circular - Egg wins
            Annotated<Class<?>> target = map.get(Store.class);
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("egg", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Egg.class));
            assertTrue(target.getAnnotation(Egg.class) != null);
            assertTrue(contains(Egg.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Egg.class, target.getAnnotations()));

            assertTrue(target.isAnnotationPresent(Chicken.class));
            assertTrue(target.getAnnotation(Chicken.class) != null);
            assertTrue(!contains(Chicken.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Chicken.class, target.getAnnotations()));
        }

        { // Circular - Chicken wins
            Annotated<Class<?>> target = map.get(Farm.class);
            assertNotNull(target);

            assertTrue(target.isAnnotationPresent(Color.class));
            assertTrue(target.getAnnotation(Color.class) != null);
            assertTrue(!contains(Color.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Color.class, target.getAnnotations()));
            assertEquals("chicken", target.getAnnotation(Color.class).value());

            assertTrue(target.isAnnotationPresent(Egg.class));
            assertTrue(target.getAnnotation(Egg.class) != null);
            assertTrue(!contains(Egg.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Egg.class, target.getAnnotations()));

            assertTrue(target.isAnnotationPresent(Chicken.class));
            assertTrue(target.getAnnotation(Chicken.class) != null);
            assertTrue(contains(Chicken.class, target.getDeclaredAnnotations()));
            assertTrue(contains(Chicken.class, target.getAnnotations()));
        }

    }

    private boolean contains(Class<? extends Annotation> type, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (type.isAssignableFrom(annotation.annotationType())) return true;
        }
        return false;
    }


    // 100% your own annotations, even the @Metatype annotation
    // Any annotation called @Metatype and annotated with itself works
    @Metaroot
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ANNOTATION_TYPE)
    public @interface Metaroot {
    }

    @Metaroot
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ANNOTATION_TYPE)
    public @interface Stereotype {
    }

    @Target(value = {TYPE})
    @Retention(value = RUNTIME)
    public static @interface Color {
        String value() default "";
    }

    @Stereotype
    @Color("red")
    // one level deep
    @Target(value = {TYPE})
    @Retention(value = RUNTIME)
    public static @interface Red {
    }

    @Stereotype
    @Red
    // two levels deep
    @Target(value = {TYPE})
    @Retention(value = RUNTIME)
    public static @interface Crimson {
    }

    @Red
    @Target(value = {TYPE})
    @Retention(value = RUNTIME)
    public static @interface NotMeta {
    }

    @Stereotype
    @Color("egg")
    @Chicken
    // Circular
    @Target(value = {TYPE})
    @Retention(value = RUNTIME)
    public static @interface Egg {
    }


    @Stereotype
    @Color("chicken")
    @Egg
    // Circular
    @Target(value = {TYPE})
    @Retention(value = RUNTIME)
    public static @interface Chicken {
    }


    @Red
    // -> @Color
    public static class Square {
    }

    @Red
    // will be covered up by @Color
    @Color("white")
    public static class Circle {
    }

    @Crimson
    // -> @Red -> @Color
    public static class Triangle {

    }

    // always good to have a fake in there

    public static class None {

    }

    @NotMeta
    public static class Fake {

    }

    @Egg
    public static class Store {

    }

    @Chicken
    public static class Farm {
    }
}
