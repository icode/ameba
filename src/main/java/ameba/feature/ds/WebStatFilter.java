package ameba.feature.ds;

import com.alibaba.druid.filter.stat.StatFilterContext;
import com.alibaba.druid.filter.stat.StatFilterContextListenerAdapter;
import com.alibaba.druid.support.http.stat.WebAppStat;
import com.alibaba.druid.support.http.stat.WebAppStatManager;
import com.alibaba.druid.support.http.stat.WebRequestStat;
import com.alibaba.druid.support.http.stat.WebURIStat;
import com.alibaba.druid.support.profile.ProfileEntryKey;
import com.alibaba.druid.support.profile.ProfileEntryReqStat;
import com.alibaba.druid.support.profile.Profiler;
import com.alibaba.druid.util.PatternMatcher;
import com.alibaba.druid.util.ServletPathMatcher;
import groovy.lang.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author icode
 * @since 1.6
 */
@PreMatching
@Singleton
class WebStatFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Constant <code>PARAM_NAME_PORFILE_ENABLE="ds.profileEnable"</code>
     */
    public final static String PARAM_NAME_PORFILE_ENABLE = "ds.profileEnable";
    /** Constant <code>PARAM_NAME_SESSION_STAT_MAX_COUNT="ds.sessionStatMaxCount"</code> */
    public final static String PARAM_NAME_SESSION_STAT_MAX_COUNT = "ds.sessionStatMaxCount";
    /** Constant <code>PARAM_NAME_EXCLUSIONS="ds.exclusions"</code> */
    public static final String PARAM_NAME_EXCLUSIONS = "ds.exclusions";
    /** Constant <code>PARAM_NAME_PRINCIPAL_COOKIE_NAME="ds.principalCookieName"</code> */
    public static final String PARAM_NAME_PRINCIPAL_COOKIE_NAME = "ds.principalCookieName";
    /** Constant <code>PARAM_NAME_REAL_IP_HEADER="ds.realIpHeader"</code> */
    public static final String PARAM_NAME_REAL_IP_HEADER = "ds.realIpHeader";
    /** Constant <code>DEFAULT_MAX_STAT_SESSION_COUNT=1000 * 100</code> */
    public final static int DEFAULT_MAX_STAT_SESSION_COUNT = 1000 * 100;
    private static final Logger logger = LoggerFactory.getLogger(WebStatFilter.class);
    /**
     * PatternMatcher used in determining which paths to react to for a given request.
     */
    protected static PatternMatcher pathMatcher = new ServletPathMatcher();
    private static WebAppStat webAppStat = null;
    private static WebStatFilterContextListener statFilterContextListener = new WebStatFilterContextListener();
    private static Set<String> excludesPattern;

    private static int sessionStatMaxCount = DEFAULT_MAX_STAT_SESSION_COUNT;
    private static boolean profileEnable = false;

    private static String contextPath;

    private static String principalCookieName;
    private static String realIpHeader;

    /**
     * <p>Constructor for WebStatFilter.</p>
     *
     * @param configuration a {@link javax.ws.rs.core.Configuration} object.
     */
    @Inject
    public WebStatFilter(Configuration configuration) {
        if (webAppStat != null) {
            return;
        }

        {
            String exclusions = (String) configuration.getProperty(PARAM_NAME_EXCLUSIONS);
            if (exclusions != null && exclusions.trim().length() != 0) {
                excludesPattern = new HashSet<String>(Arrays.asList(exclusions.split("\\s*,\\s*")));
            }
        }

        {
            String param = (String) configuration.getProperty(PARAM_NAME_PRINCIPAL_COOKIE_NAME);
            if (param != null) {
                param = param.trim();
                if (param.length() != 0) {
                    principalCookieName = param;
                }
            }
        }

        {
            String param = (String) configuration.getProperty(PARAM_NAME_PORFILE_ENABLE);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                if ("true".equals(param)) {
                    profileEnable = true;
                } else if ("false".equals(param)) {
                    profileEnable = false;
                } else {
                    logger.error("WebStatFilter Parameter '" + PARAM_NAME_PORFILE_ENABLE + "' config error");
                }
            }
        }
        {
            String param = (String) configuration.getProperty(PARAM_NAME_SESSION_STAT_MAX_COUNT);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                try {
                    sessionStatMaxCount = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    logger.error("WebStatFilter Parameter '" + PARAM_NAME_SESSION_STAT_MAX_COUNT + "' config error", e);
                }
            }
        }

        // realIpHeader
        {
            String param = (String) configuration.getProperty(PARAM_NAME_REAL_IP_HEADER);
            if (param != null) {
                param = param.trim();
                if (param.length() != 0) {
                    realIpHeader = param;
                }
            }
        }

        StatFilterContext.getInstance().addContextListener(statFilterContextListener);

        contextPath = "/";
        if (webAppStat == null) {
            webAppStat = new WebAppStat(contextPath, sessionStatMaxCount);
        }
        WebAppStatManager.getInstance().addWebAppStatSet(webAppStat);
    }

    private WebURIStat getUriStat(String requestURI) {
        WebURIStat uriStat = webAppStat.getURIStat(requestURI, false);

        if (uriStat == null) {
            int index = requestURI.indexOf(";jsessionid=");
            if (index != -1) {
                requestURI = requestURI.substring(0, index);
                uriStat = webAppStat.getURIStat(requestURI, false);
            }
        }

        return uriStat;
    }

    /** {@inheritDoc} */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestURI = getRequestURI(requestContext);
        if (isExclusion(requestURI)) {
            return;
        }
        long startNano = System.nanoTime();
        long startMillis = System.currentTimeMillis();

        WebRequestStat requestStat = new WebRequestStat(startNano, startMillis);
        WebRequestStat.set(requestStat);

        //WebSessionStat sessionStat = getSessionStat(httpRequest);
        webAppStat.beforeInvoke();

        WebURIStat uriStat = getUriStat(requestURI);

        if (isProfileEnable()) {
            Profiler.initLocal();
            Profiler.enter(requestURI, Profiler.PROFILE_TYPE_WEB);
        }

        // 第一次访问时，uriStat这里为null，是为了防止404攻击。
        if (uriStat != null) {
            uriStat.beforeInvoke();
        }

        // 第一次访问时，sessionId为null，如果缺省sessionCreate=false，sessionStat就为null。
//        if (sessionStat != null) {
//            sessionStat.beforeInvoke();
//        }
    }

    /** {@inheritDoc} */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String requestURI = getRequestURI(requestContext);
        if (isExclusion(requestURI) || WebRequestStat.current() == null) {
            return;
        }
        long endNano = System.nanoTime();
        WebRequestStat.current().setEndNano(endNano);

        long nanos = endNano - WebRequestStat.current().getStartNano();

        Throwable error = null;

        if (responseContext.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            if (Throwable.class.isInstance(responseContext.getEntity()))
                error = (Throwable) responseContext.getEntity();
        }

        webAppStat.afterInvoke(error, nanos);
        WebURIStat uriStat = getUriStat(requestURI);

        if (uriStat == null) {
            int status = responseContext.getStatus();
            if (status == Response.Status.NOT_FOUND.getStatusCode()) {
                String errorUrl = contextPath + "error_" + status;
                uriStat = webAppStat.getURIStat(errorUrl, true);
            } else {
                uriStat = webAppStat.getURIStat(requestURI, true);
            }

            if (uriStat != null) {
                uriStat.beforeInvoke(); // 补偿调用
            }
        }

        if (uriStat != null) {
            uriStat.afterInvoke(error, nanos);
        }

        WebRequestStat.set(null);

        if (isProfileEnable()) {
            Profiler.release(nanos);

            Map<ProfileEntryKey, ProfileEntryReqStat> requestStatsMap = Profiler.getStatsMap();
            if (uriStat != null) {
                uriStat.getProfiletat().record(requestStatsMap);
            }
            Profiler.removeLocal();
        }
    }

    /**
     * <p>getRemoteAddress.</p>
     *
     * @param request a {@link javax.ws.rs.container.ContainerRequestContext} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getRemoteAddress(ContainerRequestContext request) {
        String ip = null;
        if (realIpHeader != null && realIpHeader.length() != 0) {
            ip = request.getHeaderString(realIpHeader);
        }
        if (StringUtils.isBlank(ip)) {
            ip = request.getHeaderString("x-forwarded-for");
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeaderString("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeaderString("WL-Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = "unknown";
            }
        }
        return ip;
    }

    //    public void destroy() {
//        StatFilterContext.getInstance().removeContextListener(statFilterContextListener);
//
//        if (webAppStat != null) {
//            WebAppStatManager.getInstance().remove(webAppStat);
//        }
//    }

    /**
     * <p>getPrincipal.</p>
     *
     * @param httpRequest a {@link javax.ws.rs.container.ContainerRequestContext} object.
     * @return a {@link java.lang.String} object.
     */
    public String getPrincipal(ContainerRequestContext httpRequest) {
        if (principalCookieName != null && httpRequest.getCookies().size() > 0) {
            Map<String, Cookie> cookies = httpRequest.getCookies();
            for (Cookie cookie : cookies.values()) {
                if (principalCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * <p>isExclusion.</p>
     *
     * @param requestURI a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isExclusion(String requestURI) {
        if (excludesPattern == null) {
            return false;
        }

        if (contextPath != null && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
            if (!requestURI.startsWith("/")) {
                requestURI = "/" + requestURI;
            }
        }

        for (String pattern : excludesPattern) {
            if (pathMatcher.matches(pattern, requestURI)) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>getRequestURI.</p>
     *
     * @param request a {@link javax.ws.rs.container.ContainerRequestContext} object.
     * @return a {@link java.lang.String} object.
     */
    public String getRequestURI(ContainerRequestContext request) {
        return ((ContainerRequest) request).getPath(true);
    }

    /**
     * <p>Getter for the field <code>principalCookieName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPrincipalCookieName() {
        return principalCookieName;
    }

    /**
     * <p>isProfileEnable.</p>
     *
     * @return a boolean.
     */
    public boolean isProfileEnable() {
        return profileEnable;
    }

    /**
     * <p>Setter for the field <code>profileEnable</code>.</p>
     *
     * @param profileEnable a boolean.
     */
    public void setProfileEnable(boolean profileEnable) {
        WebStatFilter.profileEnable = profileEnable;
    }

    /**
     * <p>Getter for the field <code>webAppStat</code>.</p>
     *
     * @return a {@link com.alibaba.druid.support.http.stat.WebAppStat} object.
     */
    public WebAppStat getWebAppStat() {
        return webAppStat;
    }

    /**
     * <p>Setter for the field <code>webAppStat</code>.</p>
     *
     * @param webAppStat a {@link com.alibaba.druid.support.http.stat.WebAppStat} object.
     */
    public void setWebAppStat(WebAppStat webAppStat) {
        WebStatFilter.webAppStat = webAppStat;
    }

    /**
     * <p>Getter for the field <code>contextPath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * <p>Getter for the field <code>sessionStatMaxCount</code>.</p>
     *
     * @return a int.
     */
    public int getSessionStatMaxCount() {
        return sessionStatMaxCount;
    }

    /**
     * <p>Getter for the field <code>statFilterContextListener</code>.</p>
     *
     * @return a {@link ameba.feature.ds.WebStatFilter.WebStatFilterContextListener} object.
     */
    public WebStatFilterContextListener getStatFilterContextListener() {
        return statFilterContextListener;
    }

    static class WebStatFilterContextListener extends StatFilterContextListenerAdapter {

        @Override
        public void addUpdateCount(int updateCount) {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.addJdbcUpdateCount(updateCount);
            }
        }

        @Override
        public void addFetchRowCount(int fetchRowCount) {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.addJdbcFetchRowCount(fetchRowCount);
            }
        }

        @Override
        public void executeBefore(String sql, boolean inTransaction) {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcExecuteCount();
            }
        }

        @Override
        public void executeAfter(String sql, long nanos, Throwable error) {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.addJdbcExecuteTimeNano(nanos);
                if (error != null) {
                    reqStat.incrementJdbcExecuteErrorCount();
                }
            }
        }

        @Override
        public void commit() {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcCommitCount();
            }
        }

        @Override
        public void rollback() {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcRollbackCount();
            }
        }

        @Override
        public void pool_connect() {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcPoolConnectCount();
            }
        }

        @Override
        public void pool_close(long nanos) {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcPoolCloseCount();
            }
        }

        @Override
        public void resultSet_open() {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcResultSetOpenCount();
            }
        }

        @Override
        public void resultSet_close(long nanos) {
            WebRequestStat reqStat = WebRequestStat.current();
            if (reqStat != null) {
                reqStat.incrementJdbcResultSetCloseCount();
            }
        }
    }
}
