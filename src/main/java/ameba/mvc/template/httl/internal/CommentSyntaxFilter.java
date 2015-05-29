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

    private boolean removeDirectiveBlankLine;

    /**
     * httl.properties: comment.left=&lt;!--
     */
    @Reqiured
    public void setCommentLeft(String commentLeft) {
        this.commentLeft = commentLeft;
    }

    /**
     * httl.properties: comment.right=--&gt;
     */
    @Reqiured
    public void setCommentRight(String commentRight) {
        this.commentRight = commentRight;
    }

    /**
     * httl.properties: remove.directive.blank.line=true
     */
    public void setRemoveDirectiveBlankLine(boolean removeDirectiveBlankLine) {
        this.removeDirectiveBlankLine = removeDirectiveBlankLine;
    }

    public String filter(String key, String value) {
        value = value.replaceAll(commentLeft + ".*?" + commentRight, "\n");
        if (removeDirectiveBlankLine) {
            value = StringUtils.trimBlankLine(value, true, true);
        }
        return value;
    }

}