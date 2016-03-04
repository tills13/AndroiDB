package ca.sbstn.dbtest.sql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tills13 on 2015-11-16.
 */

public class SQLUtils {
    public static String format(String string, Object... args) {
        return (new SQLFormatter()).format(string, args);
    }

    public static String convertArgument(Object arg) {
        if (arg == null) return "NULL";

        if (arg instanceof String) {
            String mArg = arg.toString();
            String[] replace = new String[]{"\\\\", "\0", "\n", "\r", "'", "\""};
            String[] replacements = new String[]{"\\\\\\\\", "\\0", "\\n", "\\r", "''", "\\\""};

            for (int i = 0; i < replace.length; i++) {
                mArg = mArg.replaceAll(replace[i], replacements[i]);
            }

            return String.format("'%s'", mArg);
        } else if (arg instanceof Boolean) {
            return (boolean) arg ? "TRUE" : "FALSE";
        } else if (arg instanceof Integer) {
            return String.format("%d", (int) arg);
        }

        return arg.toString();
    }

    static class SQLFormatter {
        public String format(String format, Object... args) {
            for (int i = 1; i < (args.length + 1); i++) {
                String mPattern = String.format("\\$%d", i);
                Pattern pattern = Pattern.compile(mPattern);

                Matcher m = pattern.matcher(format);

                format = m.replaceAll(SQLUtils.convertArgument(args[i - 1]));
            }

            return format;
        }
    }
}
