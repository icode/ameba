package ameba.mvc.template.httl.internal;

import httl.spi.filters.AbstractFilter;
import httl.util.Reqiured;
import httl.util.StringUtils;

/**
 * @author icode
 */
public class CommentSyntaxFilter extends AbstractFilter {

    private String commentLeft;

    private String commentRight;
    private String replace = "";

    private boolean removeDirectiveBlankLine;

    /**
     * httl.properties: comment.left=&lt;!--
     * @param commentLeft comment left
     */
    @Reqiured
    public void setCommentLeft(String commentLeft) {
        this.commentLeft = commentLeft;
    }

    /**
     * httl.properties: comment.right=--&gt;
     * @param commentRight comment right
     */
    @Reqiured
    public void setCommentRight(String commentRight) {
        this.commentRight = commentRight;
    }

    public void setCommentReplaceWith(String replace) {
        this.replace = replace;
    }

    /**
     * httl.properties: remove.directive.blank.line=true
     * @param removeDirectiveBlankLine is remove directive
     */
    public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
        this.removeDirectiveBlankLine = removeDirectiveBlankLine;
    }

    public String filter(String key, String value) {
        value = value.replaceAll(commentLeft + ".*?" + commentRight, replace);
        if (removeDirectiveBlankLine) {
            value = StringUtils.trimBlankLine(value, true, true);
        }
        return value;
    }

}