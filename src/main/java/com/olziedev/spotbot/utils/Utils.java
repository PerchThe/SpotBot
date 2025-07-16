package com.olziedev.spotbot.utils;

import java.text.DecimalFormat;
import java.util.function.Function;

public class Utils {

    public static String clean(String s) {
        if (s == null) return null;

        return s.replaceAll("([_`~*])", "\\\\$1");
    }

    public static String formatNumber(double number) {
        return new DecimalFormat(number % 1 == 0 ? "#,###.##" : "#,##0.00").format(number);
    }

    public static String formatNumber(String number) {
        double parse = Double.parseDouble(number);
        return new DecimalFormat(parse % 1 == 0 ? "#,###.##" : "#,##0.00").format(parse);
    }

    public static String formatTime(long seconds) {
        StringBuilder sb = new StringBuilder();
        seconds = addUnit(sb, seconds, 604800, w -> w + " week" + (w == 1 ? "" : "s") + ", ");
        seconds = addUnit(sb, seconds, 86400, d -> d + " day" + (d == 1 ? "" : "s") + ", ");
        seconds = addUnit(sb, seconds, 3600, h -> h + " hour" + (h == 1 ? "" : "s") + ", ");
        seconds = addUnit(sb, seconds, 60, m -> m + " minute" + (m == 1 ? "" : "s") + ", ");
        addUnit(sb, seconds, 1, s -> s + " second" + (s == 1 ? "" : "s") + ", ");

        String timeString = sb.toString().replaceFirst("(?s)(.*), ", "$1");
        timeString = timeString.replaceFirst("(?s)(.*),", "$1 and");
        return timeString.isEmpty() ? "0 seconds" : timeString;
    }

    private static long addUnit(StringBuilder sb, long sec, long unit, Function<Long, String> s) {
        long n;
        if ((n = sec / unit) > 0) {
            sb.append(s.apply(n));
            sec %= (n * unit);
        }
        return sec;
    }

    public static int parseShortTime(String timestr) {
        return parseShortTime(timestr, true);
    }

    public static int parseShortTime(String timestr, boolean handleSeconds) {
        timestr = timestr.replace("r", "");
        if (!timestr.matches("\\d{1,8}[smhdwMy]")) {
            if (!handleSeconds) return -1;

            try {
                return Integer.parseInt(timestr);
            } catch (Exception ignored) {}
            return -1;
        }
        int multiplier = 1;
        switch (timestr.charAt(timestr.length() - 1)) {
            case 'y':
                multiplier *= 12;
            case 'M':
                multiplier *= 4;
            case 'w':
                multiplier *= 7;
            case 'd':
                multiplier *= 24;
            case 'h':
                multiplier *= 60;
            case 'm':
                multiplier *= 60;
            case 's':
                timestr = timestr.substring(0, timestr.length() - 1);
            default:
        }
        if (Integer.parseInt(timestr) < 0) return -1;

        return (multiplier * Integer.parseInt(timestr));
    }
}
