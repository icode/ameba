package ameba.core;

import ameba.mvc.ErrorPageGenerator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jersey.repackaged.com.google.common.base.Function;
import jersey.repackaged.com.google.common.collect.Collections2;
import jersey.repackaged.com.google.common.collect.Lists;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author icode
 */
public class Frameworks {
    private Frameworks() {
    }

    public static ErrorPageGenerator getErrorPageGenerator(ServiceLocator serviceLocator) {
        final Set<ExceptionMapper> exceptionMappers = Sets.newLinkedHashSet();
        exceptionMappers.addAll(Providers.getCustomProviders(serviceLocator, ExceptionMapper.class));
        exceptionMappers.addAll(Providers.getProviders(serviceLocator, ExceptionMapper.class));
        for (ExceptionMapper t : exceptionMappers) {
            if (t instanceof ErrorPageGenerator) {
                return (ErrorPageGenerator) t;
            }
        }
        return null;
    }

    public static MessageBodyWriter<Viewable> getViewableMessageBodyWriter(MessageBodyWorkers workers) {
        return workers.getMessageBodyWriter(Viewable.class, Viewable.class,
                new Annotation[]{}, null);
    }

    public static <T> List<ServiceHandle<T>> getServiceHandles(final ServiceLocator locator, final Class<T> contract,
                                                               final Annotation... qualifiers) {

        final List<ServiceHandle<T>> allServiceHandles = qualifiers == null
                ? locator.getAllServiceHandles(contract)
                : locator.getAllServiceHandles(contract, qualifiers);

        final ArrayList<ServiceHandle<T>> serviceHandles = new ArrayList<ServiceHandle<T>>();
        for (final ServiceHandle handle : allServiceHandles) {
            //noinspection unchecked
            serviceHandles.add((ServiceHandle<T>) handle);
        }
        return serviceHandles;
    }

    /**
     * Get the iterable of {@link RankedProvider providers} (default) registered for the given service provider
     * contract in the underlying {@link ServiceLocator HK2 service locator} container.
     *
     * @param <T>      service provider contract Java type.
     * @param locator  underlying HK2 service locator.
     * @param contract service provider contract.
     * @return iterable of all available ranked service providers for the contract. Return value is never null.
     */
    public static <T> Iterable<RankedProvider<T>> getRankedProviders(final ServiceLocator locator, final Class<T> contract) {
        final List<ServiceHandle<T>> providers = getServiceHandles(locator, contract);

        final Map<ActiveDescriptor<T>, RankedProvider<T>> providerMap =
                Maps.newLinkedHashMap();

        for (final ServiceHandle<T> provider : providers) {
            final ActiveDescriptor<T> key = provider.getActiveDescriptor();
            if (!providerMap.containsKey(key)) {
                final Set<Type> contractTypes = key.getContractTypes();
                final Class<?> implementationClass = key.getImplementationClass();
                boolean proxyGenerated = true;
                for (Type ct : contractTypes) {
                    if (((Class<?>) ct).isAssignableFrom(implementationClass)) {
                        proxyGenerated = false;
                        break;
                    }
                }
                providerMap.put(key,
                        new RankedProvider<>(provider.getService(), key.getRanking(), proxyGenerated ? contractTypes : null));
            }
        }

        return providerMap.values();
    }

    /**
     * Sort given providers with {@link RankedComparator ranked comparator}.
     *
     * @param comparator comparator to sort the providers with.
     * @param providers  providers to be sorted.
     * @param <T>        service provider contract Java type.
     * @return sorted {@link Iterable iterable} instance containing given providers.
     * The returned value is never {@code null}.
     */
    @SuppressWarnings("TypeMayBeWeakened")
    public static <T> Iterable<T> sortRankedProviders(final RankedComparator<T> comparator,
                                                      final Iterable<RankedProvider<T>> providers) {
        final List<RankedProvider<T>> rankedProviders = Lists.newArrayList(providers);

        Collections.sort(rankedProviders, comparator);

        return Collections2.transform(rankedProviders, new Function<RankedProvider<T>, T>() {
            @Override
            public T apply(final RankedProvider<T> input) {
                return input.getProvider();
            }
        });
    }
}
