package ameba.db.ebean.internal;

import ameba.db.dsl.QueryDSL;
import ameba.db.ebean.EbeanFeature;
import ameba.db.ebean.EbeanUtils;
import ameba.db.ebean.filter.EbeanExprInvoker;
import ameba.db.ebean.filter.WhereExprApplier;
import ameba.db.model.Finder;
import ameba.message.filtering.EntityFieldsFilteringFeature;
import ameba.message.internal.BeanPathProperties;
import io.ebean.*;
import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanList;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * <p>ModelInterceptor class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
@Singleton
@Priority(Priorities.ENTITY_CODER)
public class ModelInterceptor implements WriterInterceptor {

    private final static Integer SYS_DEFAULT_PER_PAGE = 20;
    private static String FIELDS_PARAM_NAME = "fields";
    private static String SORT_PARAM_NAME = "sort";
    private static String PAGE_PARAM_NAME = "page";
    private static String PER_PAGE_PARAM_NAME = "per_page";
    private static String REQ_TOTAL_COUNT_PARAM_NAME = "req_count";
    private static String REQ_TOTAL_COUNT_HEADER_NAME = "X-Total-Count";
    private static String FILTER_PARAM_NAME = "filter";
    private static Integer DEFAULT_PER_PAGE = SYS_DEFAULT_PER_PAGE;
    private static int MAX_PER_PAGE = 1000;
    @Context
    private Provider<Configuration> configurationProvider;
    @Context
    private Provider<UriInfo> uriInfoProvider;
    @Inject
    private ServiceLocator locator;

    /**
     * <p>getFieldsParamName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getFieldsParamName() {
        return FIELDS_PARAM_NAME;
    }

    /**
     * <p>getSortParamName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getSortParamName() {
        return SORT_PARAM_NAME;
    }

    /**
     * <p>getPageParamName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getPageParamName() {
        return PAGE_PARAM_NAME;
    }

    /**
     * <p>getPerPageParamName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getPerPageParamName() {
        return PER_PAGE_PARAM_NAME;
    }

    /**
     * <p>getReqTotalCountParamName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getReqTotalCountParamName() {
        return REQ_TOTAL_COUNT_PARAM_NAME;
    }

    /**
     * <p>getReqTotalCountHeaderName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getReqTotalCountHeaderName() {
        return REQ_TOTAL_COUNT_HEADER_NAME;
    }

    /**
     * <p>getFilterParamName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getFilterParamName() {
        return FILTER_PARAM_NAME;
    }

    /**
     * <p>getDefaultPerPage.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public static Integer getDefaultPerPage() {
        return DEFAULT_PER_PAGE;
    }

    /**
     * Return a single Integer parameter.
     *
     * @param list a {@link java.util.List} object.
     * @return a {@link java.lang.Integer} object.
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
     *
     * @param list a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String getSingleParam(List<String> list) {
        if (list != null && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * apply query parameters to select/fetch
     * <br>
     * ?fields=id,name,filed1(p1,p2,p3)
     * <br>
     * ?fields=(id,name,filed1(p1,p2,p3))
     *
     * @param query       query
     * @param queryParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     */
    public static void applyFetchProperties(MultivaluedMap<String, String> queryParams, Query query) {
        List<String> selectables = queryParams.get(FIELDS_PARAM_NAME);
        if (selectables != null) {
            StringBuilder selectBuilder = new StringBuilder();

            OrmQueryDetail detail = ((SpiQuery) query).getDetail();

            OrmQueryProperties base = detail.getChunk(null, false);
            if (base != null && StringUtils.isNotBlank(base.getProperties())) {
                // 获取已经设置的select
                selectBuilder.append(base.getProperties());
            }
            for (String s : selectables) {
                if (StringUtils.isBlank(s)) {
                    continue;
                }
                if (!s.startsWith("(")) {
                    s = "(" + s;
                }
                if (!s.startsWith(")")) {
                    s += ")";
                }
                BeanPathProperties pathProperties = BeanPathProperties.parse(s);
                for (BeanPathProperties.Props props : pathProperties.getPathProps()) {
                    String path = props.getPath();
                    String propsStr = props.getPropertiesAsString();

                    if (StringUtils.isEmpty(path)) {
                        if (selectBuilder.length() > 0) {
                            selectBuilder.append(",");
                        }
                        if (propsStr.length() > 0)
                            selectBuilder.append(propsStr);
                    } else if (StringUtils.isNotBlank(path)) {
                        FetchConfig config = null;
                        // 获取已经存在的fetch
                        OrmQueryProperties fetch = detail.getChunk(path, false);
                        if (fetch != null && StringUtils.isNotBlank(fetch.getProperties())) {
                            // 增加客户端传入值
                            propsStr = fetch.getProperties() + "," + propsStr;
                            config = fetch.getFetchConfig();
                        }
                        query.fetch(path, propsStr, config);
                    }
                }
            }
            if (selectBuilder.length() > 0) {
                query.select(selectBuilder.toString());
            }
        }
    }

    /**
     * <p>applyOrderBy.</p>
     *
     * @param queryParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query       a {@link io.ebean.Query} object.
     */
    public static void applyOrderBy(MultivaluedMap<String, String> queryParams, Query query) {
        List<String> orders = queryParams.get(SORT_PARAM_NAME);
        if (orders != null && orders.size() > 0) {
            OrderBy orderBy = query.orderBy();
            for (String order : orders) {
                EbeanUtils.appendOrder(orderBy, order);
            }
        }
    }

    /**
     * <p>fetchRowCount.</p>
     *
     * @param queryParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query       a {@link io.ebean.Query} object.
     * @return a {@link io.ebean.FutureRowCount} object.
     */
    public static FutureRowCount fetchRowCount(MultivaluedMap<String, String> queryParams, Query query) {
        String reqTotalCount = getSingleParam(queryParams.get(REQ_TOTAL_COUNT_PARAM_NAME));
        if (reqTotalCount != null && !"false".equalsIgnoreCase(reqTotalCount) && !"0".equals(reqTotalCount)) {
            return query.findFutureCount();
        }
        return null;
    }

    /**
     * <p>applyPageList.</p>
     *
     * @param queryParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query       a {@link io.ebean.Query} object.
     * @return a {@link io.ebean.FutureRowCount} object.
     */
    public static FutureRowCount applyPageList(MultivaluedMap<String, String> queryParams, Query query) {

        FutureRowCount futureRowCount = fetchRowCount(queryParams, query);

        applyPageConfig(queryParams, query);

        return futureRowCount;
    }

    /**
     * <p>applyPageConfig.</p>
     *
     * @param queryParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query a {@link io.ebean.Query} object.
     */
    public static void applyPageConfig(MultivaluedMap<String, String> queryParams, Query query) {
        Integer maxRows = getSingleIntegerParam(queryParams.get(PER_PAGE_PARAM_NAME));

        if (maxRows == null && DEFAULT_PER_PAGE != null && DEFAULT_PER_PAGE > 0) {
            maxRows = DEFAULT_PER_PAGE;
        }

        if (maxRows != null) {
            if (maxRows <= 0) {
                maxRows = SYS_DEFAULT_PER_PAGE;
            }
            if (MAX_PER_PAGE != -1 && maxRows > MAX_PER_PAGE) {
                maxRows = MAX_PER_PAGE;
            }
            query.setMaxRows(maxRows);
        }

        Integer firstRow = getSingleIntegerParam(queryParams.get(PAGE_PARAM_NAME));
        if (firstRow != null && maxRows != null) {
            if (firstRow < 1) {
                firstRow = 1;
            }
            firstRow--;
            firstRow = firstRow * maxRows;
            query.setFirstRow(firstRow);
        }
    }

    /**
     * /path?filter=p.in(1,2)c.eq('ddd')d.startWith('a')or(f.eq('a')g.startWith(2))
     *
     * @param queryParams uri query params
     * @param query       query
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     * @param <T> a T object.
     */
    public static <T> void applyFilter(MultivaluedMap<String, String> queryParams,
                                       SpiQuery<T> query,
                                       ServiceLocator locator) {
        List<String> wheres = queryParams.get(FILTER_PARAM_NAME);
        if (wheres != null && wheres.size() > 0) {
            EbeanExprInvoker invoker = new EbeanExprInvoker(query, locator);
            WhereExprApplier<T> applier = WhereExprApplier.create(query.where());
            for (String w : wheres) {
                QueryDSL.invoke(
                        w,
                        invoker,
                        applier
                );
            }
        }
    }

    /**
     * apply uri query parameter on query
     *
     * @param queryParams  uri query params
     * @param query        Query
     * @param needPageList need page list
     * @return page list count or null
     * @see #applyFetchProperties
     * @see #applyFilter
     * @see #applyOrderBy
     * @see #applyPageList
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    @SuppressWarnings("unchecked")
    public static FutureRowCount applyUriQuery(MultivaluedMap<String, String> queryParams,
                                               SpiQuery query,
                                               ServiceLocator locator, boolean needPageList) {
        Set<String> inv = query.validate();
        applyFetchProperties(queryParams, query);
        applyFilter(queryParams, query, locator);
        applyOrderBy(queryParams, query);

        EbeanUtils.checkQuery(
                (SpiQuery<?>) query,
                inv,
                null,
                locator
        );
        if (needPageList)
            return applyPageList(queryParams, query);
        return null;
    }

    /**
     * <p>applyUriQuery.</p>
     *
     * @param queryParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query       a {@link io.ebean.Query} object.
     * @return a {@link io.ebean.FutureRowCount} object.
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    public static FutureRowCount applyUriQuery(MultivaluedMap<String, String> queryParams,
                                               SpiQuery query,
                                               ServiceLocator locator) {
        return applyUriQuery(queryParams, query, locator, true);
    }

    /**
     * <p>applyRowCountHeader.</p>
     *
     * @param headerParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query        a {@link io.ebean.Query} object.
     * @param rowCount     a {@link io.ebean.FutureRowCount} object.
     */
    public static void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        if (rowCount != null) {
            try {
                headerParams.putSingle(REQ_TOTAL_COUNT_HEADER_NAME, rowCount.get());
            } catch (InterruptedException | ExecutionException e) {
                headerParams.putSingle(REQ_TOTAL_COUNT_HEADER_NAME, query.findCount());
            }
        }
    }

    @PostConstruct
    private void init() {
        Configuration configuration = configurationProvider.get();
        final String fieldsParamName = (String) configuration.getProperty(EntityFieldsFilteringFeature.QUERY_FIELDS_PARAM_NAME);
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

        final String filterParamName = (String) configuration.getProperty(EbeanFeature.FILTER_PARAM_NAME);
        FILTER_PARAM_NAME = StringUtils.isNotBlank(filterParamName) ? filterParamName : FILTER_PARAM_NAME;

        final String defaultPerPage = (String) configuration.getProperty(EbeanFeature.DEFAULT_PER_PAGE_PARAM_NAME);
        if (StringUtils.isNotBlank(defaultPerPage)) {
            try {
                DEFAULT_PER_PAGE = Integer.parseInt(defaultPerPage);
            } catch (Exception e) {
                DEFAULT_PER_PAGE = null;
            }
        }

        final String maxPerPage = (String) configuration.getProperty(EbeanFeature.MAX_PER_PAGE_PARAM_NAME);
        if (StringUtils.isNotBlank(maxPerPage)) {
            try {
                MAX_PER_PAGE = Integer.parseInt(maxPerPage);
            } catch (Exception e) {
                MAX_PER_PAGE = -1;
            }
        }
    }

    /**
     * <p>isWritable.</p>
     *
     * @param type a {@link java.lang.Class} object.
     * @return a boolean.
     */
    public boolean isWritable(Class<?> type) {
        return Finder.class.isAssignableFrom(type)
                || Query.class.isAssignableFrom(type)
                || ExpressionList.class.isAssignableFrom(type)
                || FutureList.class.isAssignableFrom(type);
    }

    /** {@inheritDoc} */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object o = context.getEntity();
        if (o != null && isWritable(o.getClass())) {

            MultivaluedMap<String, String> queryParams = uriInfoProvider.get().getQueryParameters();
            SpiQuery query = null;
            if (o instanceof Finder) {
                query = (SpiQuery) ((Finder) o).query();
            } else if (o instanceof Query) {
                query = (SpiQuery) o;
            } else if (o instanceof ExpressionList) {
                query = (SpiQuery) ((ExpressionList) o).query();
            }

            if (query != null) {
                FutureRowCount rowCount = applyUriQuery(queryParams, query, locator);
                BeanList list;
                if (o instanceof FutureList) {
                    list = (BeanList) ((FutureList) o).getUnchecked();
                } else {
                    list = (BeanList) query.findList();
                }

                applyRowCountHeader(context.getHeaders(), query, rowCount);

                List result = list.getActualList();

                context.setEntity(result);

                Class clazz = result.getClass();

                context.setType(clazz);

                context.setGenericType(query.getBeanType());
            }
        } else if (o instanceof BeanCollection && !BeanCollection.class.isAssignableFrom(context.getType())) {
            context.setEntity(o.getClass());
        }

        context.proceed();
    }
}
