package ameba.db.ebean.internal;

import ameba.db.ebean.EbeanFeature;
import ameba.db.model.Finder;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.Query;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebean.text.PathProperties;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.MessageBodyWorkers;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.List;

/**
 * @author icode
 */
@Priority(Priorities.ENTITY_CODER)
public class EbeanModelWriter implements WriterInterceptor {

    static String SELECTABLE_PARAM_NAME = "select";
    static String ORDER_BY_PARAM_NAME = "sort";
    static String MAX_ROWS_PARAM_NAME = "maxrows";
    static String FIRST_ROW_PARAM_NAME = "firstrow";
    static String WHERE_PARAM_NAME = "where";
    static Integer DEFAULT_MAX_ROWS = 20;

    @Context
    private Configuration configuration;

    @Inject
    private Provider<MessageBodyWorkers> workers;

    @PostConstruct
    private void init() {
        final String selectableParamName = (String) configuration.getProperty(EbeanFeature.SELECTABLE_PARAM_NAME);
        SELECTABLE_PARAM_NAME = StringUtils.isNotBlank(selectableParamName) ? selectableParamName : SELECTABLE_PARAM_NAME;

        final String orderByParamName = (String) configuration.getProperty(EbeanFeature.ORDER_BY_PARAM_NAME);
        ORDER_BY_PARAM_NAME = StringUtils.isNotBlank(orderByParamName) ? orderByParamName : ORDER_BY_PARAM_NAME;

        final String maxRowsParamName = (String) configuration.getProperty(EbeanFeature.MAX_ROWS_PARAM_NAME);
        MAX_ROWS_PARAM_NAME = StringUtils.isNotBlank(maxRowsParamName) ? maxRowsParamName : MAX_ROWS_PARAM_NAME;

        final String firstRowParamName = (String) configuration.getProperty(EbeanFeature.FIRST_ROW_PARAM_NAME);
        FIRST_ROW_PARAM_NAME = StringUtils.isNotBlank(firstRowParamName) ? firstRowParamName : FIRST_ROW_PARAM_NAME;

        final String whereParamName = (String) configuration.getProperty(EbeanFeature.WHERE_PARAM_NAME);
        WHERE_PARAM_NAME = StringUtils.isNotBlank(whereParamName) ? whereParamName : WHERE_PARAM_NAME;

        final String defaultMaxRows = (String) configuration.getProperty(EbeanFeature.DEFAULT_MAX_ROWS_PARAM_NAME);
        if (StringUtils.isNotBlank(defaultMaxRows)) {
            try {
                DEFAULT_MAX_ROWS = Integer.parseInt(defaultMaxRows);
            } catch (Exception e) {
                DEFAULT_MAX_ROWS = null;
            }
        }
    }


    @Context
    private UriInfo uriInfo;

    public boolean isWriteable(Class<?> type) {
        return Finder.class.isAssignableFrom(type)
                || Query.class.isAssignableFrom(type)
                || ExpressionList.class.isAssignableFrom(type)
                || FutureList.class.isAssignableFrom(type);
    }

    /**
     * parse uri
     * <p/>
     * e.g.
     * <p/>
     * ?select=id,name,props(p1,p2,p3)
     * <p/>
     * ?select=(id,name,props(p1,p2,p3))
     *
     * @param query query
     */
    protected static void applyPathProperties(MultivaluedMap<String, String> queryParams, Query query) {
        List<String> selectables = queryParams.get(SELECTABLE_PARAM_NAME);
        if (selectables != null)
            for (String s : selectables) {
                PathProperties pathProperties = PathProperties.parse(s);
                pathProperties.apply(query);
            }
    }

    /**
     * Return a single Integer parameter.
     */
    protected static Integer getSingleIntegerParam(List<String> list) {
        String s = getSingleParam(list);
        if (s != null) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Return a single parameter value.
     */
    protected static String getSingleParam(List<String> list) {
        if (list != null && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    protected static void applyOrderBy(MultivaluedMap<String, String> queryParams, Query query) {
        String orderByClause = getSingleParam(queryParams.get(EbeanModelWriter.ORDER_BY_PARAM_NAME));
        if (StringUtils.isNotBlank(orderByClause)) {
            query.order(orderByClause);
        }
    }

    protected static void applyPageList(MultivaluedMap<String, String> queryParams, Query query) {

        Integer maxRows = getSingleIntegerParam(queryParams.get(EbeanModelWriter.MAX_ROWS_PARAM_NAME));

        if (maxRows == null && DEFAULT_MAX_ROWS != null && DEFAULT_MAX_ROWS > 0) {
            maxRows = DEFAULT_MAX_ROWS;
        }

        if (maxRows != null) {
            query.setMaxRows(maxRows);
        }

        Integer firstRow = getSingleIntegerParam(queryParams.get(EbeanModelWriter.FIRST_ROW_PARAM_NAME));
        if (firstRow != null) {
            query.setFirstRow(firstRow);
        }
    }

    protected static void applyWhere(MultivaluedMap<String, String> queryParams, Query query) {
        List<String> wheres = queryParams.get(EbeanModelWriter.WHERE_PARAM_NAME);
        if (wheres != null)
            for (String w : wheres) {
                query.where(w);
            }
    }

    protected static void applyUriQuery(MultivaluedMap<String, String> queryParams, Query query) {
        applyPathProperties(queryParams, query);
        applyOrderBy(queryParams, query);
        applyPageList(queryParams, query);
        applyWhere(queryParams, query);
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object o = context.getEntity();
        if (o != null && isWriteable(o.getClass())) {

            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            Query query = null;
            if (o instanceof Finder) {
                query = ((Finder) o).query();
            } else if (o instanceof Query) {
                query = (Query) o;
            } else if (o instanceof ExpressionList) {
                query = ((ExpressionList) o).query();
            } else if (o instanceof FutureList) {
                query = ((FutureList) o).getQuery();
            }
            applyUriQuery(queryParams, query);

            List list = query.findList();

            context.setEntity(list);

            Class clazz = list.getClass();

            context.setType(clazz);

            context.setGenericType(clazz);
        }

        context.proceed();
    }
}
