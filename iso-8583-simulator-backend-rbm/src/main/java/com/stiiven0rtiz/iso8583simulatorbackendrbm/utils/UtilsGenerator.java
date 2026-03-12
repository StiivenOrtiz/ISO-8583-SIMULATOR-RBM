package com.stiiven0rtiz.iso8583simulatorbackendrbm.utils;


// imports
import java.util.Random;

/**
 * UtilsGenerator.java
 *
 * This class provides utility methods for generating random values.
 *
 * @version 1.0
 */
public class UtilsGenerator {
    /**
     * Generates a random alphanumeric string of the specified length.
     *
     * @param length The length of the random string to generate.
     * @return A random alphanumeric string.
     */
    public static String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Ensure the first digit is not zero
        sb.append(random.nextInt(9) + 1);

        for (int i = 1; i < length; i++)
            sb.append(random.nextInt(10));

        return sb.toString();
    }
}
