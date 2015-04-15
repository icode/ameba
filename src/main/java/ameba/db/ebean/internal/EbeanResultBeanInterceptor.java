package ameba.db.ebean.internal;

import ameba.db.ebean.EbeanUtils;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.common.AbstractBeanCollection;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebean.common.BeanMap;
import com.avaje.ebean.common.BeanSet;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>EbeanResultBeanInterceptor class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Priority(Integer.MAX_VALUE)
public class EbeanResultBeanInterceptor implements WriterInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();
        EbeanUtils.disableLazyLoad(entity);
        if (entity instanceof BeanList) {
            setListType(context, (BeanList) entity, entity);
        } else if (entity instanceof BeanSet) {
            setSetType(context, (BeanSet) entity, entity);
        } else if (entity instanceof BeanMap) {
            setMapType(context, entity);
        }

        context.proceed();
    }

    private void setListType(WriterInterceptorContext context, BeanList root, Object entity) {
        if (entity instanceof BeanList) {
            List list = ((BeanList) entity).getActualList();
            setListType(context, root, list);
        } else {
            if (entity instanceof List) {
                setType(context, root, entity);
            } else if (entity == null) {
                context.setType(List.class);
            }
        }
    }

    private void setSetType(WriterInterceptorContext context, BeanSet root, Object entity) {
        if (entity instanceof BeanSet) {
            Set list = ((BeanSet) entity).getActualSet();
            setSetType(context, root, list);
        } else {
            if (entity instanceof Set) {
                setType(context, root, entity);
            } else if (entity == null) {
                context.setType(Set.class);
            }
        }
    }

    private void setType(WriterInterceptorContext context, AbstractBeanCollection collection, Object entity) {
        context.setType(entity.getClass());

        EntityBean bean = collection.getOwnerBean();

        Class type;

        if (bean != null) {
            type = bean.getClass();
        } else if (collection.getActualDetails().size() > 0) {
            Object o = collection.getActualDetails().iterator().next();
            if (o != null) {
                type = o.getClass();
            } else {
                type = EntityBean.class;
            }
        } else {
            type = EntityBean.class;
        }

        context.setGenericType(type);
    }

    private void setMapType(WriterInterceptorContext context, Object entity) {
        if (entity instanceof BeanMap) {
            Map map = ((BeanMap) entity).getActualMap();
            setMapType(context, map);
        } else {
            if (entity instanceof Map) {
                context.setType(entity.getClass());
                context.setGenericType(Object.class);
            } else if (entity == null) {
                context.setType(Map.class);
            }
        }
    }
}
