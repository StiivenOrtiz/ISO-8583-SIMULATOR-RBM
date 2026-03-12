package com.stiiven0rtiz.iso8583simulatorbackendrbm.utils;


// imports

import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * IsoUtils.java
 * <p>
 * This class provides utility methods for handling ISO 8583 data types,
 * including conversion between BCD and decimal formats, byte length calculations,
 * and hexadecimal string representations.
 *
 * @version 1.1
 */
public class IsoUtils {

    public static String getCompleteID(int fieldId) {
        // 1 - 64 = P
        // 65 - 128 = S
        if (fieldId < 1 || fieldId > 128)
            throw new IllegalArgumentException("Field ID must be between 1 and 128");

        return (fieldId <= 64 ? "P" : "S") + fieldId;
    }

    /**
     * Converts a given type and length to the corresponding byte length.
     *
     * @param type   The type of data (e.g., "b-bit", "n", "z", "b", "an", "ans").
     * @param length The length of the data.
     * @return The byte length corresponding to the given type and length and a boolean indicating if the length was rounded.
     * @throws IllegalArgumentException If the type is not supported.
     */
    public static Object[] getByteLength(String type, int length) {
        int byteLength;
        boolean rounded;

        switch (type.toLowerCase()) {
            case "b-bit" -> {
                byteLength = (int) (length / 8.0);
                rounded = false;
            }
            case "n", "z", "b" -> {
                double value = length / 2.0;
                byteLength = (int) Math.ceil(value);
                rounded = value != byteLength;
            }
            case "a", "an", "ans" -> {
                byteLength = length;
                rounded = false;
            }
            default -> throw new IllegalArgumentException("Type not supported: " + type);
        }

        return new Object[]{byteLength, rounded};
    }

    /**
     * Converts a ByteBuf to a hexadecimal string representation.
     *
     * @param buffer The ByteBuf to convert.
     * @return A string containing the hexadecimal representation of the ByteBuf.
     */
    public static String toHex(ByteBuf buffer, int index, int length) {
        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < length; i++)
            hexBuilder.append(String.format("%02X ", buffer.getByte(index + i)));
        return hexBuilder.toString().trim();
    }

    /**
     * Converts a byte array in BCD format to a string representation.
     *
     * @param bcdBytes The byte array in BCD format.
     * @return A string representation of the BCD bytes.
     * @throws IllegalArgumentException If the byte array is null, empty, or contains invalid BCD values.
     */
    public static String bcdToString(byte[] bcdBytes) throws IllegalArgumentException {
        if (bcdBytes == null || bcdBytes.length == 0)
            throw new IllegalArgumentException("Bytes array cannot be null or empty.");

        StringBuilder sb = new StringBuilder();

        // Iterate through each byte in the BCD array
        for (byte b : bcdBytes) {
            int highNibble = (b >> 4) & 0x0F; // Extract the high nibble (4 most significant bits)
            int lowNibble = b & 0x0F; // Extract the low nibble (4 least significant bits)

            // Validate BCD nibbles
            if (highNibble > 9 || lowNibble > 9)
                throw new IllegalArgumentException(String.format("Byte 0x%02X isn't BCD valid.", b));

            sb.append(highNibble);
            sb.append(lowNibble);
        }

        return sb.toString();
    }

    /**
     * Converts a byte array in BCD format to a string representation, considering the real length.
     *
     * @param bcdBytes   The byte array in BCD format.
     * @param realLength The actual number of digits to extract.
     * @return A string representation of the BCD bytes.
     * @throws IllegalArgumentException If the byte array is null, empty, or realLength is invalid.
     */
    public static String bcdToString(byte[] bcdBytes, int realLength) throws IllegalArgumentException {
        if (bcdBytes == null) throw new IllegalArgumentException("bcdBytes is null");
        if (realLength < 0) throw new IllegalArgumentException("realLength < 0");

        // Calculate the number of bytes required to represent the realLength digits
        int requiredBytes = (realLength + 1) / 2;
        if (bcdBytes.length < requiredBytes) { // Not enough data
            throw new IllegalArgumentException(String.format("Not enough data: need %d byte(s) for %d digits, got %d.",
                    requiredBytes, realLength, bcdBytes.length));
        }

        boolean skipFirstHighNibble = false;   // skip first high nibble when odd and last low nibble is numeric
        boolean ignoreLastLowNibble = false;   // ignore final low nibble when it is a letter (A–F)
        int lastIndex = requiredBytes - 1; // Index of the last byte to process

        if ((realLength & 1) == 1) { // odd
            int lastLow = bcdBytes[lastIndex] & 0x0F;
            if (lastLow > 9) {
                // Padding at the end (A–F) -> ignore only the last low nibble
                ignoreLastLowNibble = true;
            } else {
                // Padding at the start -> skip the first high nibble
                skipFirstHighNibble = true;
            }
        }

        StringBuilder sb = new StringBuilder(realLength); // Pre-size for performance
        int count = 0;

        // Process each byte up to the required number of bytes
        for (int i = 0; i < requiredBytes && count < realLength; i++) {
            int high = (bcdBytes[i] >>> 4) & 0x0F;
            int low = bcdBytes[i] & 0x0F;

            if (i == 0 && skipFirstHighNibble) { // skip first high nibble only
                if (low > 9) {
                    throw new IllegalArgumentException(String.format("Invalid BCD at byte %d low nibble: 0x%X", i, low));
                }
                sb.append((char) ('0' + low));
                count++;
            } else { // normal processing
                if (high > 9) { // validate high nibble
                    throw new IllegalArgumentException(String.format("Invalid BCD at byte %d high nibble: 0x%X", i, high));
                }
                sb.append((char) ('0' + high));
                count++;

                if (count >= realLength) break; // Stop if we've reached the desired length

                if (i == lastIndex && ignoreLastLowNibble) {
                    continue; // skip padded last low nibble (A–F)
                }
                if (low > 9) {
                    throw new IllegalArgumentException(String.format(
                            "Invalid BCD at byte %d low nibble: 0x%X", i, low));
                }
                sb.append((char) ('0' + low));
                count++;
            }
        }

        return sb.toString();
    }


    /**
     * Converts a byte array in BCD format to an ASCII string representation.
     * Useful for P34
     *
     * @param bcdBytes The byte array in BCD format.
     * @return A string representation of the BCD bytes in ASCII format.
     * @throws IllegalArgumentException If the byte array is null or empty.
     */
    public static String bcdToAsciiString(byte[] bcdBytes, int realLength) {
        startsValidsBCD(bcdBytes, realLength);

        StringBuilder sb = new StringBuilder(realLength);
        int count = 0;

        for (byte b : bcdBytes) {
            if (count < realLength) {
                sb.append(nibbleToChar((b >> 4) & 0x0F));
                count++;
            }
            if (count < realLength) {
                sb.append(nibbleToChar(b & 0x0F));
                count++;
            }
        }

        return sb.toString();
    }

    /**
     * Converts a nibble (4 bits) to its ASCII character representation.
     *
     * @param nibble The nibble to convert (0-15).
     * @return The ASCII character representation of the nibble.
     */
    private static char nibbleToChar(int nibble) {
        return (char) ((nibble < 10) ? ('0' + nibble) : (':' + (nibble - 10)));
    }

    /**
     * Converts a byte array in BCD format to an ASCII string representation,
     * handling left-padded BCD (with F) when length is odd.
     * Useful for P35
     *
     * @param bcdBytes   The byte array in BCD format.
     * @param realLength The actual number of digits to extract.
     * @return A string representation of the BCD bytes in ASCII format.
     * @throws IllegalArgumentException If the byte array is null, empty, or realLength is invalid.
     */
    public static String bcdToAsciiStringPadLeft(byte[] bcdBytes, int realLength) {
        startsValidsBCD(bcdBytes, realLength);

        StringBuilder sb = new StringBuilder(realLength);
        int count = 0;
        boolean skipFirstHighNibble = (realLength % 2 != 0);

        for (int i = 0; i < bcdBytes.length && count < realLength; i++) {
            byte b = bcdBytes[i];
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;

            if (skipFirstHighNibble) {
                // Skip the first high nibble only
                sb.append(nibbleToChar(low));
                skipFirstHighNibble = false;
                count++;
            } else {
                sb.append(nibbleToChar(high));
                count++;
                if (count < realLength) {
                    sb.append(nibbleToChar(low));
                    count++;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Validates the BCD byte array and real length.
     *
     * @param bcdBytes   The byte array in BCD format.
     * @param realLength The actual number of digits to extract.
     * @throws IllegalArgumentException If the byte array is null, empty, or realLength is invalid.
     */
    public static void startsValidsBCD(byte[] bcdBytes, int realLength) throws IllegalArgumentException {
        if (bcdBytes == null || bcdBytes.length == 0)
            throw new IllegalArgumentException("Bytes array cannot be null or empty.");
        if (realLength <= 0 || realLength > (bcdBytes.length * 2))
            throw new IllegalArgumentException("Invalid real length for BCD conversion.");
    }


    /**
     * Converts a byte array in BCD format to a long value.
     *
     * @param bcdBytes The byte array in BCD format.
     * @return The long value represented by the BCD bytes.
     * @throws IllegalArgumentException If the byte array is null or empty.
     * @throws NumberFormatException    If the BCD bytes contain invalid values or are too large for a long.
     */
    public static long bcdToLong(byte[] bcdBytes) throws IllegalArgumentException {
        if (bcdBytes == null || bcdBytes.length == 0)
            throw new IllegalArgumentException("Bytes array cannot be null or empty.");

        String decimalString = getDecimalString(bcdBytes); // Get the decimal string representation of BCD bytes

        try {
            return Long.parseLong(decimalString);
        } catch (NumberFormatException e) {
            // This exception would be trown if:
            // 1. Any nibble was > 9 (for example, if you received 0x1A, it would convert to "1A", which is not a number).
            // 2. The resulting string was too long to fit in a 'long' (unlikely for 8 BCD bytes,
            //    which only have 16 digits, and a 'long' supports up to 19 digits).
            // The original byte values are included in the exception message for easier debugging.
            throw new NumberFormatException("Invalid BCD value or too large for long: '" + decimalString);
        }
    }

    /**
     * Converts a byte array in BCD format to a string representation of its decimal value.
     *
     * @param bcdBytes The byte array in BCD format.
     * @return A string representation of the decimal value.
     */
    private static String getDecimalString(byte[] bcdBytes) {
        StringBuilder sb = new StringBuilder(bcdBytes.length * 2); // Each BCD byte will contribute 2 decimal digits
        for (byte b : bcdBytes) {
            // Extact the high nibble (4 most significant bits)
            // Shift right by 4 bits and use a mask 0x0F to isolate the 4 bits.
            int nibble1 = (b >> 4) & 0x0F;
            sb.append(nibble1); // Add the digit to the String

            // Extract the low nibble (4 least significant bits)
            // Shift right by 0 bits (or not at all) and use a mask 0x0F to isolate the 4 bits.
            int nibble2 = b & 0x0F;
            sb.append(nibble2); // Add the digit to the String
        }

        // Return the complete decimal string representation
        return sb.toString();
    }

    /**
     * Converts a byte array in BCD format to an int value.
     *
     * @param bcdBytes The byte array in BCD format.
     * @return The int value represented by the BCD bytes.
     * @throws IllegalArgumentException If the byte array is null or empty.
     * @throws ArithmeticException      If the value exceeds the range of an int.
     */
    public static int bcdToInt(byte[] bcdBytes) {
        // Convert the BCD bytes to a long value
        long value = bcdToLong(bcdBytes);

        // Verify if the value fits in an int range
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
            throw new ArithmeticException("Value " + value + " exceeds int range.");

        return (int) value; // If it fits, cast to int and return
    }

    /**
     * Converts a byte array to a hexadecimal string representation.
     *
     * @param bytes The byte array to convert.
     * @return A string containing the hexadecimal representation of the byte array.
     */
    public static String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02X ", b)); // Convert each byte to a two-digit hex string and append a space
        return sb.toString().trim(); // Remove trailing space
    }

    /**
     * Converts a byte array to a hexadecimal string representation with spaces between bytes.
     *
     * @param fieldValueBytes The byte array to convert.
     * @return A string containing the hexadecimal representation of the byte array, with spaces between bytes.
     */
    public static String toHexBString(byte[] fieldValueBytes) {
        if (fieldValueBytes == null)
            return null;

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < fieldValueBytes.length; i++) {
            hexString.append(String.format("%02X", fieldValueBytes[i]));
            if (i < fieldValueBytes.length - 1)
                hexString.append(" "); // Add a space between bytes
        }
        return hexString.toString();
    }

    /**
     * Processes the field value bytes based on the field type and returns the appropriate string representation.
     *
     * @param fieldType       The type of the field (e.g., "n", "b", "z", "a", "an", "ans").
     * @param fieldId         The ID of the field (e.g., "35" for Track 2 Data).
     * @param fieldValueBytes The byte array representing the field value.
     * @param contentLength   The actual length of the content in the field.
     * @param iso             The IsoFieldsData instance containing field definitions.
     * @return A string representation of the field value based on its type.
     */
    public static String getValueStringByType(String fieldType, String fieldId, byte[] fieldValueBytes, int contentLength, IsoFieldsData iso) {
        String postFieldValue = "";

        switch (fieldType) {
            case "n" -> postFieldValue = bcdToString(fieldValueBytes, contentLength);
            case "b", "b-bit" -> postFieldValue = toHexBString(fieldValueBytes);
            case "z" -> {
                if (fieldId.equals("35"))
                    postFieldValue = bcdToAsciiStringPadLeft(fieldValueBytes, contentLength);
                else
                    postFieldValue = bcdToAsciiString(fieldValueBytes, contentLength);
            }
            case "a", "an", "ans" -> {
                if (iso.getDataElementById(fieldId).name().toLowerCase().contains("reserved") ||
                        iso.getDataElementById(fieldId).name().toLowerCase().contains("private") ||
                        iso.getDataElementById(fieldId).name().toLowerCase().contains("national"))
                    postFieldValue = toHexBString(fieldValueBytes);
                else
                    postFieldValue = byteToASCIIString(fieldValueBytes);
            }
        }

        return postFieldValue;
    }

    /**
     * Converts a byte array to an ASCII string representation.
     * Non-printable characters are represented as '.'.
     *
     * @param fieldValueBytes The byte array to convert.
     * @return A string containing the ASCII representation of the byte array.
     */
    private static String byteToASCIIString(byte[] fieldValueBytes) {
        if (fieldValueBytes == null)
            return null;

        StringBuilder asciiString = new StringBuilder();
        for (byte b : fieldValueBytes)
            // Check if the byte is a printable ASCII character
            if (b >= 32 && b <= 126)
                asciiString.append((char) b); // Convert byte to char and append
            else
                asciiString.append('.'); // Non-printable characters are represented as '.'
        return asciiString.toString();
    }

    /**
     * Converts a TPDU string to a response TPDU string.
     * The response TPDU is constructed by rearranging the bytes of the original TPDU.
     *
     * @param tpdu The original TPDU string (5 bytes separated by spaces).
     * @return A string representing the response TPDU.
     * @throws IllegalArgumentException If the TPDU does not have exactly 5 bytes.
     */
    public static String getResponseTPDUString(String tpdu) {
        String[] parts = tpdu.split(" ", 5);

        if (parts.length != 5)
            throw new IllegalArgumentException("TPDU must have exactly 5 bytes");

        return parts[0] + " " + parts[3] + " " + parts[4] + " " + parts[1] + " " + parts[2];
    }

    /**
     * Converts a numeric string to a BCD string representation.
     * The BCD string has spaces between every two digits.
     *
     * @param input The numeric string to convert.
     * @return A BCD string representation with spaces, or null if the input is invalid.
     */
    public static String stringToHexString(String input) {
        // Only separete by space if the input hasn't spaces and its numberic (inclusive 0
        String BCDString = null;

        if (!input.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                sb.append(input.charAt(i));
                if (i % 2 == 1 && i < input.length() - 1)
                    sb.append(" "); // Add space after every two digits
            }
            BCDString = sb.toString();
        }

        return BCDString;
    }

    /**
     * Converts a binary string (bits) to a hexadecimal string representation.
     * The input string must have a length that is a multiple of 4.
     *
     * @param bits The binary string to convert.
     * @return A hexadecimal string representation of the binary input.
     * @throws IllegalArgumentException If the length of the bit string is not a multiple of 4.
     */
    public static String bitsToHexString(String bits) {
        if (bits.length() % 8 != 0)
            throw new IllegalArgumentException("The length of the bit string must be a multiple of 8");

        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < bits.length(); i += 8) {
            String byteBits = bits.substring(i, i + 8);
            int decimal = Integer.parseInt(byteBits, 2);
            hex.append(String.format("%02X ", decimal));
        }

        // Remove the trailing space
        return hex.toString().trim();
    }

    /**
     * Calculates the bitmap string based on the provided list of data elements.
     * The bitmap is represented as a hexadecimal string with spaces between bytes.
     *
     * @param dataElements A list of strings representing the field IDs that are present.
     * @return A hexadecimal string representation of the bitmap.
     */
    public static String calculatedBitmap(List<String> dataElements) {
        StringBuilder bitmap = new StringBuilder();

        // Create a bitmap of 128 bits (16 bytes) pars separated by spaces

        // given that dataElements is a list of field IDs with "P" or "S" prefix (the prefix has been removed)
        for (int i = 1; i <= 128; i++)
            if (dataElements.contains(String.valueOf(i)))
                bitmap.append("1"); // Field is present
            else
                bitmap.append("0"); // Field is not present

        return bitsToHexString(bitmap.toString());
    }

    /**
     * Converts a string to its hexadecimal representation.
     * Each character in the string is converted to its corresponding hex value.
     *
     * @param input The string to convert.
     * @return A string containing the hexadecimal representation of the input string.
     */
    public static String stringToHex(String input) {
        StringBuilder hex = new StringBuilder();

        for (char c : input.toCharArray())
            hex.append(String.format("%02X ", (int) c));

        // remove the trailing space
        return hex.toString().trim();
    }

    /**
     * Extracts the Bank Identification Number (BIN) from Track 2 data.
     * The BIN is the first 6 digits of the Primary Account Number (PAN).
     *
     * @param track2Data The Track 2 data string.
     * @return The BIN as a string, or null if it cannot be extracted.
     */
    public static String getBinFromTrack2(String track2Data) {
        // Check if the input data is null or empty. If so, return null immediately.
        if (track2Data == null || track2Data.isEmpty()) {
            return null;
        }

        String pan = getPanFromTrack2(track2Data);

        // BIN Extraction: The Bank Identification Number (BIN) is the first 6 digits of the PAN.
        final int BIN_LENGTH = 6;
        if (pan.length() >= BIN_LENGTH) {
            // Return the first 6 characters, which constitute the BIN.
            return pan.substring(0, BIN_LENGTH);
        } else {
            // If the isolated PAN is shorter than 6 digits, it's not a valid BIN, so return null.
            return null;
        }
    }

    /**
     * Extracts the Primary Account Number (PAN) from Track 2 data.
     *
     * @param track2Data The Track 2 data string.
     * @return The PAN as a string.
     */
    private static String getPanFromTrack2(String track2Data) {
        // Initial Cleaning: Remove the possible Start Sentinel (;)
        // and the possible End Sentinel (?) characters often found in raw track data.
        // This ensures these protocol characters do not interfere with PAN extraction.
        String cleanedTrack2 = track2Data.replace(";", "").replace("?", "");

        // PAN Isolation: Search for the separator character ('=').
        // The separator marks the end of the Primary Account Number (PAN)
        // and the beginning of the discretionary data (expiration date, service code, etc.).
        int separatorIndex = cleanedTrack2.indexOf('=');

        // Determine the Primary Account Number (PAN)
        String pan;
        if (separatorIndex != -1) {
            // If the separator is found, the PAN is the substring from the beginning
            // up to the separator index.
            pan = cleanedTrack2.substring(0, separatorIndex);
        } else {
            // If no '=' separator is found, assume the entire remaining string
            // (after initial cleaning) is the PAN. This handles non-standard or truncated inputs.
            pan = cleanedTrack2;
        }
        return pan;
    }

    /**
     * Converts an Iso8583Msg object to a hexadecimal string representation.
     * The resulting string includes the TPDU, MTI, bitmap, and data elements in hex format.
     *
     * @param iso    The IsoFieldsData instance containing field definitions.
     * @param isoMsg The Iso8583Msg object to convert.
     * @return A string containing the hexadecimal representation of the Iso8583Msg.
     */
    public static String iso8584MSGToHEXString(IsoFieldsData iso, Iso8583Msg isoMsg) {
        StringBuilder hexReturned = new StringBuilder();

        // first the TPDU
        hexReturned.append(isoMsg.getTPDU().getValue()).append(" "); // 'cause msg is saved as hex string with spaces
        // then the MTI
        hexReturned.append(isoMsg.getMTI().getValue()).append(" "); // same here
        // then the bitmap
        hexReturned.append(isoMsg.getBitmap().getValue()).append(" "); // same here

        // here, the server will iterate over the fields in the isoMsg, to convert each field value to hex
        // based on its type and length type, and append it to the hexReturned string
        for (String fieldId : isoMsg.getDataElements().keySet()) {
            String fieldType = iso.getDataElementById(fieldId).type();
            String lengthType = iso.getDataElementById(fieldId).lengthType();
            String hexPostFieldValue;
            String fieldValue = (String) isoMsg.getDataElement(fieldId);

            switch (fieldType) {
                case "n" -> hexPostFieldValue = stringToHexString(fieldValue);
                case "z" -> {
                    if (fieldId.equals("35"))
                        hexPostFieldValue = fieldValue; //bcdToAsciiStringPadLeft(fieldValueBytes, contentLength);
                    else
                        hexPostFieldValue = stringToHexString(fieldValue);
                }
                case "a", "an", "ans" -> {
                    if (iso.getDataElementById(fieldId).name().toLowerCase().contains("reserved") ||
                            iso.getDataElementById(fieldId).name().toLowerCase().contains("private") ||
                            iso.getDataElementById(fieldId).name().toLowerCase().contains("national"))
                        hexPostFieldValue = fieldValue;
                    else
                        hexPostFieldValue = stringToHex(fieldValue);
                }
                default -> hexPostFieldValue = fieldValue;
            }

            if (!lengthType.equals("F")) {
                // remove spaces from hexPostFieldValue to calculate length
                String hexNoSpaces = hexPostFieldValue.replace(" ", "");
                boolean flagBCD = fieldType.equals("n") || fieldType.equals("z") || fieldType.equals("b");
                int fieldLength = flagBCD ? hexNoSpaces.length() : hexNoSpaces.length() / 2;

                if (fieldLength % 2 != 0 && flagBCD)
                    if (fieldType.equals("z") && fieldId.equals("35"))
                        hexNoSpaces = "F" + hexNoSpaces;
                    else
                        hexNoSpaces = hexNoSpaces + "F";

                String lengthHex = "";

                if (lengthType.equals("LLVAR"))
                    // determinate if lengthHex needs to be add zero for complete 2 digits
                    if (fieldLength < 10)
                        lengthHex = "0" + fieldLength;
                    else
                        lengthHex = String.valueOf(fieldLength);
                else if (lengthType.equals("LLLVAR"))
                    // determinate if lengthHex needs to be add zero for complete 3 digits
                    if (fieldLength <= 9)
                        lengthHex = "000" + fieldLength; // 000 1
                    else if (fieldLength < 100)
                        lengthHex = "00" + fieldLength; // 00 10
                    else if (fieldLength < 1000)
                        lengthHex = "0" + fieldLength; // 0 100
                    else if (fieldLength < 10000)
                        lengthHex = "" + fieldLength; // 1000

                hexNoSpaces = lengthHex + hexNoSpaces;

                hexPostFieldValue = stringToHexString(hexNoSpaces);
            }

            if (hexPostFieldValue.replace(" ", "").length() % 2 != 0) {
                String hexNoSpaces = hexPostFieldValue.replace(" ", "");
                hexNoSpaces = "0" + hexNoSpaces;
                hexPostFieldValue = stringToHexString(hexNoSpaces);
            }

            hexReturned.append(hexPostFieldValue).append(" "); // Append the field value in hex format
        }

        return hexReturned.toString();
    }

    public static String StringHexToStringASCII(String hexString) {
        StringBuilder asciiString = new StringBuilder();

        // Split the hex string into pairs of characters
        for (int i = 0; i < hexString.length(); i += 2) {
            String hexPair = hexString.substring(i, i + 2);
            // Convert the hex pair to an integer, then to a character
            char asciiChar = (char) Integer.parseInt(hexPair, 16);
            asciiString.append(asciiChar);
        }

        return asciiString.toString();
    }

    public static String getFixedDataElement(String rawData, int targetField) {

//        System.out.println("=== ISO8583 Fixed Field Extraction START ===");
//        System.out.println("Target field: " + targetField);

        if (rawData == null || rawData.isEmpty()) {
//            System.out.println("ERROR: Raw data is null or empty");
            return "";
        }

//        System.out.println("Raw data length: " + rawData.length());

        // TPDU = 5 bytes = 10 hex chars
        int index = 10;
//        System.out.println("After TPDU, index=" + index);

        // MTI = 2 bytes = 4 chars
        index += 4;
//        System.out.println("After MTI, index=" + index);

        // Primary bitmap = 8 bytes = 16 hex chars
        String bitmapHex = rawData.substring(index, index + 16);
//        System.out.println("Primary bitmap (HEX): " + bitmapHex);

        index += 16;
//        System.out.println("After bitmap, index=" + index);

        long bitmap = Long.parseUnsignedLong(bitmapHex, 16);
//        System.out.println("Primary bitmap (BIN): " + Long.toBinaryString(bitmap));

        for (int field = 1; field <= 64; field++) {

            boolean present = (bitmap & (1L << (64 - field))) != 0;
//            System.out.println("Field " + field + " present=" + present);

            if (!present)
                continue;

            int length;

            switch (field) {

                // ---------- LLVAR ----------
                case 2 -> { // PAN
                    int ll = Integer.parseInt(rawData.substring(index, index + 2));
//                    System.out.println("P2 LLVAR length=" + ll);
                    index += 2 + ll;
                    continue;
                }

                case 32 -> {
                    int ll = Integer.parseInt(rawData.substring(index, index + 2));
//                    System.out.println("P32 LLVAR length=" + ll);
                    index += 2 + ll;
                    continue;
                }

                case 33 -> {
                    int ll = Integer.parseInt(rawData.substring(index, index + 2));
//                    System.out.println("P33 LLVAR length=" + ll);
                    index += 2 + ll;
                    continue;
                }

                case 34 -> {
                    int ll = Integer.parseInt(rawData.substring(index, index + 2));
//                    System.out.println("P34 LLVAR length=" + ll);
                    index += 2 + ll;
                    continue;
                }

                case 35 -> { // Track 2
                    int ll = Integer.parseInt(rawData.substring(index, index + 2));
//                    System.out.println("P35 LLVAR length=" + ll);
                    index += 2 + ll;
                    continue;
                }

                // ---------- LLLVAR ----------
                case 36 -> { // Track 3
                    int lll = Integer.parseInt(rawData.substring(index, index + 3));
//                    System.out.println("P36 LLLVAR length=" + lll);
                    index += 3 + lll;
                    continue;
                }

                // ---------- FIXED ----------
                case 3 -> length = 6;
                case 4 -> length = 12;
                case 5 -> length = 12;
                case 6 -> length = 12;
                case 7 -> length = 10;
                case 8 -> length = 8;
                case 9 -> length = 8;
                case 10 -> length = 8;
                case 11 -> length = 6;
                case 12 -> length = 6;
                case 13 -> length = 4;
                case 14 -> length = 4;
                case 15 -> length = 4;
                case 16 -> length = 4;
                case 17 -> length = 4;
                case 18 -> length = 4;
                case 19 -> length = 3;
                case 20 -> length = 3;
                case 21 -> length = 3;
                case 22 -> length = 3;
                case 23 -> length = 3;
                case 24 -> length = 4;
                case 25 -> length = 2;
                case 26 -> length = 2;
                case 27 -> length = 1;
                case 28 -> length = 10;
                case 29 -> length = 10;
                case 30 -> length = 10;
                case 31 -> length = 10;
                case 37 -> length = 24;
                case 38 -> length = 12;
                case 39 -> length = 4;

                // ---------- NOT SUPPORTED ----------
                default -> {
//                    System.out.println("Unsupported field encountered: " + field);
                    return "";
                }
            }

//            System.out.println("Parsing field " + field + " with fixed length=" + length);
//            System.out.println("Current index before field " + field + ": " + index);

            if (index + length > rawData.length()) {
//                System.out.println("ERROR: Index out of bounds while parsing field " + field);
                return "";
            }

            String value = rawData.substring(index, index + length);
//            System.out.println("Field " + field + " value=[" + value + "]");

            if (field == targetField) {
//                System.out.println("Target field " + targetField + " found, returning value");
//                System.out.println("=== ISO8583 Fixed Field Extraction END ===");
                return value;
            }

            index += length;
//            System.out.println("Index after field " + field + ": " + index);
        }

//        System.out.println("WARNING: Target field " + targetField + " not present in bitmap");
//        System.out.println("=== ISO8583 Fixed Field Extraction END ===");
        return "";
    }
}
