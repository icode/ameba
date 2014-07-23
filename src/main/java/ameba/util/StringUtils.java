package ameba.util;

/**
 * @author icode
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static CharSequence getPrettyError(String[] sourceLines, int line, int column, int start, int stop, int show_lines) {
        StringBuilder sb = new StringBuilder(128);
        for (int i = line - show_lines; i < line; i++) {
            if (i >= 0) {
                String sourceLine = sourceLines[i];

                // 1 个 Tab 变成 4 个空格
                if (i == line - 1) {
                    int origin_column = Math.min(column, sourceLine.length() - 1);
                    for (int j = 0; j < origin_column; j++) {
                        char c = sourceLine.charAt(j);
                        if (c == '\t') {
                            column += 3;
                        } else if (c >= '\u2E80' && c <= '\uFE4F') {
                            column++; // 中日韩统一表意文字（CJK Unified Ideographs）
                        }
                    }
                }
                sourceLine = sourceLine.replaceAll("\\t", "    ");
                sb.append(String.format("%4d: %s%n", i + 1, sourceLine));
            }
        }
        if (start > stop) {
            // <EOF>
            sb.append("      <EOF>\n");
            sb.append("      ^^^^^");
        } else {
            sb.append("      "); // padding
            for (int i = 0; i < column - 1; i++) {
                sb.append(' ');
            }
            for (int i = start; i <= stop; i++) {
                sb.append('^');
            }
        }
        sb.append('\n');
        return sb;
    }
}
