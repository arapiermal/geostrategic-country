package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GUtils {
    public static String joinStrings(String[] s, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < s.length; i++) {
            sb.append(s[i]).append(" ");
        }
        return sb.toString();
    }

    public static int parseI(String s) {
        try {
            int i = Integer.parseInt(s);
            return i;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int parseI(String[] k, int a) {
        try {
            String s = k[a];
            int i = Integer.parseInt(s);
            return i;
        } catch (Exception e) {
            return 0;
        }
    }

    public static long parseL(String s) {
        try {
            long l = Long.parseLong(s);
            return l;
        } catch (Exception e) {
            return 0L;
        }
    }

    public static double parseD(String s) {
        try {
            double d = Double.parseDouble(s);
            return d;
        } catch (Exception e) {
            return 0d;
        }
    }

    public static double parseDoubleAndPercent(String s) {
        try {
            s = s.replaceAll("\\s+", "");
            int div = 1;
            if (s.endsWith("%")) {
                div = 100;
                s = s.substring(0, s.length() - 1);
            }
            double d = Double.parseDouble(s) / div;
            return d;
        } catch (Exception e) {
            return 0d;
        }
    }


    public static String getVal(String s) {
        int startIndex = s.indexOf(":") + 1;
        return s.substring(startIndex).trim();
    }

    public static String[] getValues(String s) {
        return s.substring(s.indexOf(":") + 1).trim().split("\\s*,\\s*");
    }

    public static String[] getValues2(String s) {
        return s.trim().split("\\s*,\\s*");
    }

    public static String[] getValues(BufferedReader br) {
        try {
            String line = br.readLine().trim();
            String[] firstRow = getValues(line);
            if (!line.endsWith(";")) {
                return firstRow;
            }
            List<String[]> list = new ArrayList<>();
            list.add(firstRow);
            while ((line = br.readLine()) != null && (line = line.trim()).endsWith(";")) {
                String[] row = getValues2(line);
                list.add(row);
                break;
            }
            return listWithStringArrayToStringArray(list);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[][] getValuesMatrix(BufferedReader br) {
        try {
            String line = br.readLine().trim();
            String[] firstRow = getValues(line);
            if (!line.endsWith(";")) {
                return new String[][]{firstRow};
            }
            List<String[]> list = new ArrayList<>();
            list.add(firstRow);
            while ((line = br.readLine().trim()) != null && line.endsWith(";")) {
                String[] row = getValues2(line);
                list.add(row);
                break;
            }
            return (String[][]) list.toArray(); // hmmm
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] listWithStringArrayToStringArray(List<String[]> list) {
        int size = 0;
        for (int i = 0; i < list.size(); i++) {
            size += list.get(i).length;
        }
        String[] result = new String[size];
        int i = 0;
        for (String[] temp : list) {
            for (int j = 0; j < temp.length; j++) {
                result[i++] = temp[j];
            }
        }
        return result;
    }

    public static boolean[] parseBooleanArrFromByte(byte value) {
        boolean[] result = new boolean[8];
        for (byte i = 0; i < 8; i++) {
            result[i] = (value & (1 << i)) != 0;
        }
        return result;
    }

    public static byte parseByteFromBooleanArr(boolean[] value) {
        byte result = 0;
        for (byte i = 0; i < 8; i++) {
            result += (byte) (value[i] ? (1 << i) : 0);
        }
        return result;
    }

    public static boolean[] parseBooleanArrFromShort(short value) {
        boolean[] result = new boolean[16];
        for (short i = 0; i < 16; i++) {
            result[i] = (value & (1 << i)) != 0;
        }
        return result;
    }

    public static short parseShortFromBooleanArr(boolean[] value) {
        short result = 0;
        for (short i = 0; i < 16; i++) {
            result += (short) (value[i] ? (1 << i) : 0);
        }
        return result;
    }

    public static boolean[] parseBooleanArrFromInt(int value) {
        boolean[] result = new boolean[32];
        for (int i = 0; i < 32; i++) {
            result[i] = (value & (1 << i)) != 0;
        }
        return result;
    }

    public static int parseIntFromBooleanArr(boolean[] value) {
        int result = 0;
        for (int i = 0; i < 32; i++) {
            result += value[i] ? (1 << i) : 0;
        }
        return result;
    }

    public static boolean[] parseBooleanArrFromLong(long value) {
        boolean[] result = new boolean[64];
        for (int i = 0; i < 64; i++) {
            result[i] = (value & (1L << i)) != 0;
        }
        return result;
    }

    //
    public static long parseLongFromBooleanArr(boolean[] value) {
        long result = 0;
        for (int i = 0; i < 64; i++) {
            result += value[i] ? (1L << (long) i) : 0;

        }
        return result;
    }

    public static String toAbsolutePathString(String path) {
        return Paths.get(path).toAbsolutePath().toString();
    }

    public static char charGenBig(Random rand) {
        return (char) (rand.nextInt('Z' - 'A') + 'A');
    }

    public static char charGenSmall(Random rand) {

        return (char) (rand.nextInt('z' - 'a') + 'a');
    }


    public static String stringGen(Random rand, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append(charGenBig(rand));
        for (int i = 1; i < size; i++) {
            sb.append(charGenSmall(rand));
        }
        return sb.toString();
    }

    private static final String[] NUMBERSUFFIXES = {"", " thousand", " million", " billion", " trillion"};

    public static String doubleToString(double d) {
        int suffixIndex = 0;
        double formatted = d;
        while (formatted >= 1000 && suffixIndex < NUMBERSUFFIXES.length - 1) {
            formatted /= 1000;
            suffixIndex++;
        }
        String formattedValue;
        if (formatted < 1000) {
            formattedValue = String.format("%.1f", formatted);
        } else {
            formattedValue = String.format("%.0f", formatted);
        }

        return formattedValue + NUMBERSUFFIXES[suffixIndex];
    }

    public static int romanValue(char r) {
        return switch (r) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> -1;
        };
    }

    static final int[] romanValues = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    static final String[] romanLetters = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static String intToRoman(int num) {
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < romanValues.length; i++) {
            while (num >= romanValues[i]) {
                num = num - romanValues[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }

    public static int romanToInt(String s) {
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            int v1 = romanValue(s.charAt(i));
            if (i + 1 < s.length()) {
                int v2 = romanValue(s.charAt(i + 1));
                if (v1 >= v2) {
                    val += v1;
                } else {
                    val += v2 - v1;
                    i++;
                }
            } else {
                val += v1;
            }
        }
        return val;
    }

    public static int[] parseLatLng(String in) {
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int lat = 0;
        int lng = 0;

        return new int[]{lat, lng};
    }

    public static int[] getDate(String in) {
        int day = 0;
        int month = 0;
        int year = 0;
        char c;
        char sep = (char) 0;
        int i = 0;
        while (i < in.length()) {
            c = in.charAt(i);
            if (Character.isDigit(c)) {
                day *= 10;
                day += c - '0';
            } else if (!Character.isLetter(c)) {
                sep = c;
                i++;
                break;
            }
            i++;
        }
        while (i < in.length()) {
            c = in.charAt(i);
            if (Character.isDigit(c)) {
                month *= 10;
                month += c - '0';
            } else if (c == sep) {
                i++;
                break;
            }
            i++;
        }
        while (i < in.length()) {
            c = in.charAt(i);
            if (Character.isDigit(c)) {
                year *= 10;
                year += c - '0';
            } else if (c == sep) {
                i++;
                break;
            }
            i++;
        }
        return new int[]{day, month, year};
    }


    public static String numberToLetters(char c) {
        switch (c) {
            case '0':
                return "zero";
            case '1':
                return "one";
            case '2':
                return "two";
            case '3':
                return "three";
            case '4':
                return "four";
            case '5':
                return "five";
            case '6':
                return "six";
            case '7':
                return "seven";
            case '8':
                return "eight";
            case '9':
                return "nine";
            default:
                return String.valueOf(c);
        }
    }

    public static int[] parseIntArr(String[] str) {
        int[] arr = new int[str.length];
        int i = 0;
        for (String s : str) {
            try {
                arr[i] = Integer.parseInt(s);
                i++;
            } catch (NumberFormatException nfe) {

            }
        }
        if (i < arr.length)
            return Arrays.copyOf(arr, i);
        return arr;
    }

    public static short[] parseShortArr(String[] str) {
        short[] arr = new short[str.length];
        int i = 0;
        for (String s : str) {
            try {
                arr[i] = Short.parseShort(s);
                i++;
            } catch (NumberFormatException nfe) {

            }
        }
        if (i < arr.length)
            return Arrays.copyOf(arr, i);
        return arr;
    }

    public static int parseIntOrMinMaxDef(String s, int min, int max, int def) {
        try{
            int res = Integer.parseInt(s);
            if(res > max)
                return max;
            else if(res < min)
                return min;
            else
                return res;
        } catch(NumberFormatException nfe){
            return def;
        }
    }
}
