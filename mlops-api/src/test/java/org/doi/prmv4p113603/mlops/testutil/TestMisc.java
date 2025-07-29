package org.doi.prmv4p113603.mlops.testutil;

import java.lang.reflect.Field;

/**
 * Miscellaneous static methods for testing.
 */
public class TestMisc {

    // NOTE: simple way to print attributes using reflection
    public static void printEntityAttributes(Object obj) {

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {

            field.setAccessible(true);

            try {
                System.out.println(field.getName() + ": " + field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

}
