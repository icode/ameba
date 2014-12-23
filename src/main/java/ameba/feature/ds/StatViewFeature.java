package ameba.feature.ds;

import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.druid.util.Utils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * @author ICode
 * @since 13-8-14 下午7:49
 */
public class StatViewFeature implements Feature {
    public static final String PARAM_NAME_RESET_ENABLE = "ds.resetEnable";
    public static final String PARAM_NAME_USERNAME = "ds.loginUsername";
    public static final String PARAM_NAME_PASSWORD = "ds.loginPassword";
    public static final String SESSION_USER_KEY = "SVST";//stat view session token
    public static final String PARAM_NAME_JMX_URL = "ds.jmxUrl";
    public static final String PARAM_NAME_JMX_USERNAME = "ds.jmxUsername";
    public static final String PARAM_NAME_JMX_PASSWORD = "ds.jmxPassword";
    private static final Logger logger = LoggerFactory.getLogger(StatViewFeature.class);
    private final static String RESOURCE_PATH = "support/http/resources";
    private static String username = null;
    private static String password = "";
    private static String authorizeToken = null;
    /**
     * 配置的jmx的连接地址
     */
    private static String jmxUrl = null;
    /**
     * 配置的jmx的用户名
     */
    private static String jmxUsername = null;
    /**
     * 配置的jmx的密码
     */
    private static String jmxPassword = null;
    private static MBeanServerConnection conn = null;
    private static DruidStatService statService = DruidStatService.getInstance();
    private static String dsPath = "/__ds";

    private static void init(Configuration configuration) {
        initAuthEnv(configuration);

        try {
            String param = (String) configuration.getProperty(PARAM_NAME_RESET_ENABLE);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                boolean resetEnable = Boolean.parseBoolean(param);
                statService.setResetEnable(resetEnable);
            }
        } catch (Exception e) {
            String msg = "initParameter config error, resetEnable : " + configuration.getProperty(PARAM_NAME_RESET_ENABLE);
            logger.error(msg, e);
        }

        // 获取jmx的连接配置信息
        String param = readInitParam(configuration, PARAM_NAME_JMX_URL);
        if (param != null) {
            jmxUrl = param;
            jmxUsername = readInitParam(configuration, PARAM_NAME_JMX_USERNAME);
            jmxPassword = readInitParam(configuration, PARAM_NAME_JMX_PASSWORD);
            try {
                initJmxConn();
            } catch (IOException e) {
                logger.error("init jmx connection error", e);
            }
        }
    }

    /**
     * 初始化jmx连接
     *
     * @throws IOException
     */
    private static void initJmxConn() throws IOException {
        if (jmxUrl != null) {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            Map<String, String[]> env = null;
            if (jmxUsername != null) {
                env = Maps.newHashMap();
                String[] credentials = new String[]{jmxUsername, jmxPassword};
                env.put(JMXConnector.CREDENTIALS, credentials);
            }
            JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
            conn = jmxc.getMBeanServerConnection();
        }
    }

    private static void initAuthEnv(Configuration configuration) {
        String paramUserName = (String) configuration.getProperty(PARAM_NAME_USERNAME);
        if (StringUtils.isNotBlank(paramUserName)) {
            username = paramUserName;
        }

        String paramPassword = (String) configuration.getProperty(PARAM_NAME_PASSWORD);
        if (StringUtils.isNotBlank(paramPassword)) {
            password = paramPassword;
        }
    }

    /**
     * 读取配置参数.
     *
     * @param key 配置参数名
     * @return 配置参数值，如果不存在当前配置参数，或者为配置参数长度为0，将返回null
     */
    private static String readInitParam(Configuration configuration, String key) {
        String value = null;
        try {
            String param = (String) configuration.getProperty(key);
            if (param != null) {
                param = param.trim();
                if (param.length() > 0) {
                    value = param;
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config [" + key + "] error";
            logger.warn(msg, e);
        }
        return value;
    }

    /**
     * 根据指定的url来获取jmx服务返回的内容.
     *
     * @param connetion jmx连接
     * @param url       url内容
     * @return the jmx返回的内容
     * @throws Exception the exception
     */
    private static String getJmxResult(MBeanServerConnection connetion, String url) throws Exception {
        ObjectName name = new ObjectName(DruidStatService.MBEAN_NAME);

        return (String) conn.invoke(name, "service", new String[]{url},
                new String[]{String.class.getName()});
    }

    /**
     * 程序首先判断是否存在jmx连接地址，如果不存在，则直接调用本地的duird服务； 如果存在，则调用远程jmx服务。在进行jmx通信，首先判断一下jmx连接是否已经建立成功，如果已经
     * 建立成功，则直接进行通信，如果之前没有成功建立，则会尝试重新建立一遍。.
     *
     * @param url 要连接的服务地址
     * @return 调用服务后返回的json字符串
     */
    private static String genServiceResponse(String url) {
        String resp = null;
        if (jmxUrl == null) {
            resp = statService.service(url);
        } else {
            if (conn == null) {// 连接在初始化时创建失败
                try {// 尝试重新连接
                    initJmxConn();
                } catch (IOException e) {
                    logger.error("init jmx connection error", e);
                    resp = DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_ERROR,
                            "init jmx connection error" + e.getMessage());
                }
                if (conn != null) {// 连接成功
                    try {
                        resp = getJmxResult(conn, url);
                    } catch (Exception e) {
                        logger.error("get jmx data error", e);
                        resp = DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_ERROR, "get data error:"
                                + e.getMessage());
                    }
                }
            } else {// 连接成功
                try {
                    resp = getJmxResult(conn, url);
                } catch (Exception e) {
                    logger.error("get jmx data error", e);
                    resp = DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_ERROR,
                            "get data error" + e.getMessage());
                }
            }
        }
        return resp;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        Configuration configuration = context.getConfiguration();

        init(configuration);

        String path = (String) configuration.getProperty("ds.resource.path");

        if (StringUtils.isNotBlank(path)) {
            dsPath = path.startsWith("/") ? path : "/" + path;
        }

        context.register(WebStatFilter.class);

        if (StringUtils.isNotBlank(username)) {
            authorizeToken = UUID.randomUUID().toString().toUpperCase();

            context.register(AuthorizationRequestFilter.class);
        }

        context.register(new ModelProcessor() {
            @Override
            public ResourceModel processResourceModel(ResourceModel resourceModel, final Configuration configuration) {
                ResourceModel.Builder resourceModelBuilder = new ResourceModel.Builder(resourceModel, false);
                Resource.Builder resourceBuilder = Resource.builder(DsStatViewResource.class);
                resourceBuilder.path(dsPath);
                Resource resource = resourceBuilder.build();
                resourceModelBuilder.addResource(resource);
                return resourceModelBuilder.build();
            }

            @Override
            public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
                return subResourceModel;
            }
        });

        return true;
    }

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    static @interface DsAuthorization {
    }

    @DsAuthorization
    static class AuthorizationRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext)
                throws IOException {
            String path = "/" + requestContext.getUriInfo().getPath();
            Cookie cookie = requestContext.getCookies().get(SESSION_USER_KEY);
            if ((cookie == null || !authorizeToken.equals(cookie.getValue())) &&
                    !((dsPath + "/login.html").equals(path)
                            || (dsPath + "/submitLogin").equals(path)
                            || path.startsWith(dsPath + "/css")
                            || path.startsWith(dsPath + "/js")
                            || path.startsWith(dsPath + "/img"))) {
                requestContext.abortWith(Response
                        .temporaryRedirect(URI.create(dsPath + "/login.html"))
                        .build());
            }
        }
    }

    @Path("ds")
    @Singleton
    @DsAuthorization
    public static class DsStatViewResource {

        @Inject
        UriInfo uriInfo;

        @POST
        @Path("submitLogin")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response login(@FormParam("loginUsername") String uname, @FormParam("loginPassword") String pwd) {
            if (username.equals(uname) && password.equals(pwd)) {
                return Response.ok("success").cookie(new NewCookie(SESSION_USER_KEY, authorizeToken)).build();
            } else {
                return Response.ok("error").build();
            }
        }

        @GET
        public Response index() throws IOException {
            return Response.temporaryRedirect(URI.create(dsPath + "/index.html")).build();
        }

        @GET
        @Path("{path:.*}")
        public Response getService(@PathParam("path") String path) throws IOException {
            if ("".equals(path)) {
                return Response.temporaryRedirect(URI.create(dsPath + "/index.html")).build();
            } else {
                if (!path.startsWith("/"))
                    path = "/" + path;
            }

            if (!path.contains(".")) {
                return Response.ok(genServiceResponse(path + getQueryString())).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

            // find file in resources path
            return returnResourceFile(path);
        }

        private String getQueryString() {
            String query = uriInfo.getRequestUri().getRawQuery();
            return (uriInfo.getPath().contains(".json") ? "" : ".json") + (StringUtils.isNotBlank(query) ? "?" + query : "");
        }

        @POST
        @Path("{path:.*}")
        public Response postService(@PathParam("path") String path) throws IOException {
            if (!path.startsWith("/"))
                path = "/" + path;
            if (!path.contains(".")) {
                return Response.ok(genServiceResponse(path + getQueryString())).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
            return returnResourceFile(path);
        }

        private Response returnResourceFile(String fileName) throws IOException {
            Response.ResponseBuilder builder = null;
            if (fileName.endsWith(".jpg")) {
                byte[] bytes = Utils.readByteArrayFromResource(RESOURCE_PATH + fileName);
                builder = Response.ok(bytes);
                return builder.build();
            }

            String text = Utils.readFromResource(RESOURCE_PATH + fileName);
            builder = Response.ok(text);
            if (fileName.endsWith(".css")) {
                builder.type("text/css;charset=utf-8");
            } else if (fileName.endsWith(".js")) {
                builder.type("text/javascript;charset=utf-8");
            }
            return builder.build();
        }
    }
}
