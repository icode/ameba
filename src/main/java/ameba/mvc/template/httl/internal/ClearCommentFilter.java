package ameba.mvc.template.httl.internal;

import httl.spi.Filter;
import httl.spi.filters.AbstractFilter;

import java.util.regex.Pattern;

/**
 * @author icode
 */
public class ClearCommentFilter extends AbstractFilter {

    private CommentSyntaxFilter[] filters;

    public ClearCommentFilter() {
        filters = new CommentSyntaxFilter[3];
        CommentSyntaxFilter filter = new CommentSyntaxFilter();
        filter.setCommentLeft("//");
        filter.setCommentRight("\r\n");
        filter.setCommentReplaceWith("\n");
        filters[0] = filter;

        filter = new CommentSyntaxFilter();
        filter.setCommentLeft("//");
        filter.setCommentRight("\n");
        filter.setCommentReplaceWith("\n");
        filters[1] = filter;

        filter = new CommentSyntaxFilter();
        filter.setCommentLeft(Pattern.quote("/*"));
        filter.setCommentRight(Pattern.quote("*/"));
        filter.setCommentReplaceWith("\n");
        filters[2] = filter;

    }

    public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
        for (CommentSyntaxFilter filter : filters) {
            filter.setRemoveDirectiveBlankLine(removeDirectiveBlankLine);
        }
    }

    @Override
    public String filter(String key, String value) {
        if (filters == null || filters.length == 0) {
            return value;
        }
        if (filters.length == 1) {
            return filters[0].filter(key, value);
        }
        for (Filter filter : filters) {
            value = filter.filter(key, value);
        }
        return value;
    }
}
