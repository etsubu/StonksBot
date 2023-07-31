package com.etsubu.stonksbot.utility;

public class TypeUtils {

    /**
     * Creates a new array of same size and copies the elements to that array
     *
     * @param array Array to clone
     * @return Deep cloned array
     */
    public static byte[] cloneByteArray(byte[] array) {
        byte[] clone = new byte[array.length];
        System.arraycopy(array, 0, clone, 0, clone.length);
        return clone;
    }
}
