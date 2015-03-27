package ameba.db.ebean.internal;

import ameba.db.ebean.EbeanFeature;
import ameba.db.model.Finder;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Query;
import com.avaje.ebean.text.PathProperties;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
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
import java.util.concurrent.ExecutionException;

/**
 * @author icode
 */
@Priority(Priorities.ENTITY_CODER)
public class EbeanModelProcessor implements WriterInterceptor {

    static String FIELDS_PARAM_NAME = "fields";
    static String SORT_PARAM_NAME = "sort";
    static String PAGE_PARAM_NAME = "page";
    static String PER_PAGE_PARAM_NAME = "per_page";
    static String REQ_TOTAL_COUNT_PARAM_NAME = "req_count";
    static String REQ_TOTAL_COUNT_HEADER_NAME = "X-Total-Count";
    static String WHERE_PARAM_NAME = "where";
    static Integer DEFAULT_PER_PAGE = 20;

    @Context
    private Configuration configuration;

    @PostConstruct
    private void init() {
        final String fieldsParamName = (String) configuration.getProperty(EbeanFeature.FIELDS_PARAM_NAME);
        FIELDS_PARAM_NAME = StringUtils.isNotBlank(fieldsParamName) ? fieldsParamName : FIELDS_PARAM_NAME;

        final String sortParamName = (String) configuration.getProperty(EbeanFeature.SORT_PARAM_NAME);
        SORT_PARAM_NAME = StringUtils.isNotBlank(sortParamName) ? sortParamName : SORT_PARAM_NAME;

        final String pageParamName = (String) configuration.getProperty(EbeanFeature.PAGE_PARAM_NAME);
        PAGE_PARAM_NAME = StringUtils.isNotBlank(pageParamName) ? pageParamName : PAGE_PARAM_NAME;

        final String perPageParamName = (String) configuration.getProperty(EbeanFeature.PER_PAGE_PARAM_NAME);
        PER_PAGE_PARAM_NAME = StringUtils.isNotBlank(perPageParamName) ? perPageParamName : PER_PAGE_PARAM_NAME;

        final String reqTotalCountParamName = (String) configuration.getProperty(EbeanFeature.REQ_TOTAL_COUNT_PARAM_NAME);
        REQ_TOTAL_COUNT_PARAM_NAME = StringUtils.isNotBlank(reqTotalCountParamName) ? perPageParamName : REQ_TOTAL_COUNT_PARAM_NAME;

        final String reqTotalCountHeaderName = (String) configuration.getProperty(EbeanFeature.REQ_TOTAL_COUNT_HEADER_NAME);
        REQ_TOTAL_COUNT_HEADER_NAME = StringUtils.isNotBlank(reqTotalCountHeaderName) ? perPageParamName : REQ_TOTAL_COUNT_HEADER_NAME;

        final String whereParamName = (String) configuration.getProperty(EbeanFeature.WHERE_PARAM_NAME);
        WHERE_PARAM_NAME = StringUtils.isNotBlank(whereParamName) ? whereParamName : WHERE_PARAM_NAME;

        final String defaultPerPage = (String) configuration.getProperty(EbeanFeature.DEFAULT_PER_PAGE_PARAM_NAME);
        if (StringUtils.isNotBlank(defaultPerPage)) {
            try {
                DEFAULT_PER_PAGE = Integer.parseInt(defaultPerPage);
            } catch (Exception e) {
                DEFAULT_PER_PAGE = null;
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
    public static void applyPathProperties(MultivaluedMap<String, String> queryParams, Query query) {
        List<String> selectables = queryParams.get(FIELDS_PARAM_NAME);
        if (selectables != null)
            for (String s : selectables) {
                PathProperties pathProperties = PathProperties.parse(s);
                pathProperties.apply(query);
            }
    }

    public static void applyOrderBy(MultivaluedMap<String, String> queryParams, Query query) {
        String orderByClause = getSingleParam(queryParams.get(EbeanModelProcessor.SORT_PARAM_NAME));
        if (StringUtils.isNotBlank(orderByClause)) {
            query.order(orderByClause);
        }
    }

    public static FutureRowCount applyPageList(MultivaluedMap<String, String> queryParams, Query query) {

        Integer maxRows = getSingleIntegerParam(queryParams.get(EbeanModelProcessor.PER_PAGE_PARAM_NAME));

        if (maxRows == null && DEFAULT_PER_PAGE != null && DEFAULT_PER_PAGE > 0) {
            maxRows = DEFAULT_PER_PAGE;
        }

        if (maxRows != null) {
            if (maxRows <= 0) {
                maxRows = 20;
            }
            query.setMaxRows(maxRows);
        }

        Integer firstRow = getSingleIntegerParam(queryParams.get(EbeanModelProcessor.PAGE_PARAM_NAME));
        if (firstRow != null) {
            if (firstRow < 1) {
                firstRow = 1;
            }
            firstRow--;
            query.setFirstRow(firstRow);
        }

        Integer reqTotalCount = getSingleIntegerParam(queryParams.get(EbeanModelProcessor.PAGE_PARAM_NAME));
        if (reqTotalCount != null && 1 == reqTotalCount) {
            return query.findFutureRowCount();
        }

        return null;
    }

    public static void applyWhere(MultivaluedMap<String, String> queryParams, Query query) {
        List<String> wheres = queryParams.get(EbeanModelProcessor.WHERE_PARAM_NAME);
        if (wheres != null)
            for (String w : wheres) {
                query.where(w);
            }
    }

    public static FutureRowCount applyUriQuery(MultivaluedMap<String, String> queryParams, Query query) {
        applyPathProperties(queryParams, query);
        applyWhere(queryParams, query);
        applyOrderBy(queryParams, query);
        return applyPageList(queryParams, query);
    }

    public static void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        if (rowCount != null) {
            try {
                headerParams.putSingle(REQ_TOTAL_COUNT_HEADER_NAME, rowCount.get());
            } catch (InterruptedException e) {
                headerParams.putSingle(REQ_TOTAL_COUNT_HEADER_NAME, query.findRowCount());
            } catch (ExecutionException e) {
                headerParams.putSingle(REQ_TOTAL_COUNT_HEADER_NAME, query.findRowCount());
            }
        }
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
            FutureRowCount rowCount = applyUriQuery(queryParams, query);
            List list;
            if (o instanceof FutureList) {
                list = query.findFutureList().getUnchecked();
            } else {
                list = query.findList();
            }

            applyRowCountHeader(context.getHeaders(), query, rowCount);

            context.setEntity(list);

            Class clazz = list.getClass();

            context.setType(clazz);

            context.setGenericType(clazz);
        }

        context.proceed();
    }
}
