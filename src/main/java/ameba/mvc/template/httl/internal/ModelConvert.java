package ameba.mvc.template.httl.internal;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Provider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class ModelConvert {
    private static Provider<ObjectMapper> MAPPER_THREAD_LOCAL;

    public static class Binder implements Feature {

        @Context
        private Provider<ObjectMapper> provider;

        @Override
        public boolean configure(FeatureContext context) {
            MAPPER_THREAD_LOCAL = provider;
            return true;
        }
    }
}
