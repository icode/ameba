package ameba.validation;

import ameba.core.Application;
import ameba.i18n.Messages;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;
import org.glassfish.jersey.server.validation.internal.ValidationBinder;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author icode
 */
public final class ValidationFeature implements Feature {

    public static final String VALIDATION_MESSAGE_BUNDLE_NAME = Messages.BUNDLE_DIR + "validation_message";

    @Override
    public boolean configure(final FeatureContext context) {
        // disable Jersey default BeanValidation feature
        context.property(ServerProperties.BV_FEATURE_DISABLE, "true");
        context.register(new ValidationBinder())
                .register(ValidationExceptionMapper.class)
                .register(ValidationConfigurationContextResolver.class);
        return true;
    }

    public static class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

        @Inject
        private Application application;
        @Context
        private ResourceContext resourceContext;

        @Override
        public ValidationConfig getContext(final Class<?> type) {
            return new ValidationConfig()
                    .constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class))
                    .parameterNameProvider(new ParanamerParameterNameProvider())
                    .messageInterpolator(
                            new ResourceBundleMessageInterpolator(
                                    buildBundleLocator(VALIDATION_MESSAGE_BUNDLE_NAME),
                                    buildBundleLocator(Messages.BUNDLE_NAME),
                                    application.getMode().isProd()
                            )
                    );
        }

        private ResourceBundleLocator buildBundleLocator(final String name) {
            return new ResourceBundleLocator() {
                @Override
                public ResourceBundle getResourceBundle(Locale locale) {
                    return Messages.getResourceBundle(name, locale);
                }
            };
        }
    }
}