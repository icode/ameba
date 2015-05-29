package ameba.mvc.template.httl.internal;

import httl.spi.Filter;
import httl.spi.filters.CommentSyntaxFilter;
import httl.spi.filters.MultiFilter;

/**
 * @author icode
 */
public class ClearCommentFilter extends MultiFilter {

    private boolean removeDirectiveBlankLine;

    public ClearCommentFilter() {
        Filter[] filters = new Filter[3];
        CommentSyntaxFilter filter = new CommentSyntaxFilter();
        filter.setCommentLeft("//");
        filter.setCommentRight("/n");
        filter.setRemoveDirectiveBlankLine(removeDirectiveBlankLine);
        filters[0] = filter;

        filter = new CommentSyntaxFilter();
        filter.setCommentLeft("//");
        filter.setCommentRight("/n/r");
        filter.setRemoveDirectiveBlankLine(removeDirectiveBlankLine);
        filters[1] = filter;

        filter = new CommentSyntaxFilter();
        filter.setCommentLeft("/*");
        filter.setCommentRight("*/");
        filter.setRemoveDirectiveBlankLine(removeDirectiveBlankLine);
        filters[2] = filter;

        setFilters(filters);
    }

    public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
        this.removeDirectiveBlankLine = removeDirectiveBlankLine;
    }
}
