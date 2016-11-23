package ameba.core.ws.rs;

import com.google.common.collect.Lists;

import java.util.List;

import static ameba.message.internal.MediaType.APPLICATION_JSON_PATCH;

/**
 * <p>HttpPatchProperties class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
 */
public class HttpPatchProperties {
    /**
     * Constant <code>METHOD_NAME="PATCH"</code>
     */
    public static final String METHOD_NAME = "PATCH";
    /**
     * Constant <code>ACCEPT_PATCH_HEADER="Accept-Patch"</code>
     */
    public static final String ACCEPT_PATCH_HEADER = "Accept-Patch";
    /**
     * Constant <code>SUPPORT_PATCH_MEDIA_TYPES</code>
     */
    public static final List<String> SUPPORT_PATCH_MEDIA_TYPES =
            Lists.newArrayList(
                    APPLICATION_JSON_PATCH
            );
}
