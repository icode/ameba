package ameba.db.ebean;

import ameba.util.ClassUtils;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.common.BeanMap;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;

/**
 * <p>EbeanUtils class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class EbeanUtils {
    private EbeanUtils() {
    }

    /**
     * <p>forceUpdateAllProperties.</p>
     *
     * @param server a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     * @param model  a T object.
     * @param <T>    a T object.
     */
    @SuppressWarnings("unchecked")
    public static <T> void forceUpdateAllProperties(SpiEbeanServer server, T model) {
        forceUpdateAllProperties(server.getBeanDescriptor((Class<T>) model.getClass()), model);
    }

    /**
     * <p>forceUpdateAllProperties.</p>
     *
     * @param beanDescriptor a {@link com.avaje.ebeaninternal.server.deploy.BeanDescriptor} object.
     * @param model          a T object.
     * @param <T>            a T object.
     */
    public static <T> void forceUpdateAllProperties(BeanDescriptor<T> beanDescriptor, T model) {
        EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
        intercept.setLoaded();
        int idIndex = beanDescriptor.getIdProperty().getPropertyIndex();
        for (int i = 0; i < intercept.getPropertyLength(); i++) {
            if (i != idIndex) {
                intercept.markPropertyAsChanged(i);
                intercept.setLoadedProperty(i);
            }
        }
    }

    /**
     * <p>forcePropertiesLoaded.</p>
     *
     * @param model a T object.
     * @param <T>   a T object.
     */
    public static <T> void forcePropertiesLoaded(T model) {
        if (model == null) return;
        if (model instanceof EntityBean) {
            EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
            intercept.setLoaded();
            for (int i = 0; i < intercept.getPropertyLength(); i++) {
                intercept.setLoadedProperty(i);
            }
        } else if (model instanceof Collection) {
            for (Object m : (Collection) model) {
                forcePropertiesLoaded(m);
            }
        } else if (model instanceof BeanMap) {
            forcePropertiesLoaded(((BeanMap) model).values());
        } else if (model.getClass().isArray()) {
            for (Object m : (Object[]) model) {
                forcePropertiesLoaded(m);
            }
        }
    }

    /**
     * <p>disableLazyLoad.</p>
     *
     * @param model a T object.
     * @param <T>   a T object.
     */
    public static <T> void disableLazyLoad(T model) {
        if (model == null) return;
        disableLazyLoad(model, Sets.newHashSet());
    }

    private static <T> void disableLazyLoad(T model, Set<Object> processed) {
        if (model == null || processed.contains(model)) return;
        processed.add(model);
        if (model instanceof Collection) {
            for (Object m : (Collection) model) {
                disableLazyLoad(m, processed);
            }
        } else if (model instanceof BeanMap) {
            disableLazyLoad(((BeanMap) model).values(), processed);
        } else if (model.getClass().isArray()) {
            for (Object m : (Object[]) model) {
                disableLazyLoad(m, processed);
            }
        } else if (isLazyLoadProcessType(model.getClass())) {
            if (model instanceof EntityBean) {
                EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
                intercept.setDisableLazyLoad(true);
            }
            Class sClass = model.getClass();
            while (isLazyLoadProcessType(sClass)) {
                for (Field f : sClass.getDeclaredFields()) {
                    Class fType = f.getType();
                    if (isLazyLoadProcessType(fType)
                            && !f.getName().toLowerCase().startsWith("_")) {
                        try {
                            Object val = f.get(model);
                            disableLazyLoad(val, processed);
                        } catch (IllegalAccessException e) {
                            // no op
                        }
                    }
                }
                sClass = sClass.getSuperclass();
            }
        }
    }

    private static boolean isLazyLoadProcessType(Class sClass) {
        return sClass != null
                && (!ClassUtils.isPrimitiveOrWrapper(sClass)
                && !sClass.getName().startsWith("java.lang")
                && !EntityBeanIntercept.class.isAssignableFrom(sClass)
                || sClass.isArray());
    }

    @SuppressWarnings("unchecked")
    public static <T> T covertOriginalObject(Object model) throws IllegalAccessException {
        if (model != null) {
            Class sClass = model.getClass();
            while (isLazyLoadProcessType(sClass)) {
                for (Field f : sClass.getDeclaredFields()) {
                    int ms = f.getModifiers();
                    Class fType = f.getType();
                    Object val = f.get(model);
                    if (val != null && !Modifier.isFinal(ms)) {
                        Boolean isAcc = null;
                        if (!Modifier.isPublic(ms)) {
                            isAcc = f.isAccessible();
                            f.setAccessible(true);
                        }
                        Class valClass = val.getClass();
                        if (BeanCollection.class.isAssignableFrom(valClass)) {
                            Object valRef;
                            if (BeanMap.class.isAssignableFrom(valClass)) {
                                valRef = ((BeanMap) val).getActualMap();

                            } else {
                                valRef = ((BeanCollection) val).getActualDetails();
                            }
                            if (fType.isInstance(valRef)) {
                                val = valRef;
                                f.set(model, val);
                            }
                        }
                        if (isAcc != null) {
                            f.setAccessible(isAcc);
                        }
                    }
                }
                sClass = sClass.getSuperclass();
            }
            if (model instanceof BeanMap) {
                return (T) ((BeanMap) model).getActualMap();
            } else if (model instanceof SqlRow) {
                return (T) Maps.newLinkedHashMap((SqlRow) model);
            } else if (model instanceof BeanCollection) {
                return (T) ((BeanCollection) model).getActualDetails();
            }
        }
        return (T) model;
    }
}
