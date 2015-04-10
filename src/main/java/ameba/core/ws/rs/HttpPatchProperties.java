package ameba.core.ws.rs;

import com.google.common.collect.Lists;

import java.util.List;

import static ameba.message.internal.MediaType.APPLICATION_JSON_PATCH;

/**
 * @author icode
 */
public class HttpPatchProperties {
    public static final String METHOD_NAME = "PATCH";
    public static final String ACCEPT_PATCH_HEADER = "Accept-Patch";
    public static final List<String> SUPPORT_PATCH_MEDIA_TYPES =
            Lists.newArrayList(
                    APPLICATION_JSON_PATCH
            );
}
