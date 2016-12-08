package ameba.message.internal;

/**
 * Parses Uri segments like :(id,name,shippingAddress(*),contacts(*)) so that
 * the response can be customised for performance.
 *
 * @author rbygrave
 * @author icode
 *
 */
public class PathPropertiesParser {

    // :(a,b,c(d,e,f))

    private final BeanPathProperties pathProps;

    private final String source;

    private final char[] chars;

    private final int eof;

    private int pos;
    private int startPos;

    private BeanPathProperties.Props currentPathProps;

    private PathPropertiesParser(String src) {

        if (src.startsWith(":")) {
            src = src.substring(1);
        }
        this.pathProps = new BeanPathProperties();
        this.source = src;
        this.chars = src.toCharArray();
        this.eof = chars.length;

        if (eof > 0) {
            currentPathProps = pathProps.getRootProperties();
            parse();
        }
    }

    /**
     * Use {@link BeanPathProperties#parse(String)}.
     */
    static BeanPathProperties parse(String source) {
        return new PathPropertiesParser(source).pathProps;
    }

    private String getPath() {
        do {
            char c1 = chars[pos++];
            switch (c1) {
                case '(':
                    return currentWord();
                default:
                    if (pos == 1) {
                        return "";
                    }
            }
        } while (pos < eof);
        throw new RuntimeException("Hit EOF while reading sectionTitle from " + startPos);
    }

    private void parse() {

        do {
            String path = getPath();
            pushPath(path);
            parseSection();

        } while (pos < eof);
    }

    private void parseSection() {
        do {
            char c1 = chars[pos++];
            switch (c1) {
                case '(':
                    addSubPath();
                    break;
                case ',':
                    addCurrentProperty();
                    break;
                case ':':
                    // start new section
                    startPos = pos;
                    return;
                case ')':
                    // end of section
                    addCurrentProperty();
                    popSubpath();
                    break;
                default:
            }

        } while (pos < eof);
        if (startPos < pos) {
            String currentWord = source.substring(startPos, pos);
            currentPathProps.addProperty(currentWord);
        }
    }

    private void addSubPath() {
        pushPath(currentWord());
    }

    private void addCurrentProperty() {
        String w = currentWord();
        if (w.length() > 0) {
            currentPathProps.addProperty(w);
        }
    }

    private String currentWord() {
        if (startPos == pos) {
            return "";
        }
        String currentWord = source.substring(startPos, pos - 1);
        startPos = pos;
        return currentWord;
    }

    private void pushPath(String title) {

        if (!"".equals(title)) {
            currentPathProps = currentPathProps.addChild(title);
        }
    }

    private void popSubpath() {

        currentPathProps = currentPathProps.getParent();
    }

}
