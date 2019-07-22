package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static ArrayList<Float> byte2float(byte[] bytes) {
        int resultStrLen = bytes.length;
        if (resultStrLen % 4 != 0) {
            int byteCount = resultStrLen / 4;
            int margin = resultStrLen - 4 * byteCount;
            if (byteCount > 0) {
                bytes = Arrays.copyOfRange(bytes, 0, 4 * byteCount - margin);
            }
        }

        ArrayList<Float> resultArray = new ArrayList<>();
        for (int i = 0; i < bytes.length; i += 4) {
            byte[] newBytesFour = Arrays.copyOfRange(bytes, i, i + 4);
            resultArray.add(ByteBuffer.wrap(newBytesFour).order(ByteOrder.LITTLE_ENDIAN).getFloat());
        }
        return resultArray;
    }

    public static long byte2Long(byte[] bytes) {
        if (bytes == null) {
            return -1L;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            builder.append(bytes[i] - 48);
        }
        return Long.parseLong(builder.toString());
    }

    public static String[] textListToStringArray(List<Character> texts) {
        String[] ret = new String[texts.size()];
        for (int i = 0; i < texts.size(); i++) {
            ret[i] = texts.get(i).toString();
        }
        return ret;
    }

    public static String textListToString(List<Character> texts) {
        char[] chars = new char[texts.size()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = texts.get(i);
        }
        return new String(chars);
    }

    public static boolean checkInputListListStr(List<List<Character>> textListList) {
        if (textListList.isEmpty()) {
            System.err.println("'texts' must be a non-empty list, but received "
                    + textListList.toArray() + " with " + textListList.size() + " elements");
            return false;
        }
        for (List<Character> characters : textListList) {
            if (!checkInputListStr(characters)) return false;
        }

        return true;
    }

    public static boolean checkInputListStr(List<Character> texts) {
        if (texts.isEmpty()) {
            System.err.println("'texts' must be a non-empty list, but received "
                    + texts.toArray() + " with " + texts.size() + " elements");
            return false;
        }
        for (int i = 0; i < texts.size(); i++) {
            Character item = texts.get(i);
            if (item == ' ') {
                System.err.println("all elements in the list must be non-empty string, but element "
                        + i + " is " + item);
                return false;
            }
        }
        return true;
    }
}
