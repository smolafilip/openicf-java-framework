/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2015 ForgeRock AS.
 */
package org.identityconnectors.common.security;

import java.security.MessageDigest;
import java.util.Arrays;

import org.identityconnectors.common.Base64;

public final class SecurityUtil {

    final protected static char[] LOWER_HEX_ARRAY = "0123456789abcdef".toCharArray();
    final protected static char[] UPPER_HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    private SecurityUtil() {
    }

    /**
     * Converts chars to bytes without using any external functions that might
     * allocate additional buffers for the potentially sensitive data.
     *
     * This guarantees the caller that they only need to cleanup the input and
     * result.
     *
     * @param chars
     *            The chars
     * @return The bytes
     */
    public static byte[] charsToBytes(char[] chars) {
        byte[] bytes = new byte[chars.length * 2];

        for (int i = 0; i < chars.length; i++) {
            char v = chars[i];
            bytes[i * 2] = (byte) (0xff & (v >> 8));
            bytes[i * 2 + 1] = (byte) (0xff & (v));
        }
        return bytes;
    }

    /**
     * Converts bytes to chars without using any external functions that might
     * allocate additional buffers for the potentially sensitive data.
     *
     * This guarantees the caller that they only need to cleanup the input and
     * result.
     *
     * @param bytes
     *            The bytes (to convert into characters).
     * @return The characters (converted from the specified bytes).
     */
    public static char[] bytesToChars(byte[] bytes) {
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < chars.length; i++) {
            char v = (char) (((0xFF & (bytes[i * 2])) << 8) | (0xFF & bytes[i * 2 + 1]));
            chars[i] = v;
        }
        return chars;
    }

    /**
     * Clears an array of potentially sensitive bytes
     *
     * @param bytes
     *            The bytes. May be null.
     */
    public static void clear(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Clears an array of potentially sensitive chars
     *
     * @param chars
     *            The characters. May be null.
     */
    public static void clear(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, (char) 0);
        }
    }

    /**
     * Computes the base 64 encoded SHA1 hash of the input.
     *
     * @param input
     *            The input chars
     * @return the hash
     */
    public static String computeBase64SHA1Hash(char[] input) {
        // convert the char [] to bytes. I know there
        // are utility methods for doing this, but I don't
        // know what sort of buffering they use. because it
        // is possibly sensitive data, we do this in line so
        // that we can clear out our bytes after we are done.
        byte[] bytes = null;
        try {
            bytes = SecurityUtil.charsToBytes(input);
            return SecurityUtil.computeBase64SHA1Hash(bytes);
        } finally {
            // clear the possibly sensitive bytes out
            SecurityUtil.clear(bytes);
            // no need to clear "data" since it is now just a hash
        }
    }

    /**
     * Computes the base 64 encoded SHA1 hash of the input.
     *
     * @param bytes
     *            The input bytes.
     * @return the hash (computed from the input bytes).
     */
    public static String computeBase64SHA1Hash(byte[] bytes) {
        byte[] data;
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA");
            data = hasher.digest(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Base64.encode(data);
    }

    /**
     * Computes the Hex encoded SHA1 hash of the input.
     *
     * @param bytes
     *            The input bytes.
     * @param toLowerCase
     *            {@code true} converts to lowercase or {@code false} to
     *            uppercase
     * @return the hash (computed from the input bytes).
     * @since 1.5
     */
    public static String computeHexSHA1Hash(byte[] bytes, final boolean toLowerCase) {
        byte[] data;
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA");
            data = hasher.digest(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bytesToHex(data, toLowerCase);
    }

    /**
     * Computes the Hex encoded input.
     * 
     * @param bytes
     *            The input bytes to convert to Hex characters
     * @param toLowerCase
     *            {@code true} converts to lowercase or {@code false} to
     *            uppercase
     * @return A String containing hexadecimal characters
     * @since 1.5
     */
    public static String bytesToHex(final byte[] bytes, final boolean toLowerCase) {
        char[] hexChars = new char[bytes.length * 2];
        char[] hexArray = toLowerCase ? LOWER_HEX_ARRAY : UPPER_HEX_ARRAY;
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Verifies the base 64-encoded SHA1 hash of the input.
     *
     * @param input
     *            The input chars
     * @param hash
     *            The expected hash
     * @return true if the hash of the input characters matches the expected
     *         hash.
     */
    public static boolean verifyBase64SHA1Hash(char[] input, String hash) {
        String inputHash = computeBase64SHA1Hash(input);
        return inputHash.equals(hash);
    }

    /**
     * Decrypts the value of a {@link GuardedString}.
     *
     * @param guardedString
     *            the guarded string value.
     * @return the clear string value.
     * @since 1.4
     */
    public static String decrypt(GuardedString guardedString) {
        final String[] clearText = new String[1];

        GuardedString.Accessor accessor = new GuardedString.Accessor() {

            public void access(char[] clearChars) {
                clearText[0] = new String(clearChars);
            }
        };

        guardedString.access(accessor);
        return clearText[0];
    }

    /**
     * Decrypts the value of a {@link GuardedByteArray}.
     *
     * @param guardedByteArray
     *            the guarded byte array value.
     * @return the clear byte array value.
     * @since 1.4
     */
    public static byte[] decrypt(GuardedByteArray guardedByteArray) {
        final byte[][] clearByte = new byte[1][];

        GuardedByteArray.Accessor accessor = new GuardedByteArray.Accessor() {
            public void access(byte[] clearBytes) {
                clearByte[0] = clearBytes;
            }
        };

        guardedByteArray.access(accessor);
        return clearByte[0];
    }

}
