package ameba.container.server;

import ameba.exception.ConfigErrorException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ameba.util.IOUtils.readByteArrayFromResource;

/**
 * <p>Connector class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public class Connector {
    /**
     * Constant <code>CONNECTOR_CONF_PREFIX="connector."</code>
     */
    public static final String CONNECTOR_CONF_PREFIX = "connector.";
    /**
     * Constant <code>DEFAULT_NETWORK_HOST="0.0.0.0"</code>
     */
    public static final String DEFAULT_NETWORK_HOST = "0.0.0.0";
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);
    protected URI httpServerBaseUri;
    protected String host;
    protected String name;
    protected boolean ajpEnabled;
    protected boolean secureEnabled;
    protected Integer port;
    protected String sslProtocol;
    protected boolean sslClientMode;
    protected boolean sslNeedClientAuth;
    protected boolean sslWantClientAuth;
    protected String sslKeyPassword;
    protected byte[] sslKeyStoreFile;
    protected String sslKeyStoreType;
    protected String sslKeyStorePassword;
    protected String sslKeyStoreProvider;
    protected String sslKeyManagerFactoryAlgorithm;
    protected String sslTrustPassword;
    protected byte[] sslTrustStoreFile;
    protected String sslTrustStorePassword;
    protected String sslTrustStoreType;
    protected String sslTrustStoreProvider;
    protected String sslTrustManagerFactoryAlgorithm;
    protected boolean sslConfigReady;
    protected Map<String, String> rawProperties;

    /**
     * <p>Constructor for Connector.</p>
     */
    protected Connector() {
    }

    /**
     * <p>createDefault.</p>
     *
     * @param properties a {@link java.util.Map} object.
     * @return a {@link ameba.container.server.Connector} object.
     */
    public static Connector createDefault(Map<String, String> properties) {
        Connector.Builder builder = Connector.Builder.create()
                .rawProperties(properties)
                .secureEnabled(Boolean.parseBoolean(properties.get("ssl.enabled")))
                .sslProtocol(properties.get("ssl.protocol"))
                .sslClientMode(Boolean.parseBoolean(properties.get("ssl.clientMode")))
                .sslNeedClientAuth(Boolean.parseBoolean(properties.get("ssl.needClientAuth")))
                .sslWantClientAuth(Boolean.parseBoolean(properties.get("ssl.wantClientAuth")))
                .sslKeyManagerFactoryAlgorithm(properties.get("ssl.key.manager.factory.algorithm"))
                .sslKeyPassword(properties.get("ssl.key.password"))
                .sslKeyStoreProvider(properties.get("ssl.key.store.provider"))
                .sslKeyStoreType(properties.get("ssl.key.store.type"))
                .sslKeyStorePassword(properties.get("ssl.key.store.password"))
                .sslTrustManagerFactoryAlgorithm(properties.get("ssl.trust.manager.factory.algorithm"))
                .sslTrustPassword(properties.get("ssl.trust.password"))
                .sslTrustStoreProvider(properties.get("ssl.trust.store.provider"))
                .sslTrustStoreType(properties.get("ssl.trust.store.type"))
                .sslTrustStorePassword(properties.get("ssl.trust.store.password"))
                .ajpEnabled(Boolean.parseBoolean(properties.get("ajp.enabled")))
                .host(StringUtils.defaultIfBlank(properties.get("host"), "0.0.0.0"))
                .port(Integer.valueOf(StringUtils.defaultIfBlank(properties.get("port"), "80")))
                .name(properties.get("name"));

        String keyStoreFile = properties.get("ssl.key.store.file");
        if (StringUtils.isNotBlank(keyStoreFile))
            try {
                builder.sslKeyStoreFile(readByteArrayFromResource(keyStoreFile));
            } catch (IOException e) {
                logger.error("读取sslKeyStoreFile出错", e);
            }

        String trustStoreFile = properties.get("ssl.trust.store.file");
        if (StringUtils.isNotBlank(trustStoreFile))
            try {
                builder.sslTrustStoreFile(readByteArrayFromResource(trustStoreFile));
            } catch (IOException e) {
                logger.error("读取sslTrustStoreFile出错", e);
            }

        return builder.build();
    }

    /**
     * <p>createDefaultConnectors.</p>
     *
     * @param properties a {@link java.util.Map} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Connector> createDefaultConnectors(Map<String, Object> properties) {
        List<Connector> connectors = Lists.newArrayList();
        Map<String, Map<String, String>> propertiesMap = Maps.newLinkedHashMap();
        for (String key : properties.keySet()) {
            if (key.startsWith(Connector.CONNECTOR_CONF_PREFIX)) {
                String oKey = key;
                key = key.substring(Connector.CONNECTOR_CONF_PREFIX.length());
                int index = key.indexOf(".");
                if (index == -1) {
                    throw new ConfigErrorException("connector configure error, format connector.{connectorName}.{property}");
                }
                String name = key.substring(0, index);
                Map<String, String> pr = propertiesMap.get(name);
                if (pr == null) {
                    pr = Maps.newLinkedHashMap();
                    propertiesMap.put(name, pr);
                    pr.put("name", name);
                }
                pr.put(key.substring(index + 1), String.valueOf(properties.get(oKey)));
            }
        }

        connectors.addAll(propertiesMap.values().stream().map(Connector::createDefault).collect(Collectors.toList()));
        return connectors;
    }

    /**
     * <p>Getter for the field <code>host</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHost() {
        return host;
    }

    /**
     * <p>Getter for the field <code>port</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * <p>Getter for the field <code>httpServerBaseUri</code>.</p>
     *
     * @return a {@link java.net.URI} object.
     */
    public URI getHttpServerBaseUri() {
        return httpServerBaseUri;
    }

    /**
     * <p>isSecureEnabled.</p>
     *
     * @return a boolean.
     */
    public boolean isSecureEnabled() {
        return secureEnabled;
    }

    /**
     * <p>isSslClientMode.</p>
     *
     * @return a boolean.
     */
    public boolean isSslClientMode() {
        return sslClientMode;
    }

    /**
     * <p>isSslNeedClientAuth.</p>
     *
     * @return a boolean.
     */
    public boolean isSslNeedClientAuth() {
        return sslNeedClientAuth;
    }

    /**
     * <p>isSslWantClientAuth.</p>
     *
     * @return a boolean.
     */
    public boolean isSslWantClientAuth() {
        return sslWantClientAuth;
    }

    /**
     * <p>Getter for the field <code>sslKeyPassword</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    /**
     * <p>Getter for the field <code>sslKeyStoreFile</code>.</p>
     *
     * @return an array of byte.
     */
    public byte[] getSslKeyStoreFile() {
        return sslKeyStoreFile;
    }

    /**
     * <p>Getter for the field <code>sslKeyStoreType</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    /**
     * <p>Getter for the field <code>sslKeyStorePassword</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    /**
     * <p>Getter for the field <code>sslTrustPassword</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslTrustPassword() {
        return sslTrustPassword;
    }

    /**
     * <p>Getter for the field <code>sslTrustStoreFile</code>.</p>
     *
     * @return an array of byte.
     */
    public byte[] getSslTrustStoreFile() {
        return sslTrustStoreFile;
    }

    /**
     * <p>Getter for the field <code>sslTrustStorePassword</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    /**
     * <p>Getter for the field <code>sslTrustStoreType</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslTrustStoreType() {
        return sslTrustStoreType;
    }

    /**
     * <p>isSslConfigReady.</p>
     *
     * @return a boolean.
     */
    public boolean isSslConfigReady() {
        return sslConfigReady;
    }

    /**
     * <p>Getter for the field <code>sslProtocol</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslProtocol() {
        return sslProtocol;
    }

    /**
     * <p>Getter for the field <code>sslKeyStoreProvider</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslKeyStoreProvider() {
        return sslKeyStoreProvider;
    }

    /**
     * <p>Getter for the field <code>sslTrustStoreProvider</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslTrustStoreProvider() {
        return sslTrustStoreProvider;
    }

    /**
     * <p>Getter for the field <code>sslKeyManagerFactoryAlgorithm</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslKeyManagerFactoryAlgorithm() {
        return sslKeyManagerFactoryAlgorithm;
    }

    /**
     * <p>Getter for the field <code>sslTrustManagerFactoryAlgorithm</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSslTrustManagerFactoryAlgorithm() {
        return sslTrustManagerFactoryAlgorithm;
    }

    /**
     * <p>isAjpEnabled.</p>
     *
     * @return a boolean.
     */
    public boolean isAjpEnabled() {
        return ajpEnabled;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Getter for the field <code>rawProperties</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String> getRawProperties() {
        return rawProperties;
    }

    public static class Builder {
        Connector connector;

        private Builder(Connector connector) {
            this.connector = connector;
        }

        private Builder() {
            this(new Connector());
        }

        public static Builder from(Connector connector) {
            return new Builder(connector);
        }

        public static Builder create() {
            return new Builder();
        }

        public String getHost() {
            return connector.getHost();
        }

        public URI getHttpServerBaseUri() {
            return connector.getHttpServerBaseUri();
        }

        public boolean isSslWantClientAuth() {
            return connector.isSslWantClientAuth();
        }

        public String getSslTrustStoreProvider() {
            return connector.getSslTrustStoreProvider();
        }

        public String getSslKeyStoreProvider() {
            return connector.getSslKeyStoreProvider();
        }

        public String getSslTrustManagerFactoryAlgorithm() {
            return connector.getSslTrustManagerFactoryAlgorithm();
        }

        public String getSslProtocol() {
            return connector.getSslProtocol();
        }

        public boolean isAjpEnabled() {
            return connector.isAjpEnabled();
        }

        public Integer getPort() {
            return connector.getPort();
        }

        public boolean isSslClientMode() {
            return connector.isSslClientMode();
        }

        public byte[] getSslTrustStoreFile() {
            return connector.getSslTrustStoreFile();
        }

        public String getSslTrustStorePassword() {
            return connector.getSslTrustStorePassword();
        }

        public String getSslTrustPassword() {
            return connector.getSslTrustPassword();
        }

        public String getSslKeyStoreType() {
            return connector.getSslKeyStoreType();
        }

        public boolean isSslConfigReady() {
            return connector.isSslConfigReady();
        }

        public String getSslTrustStoreType() {
            return connector.getSslTrustStoreType();
        }

        public byte[] getSslKeyStoreFile() {
            return connector.getSslKeyStoreFile();
        }

        public String getSslKeyStorePassword() {
            return connector.getSslKeyStorePassword();
        }

        public String getSslKeyManagerFactoryAlgorithm() {
            return connector.getSslKeyManagerFactoryAlgorithm();
        }

        public boolean isSslNeedClientAuth() {
            return connector.isSslNeedClientAuth();
        }

        public boolean isSecureEnabled() {
            return connector.isSecureEnabled();
        }

        public String getSslKeyPassword() {
            return connector.getSslKeyPassword();
        }

        public Builder host(String host) {
            this.connector.host = host;
            return this;
        }

        public Builder ajpEnabled(boolean ajpEnabled) {
            connector.ajpEnabled = ajpEnabled;
            return this;
        }

        public Builder secureEnabled(boolean secureEnabled) {
            connector.secureEnabled = secureEnabled;
            return this;
        }

        public Builder port(Integer port) {
            connector.port = port;
            return this;
        }

        public Builder sslProtocol(String sslProtocol) {
            connector.sslProtocol = sslProtocol;
            return this;
        }

        public Builder sslClientMode(boolean sslClientMode) {
            connector.sslClientMode = sslClientMode;
            return this;
        }

        public Builder sslNeedClientAuth(boolean sslNeedClientAuth) {
            connector.sslNeedClientAuth = sslNeedClientAuth;
            return this;
        }

        public Builder sslWantClientAuth(boolean sslWantClientAuth) {
            connector.sslWantClientAuth = sslWantClientAuth;
            return this;
        }

        public Builder sslKeyPassword(String sslKeyPassword) {
            connector.sslKeyPassword = sslKeyPassword;
            return this;
        }

        public Builder sslKeyStoreFile(byte[] sslKeyStoreFile) {
            connector.sslKeyStoreFile = sslKeyStoreFile;
            return this;
        }

        public Builder sslKeyStoreType(String sslKeyStoreType) {
            connector.sslKeyStoreType = sslKeyStoreType;
            return this;
        }

        public Builder sslKeyStorePassword(String sslKeyStorePassword) {
            connector.sslKeyStorePassword = sslKeyStorePassword;
            return this;
        }

        public Builder sslKeyStoreProvider(String sslKeyStoreProvider) {
            connector.sslKeyStoreProvider = sslKeyStoreProvider;
            return this;
        }

        public Builder sslKeyManagerFactoryAlgorithm(String sslKeyManagerFactoryAlgorithm) {
            connector.sslKeyManagerFactoryAlgorithm = sslKeyManagerFactoryAlgorithm;
            return this;
        }

        public Builder sslTrustPassword(String sslTrustPassword) {
            connector.sslTrustPassword = sslTrustPassword;
            return this;
        }

        public void sslTrustStoreFile(byte[] sslTrustStoreFile) {
            connector.sslTrustStoreFile = sslTrustStoreFile;
        }

        public Builder sslTrustStorePassword(String sslTrustStorePassword) {
            connector.sslTrustStorePassword = sslTrustStorePassword;
            return this;
        }

        public Builder sslTrustStoreType(String sslTrustStoreType) {
            connector.sslTrustStoreType = sslTrustStoreType;
            return this;
        }

        public Builder sslTrustStoreProvider(String sslTrustStoreProvider) {
            connector.sslTrustStoreProvider = sslTrustStoreProvider;
            return this;
        }

        public Builder sslTrustManagerFactoryAlgorithm(String sslTrustManagerFactoryAlgorithm) {
            connector.sslTrustManagerFactoryAlgorithm = sslTrustManagerFactoryAlgorithm;
            return this;
        }

        public Builder name(String name) {
            connector.name = name;
            return this;
        }

        public Builder rawProperties(Map<String, String> properties) {
            connector.rawProperties = ImmutableMap.copyOf(properties);
            return this;
        }

        public Connector build() {
            if (connector.httpServerBaseUri == null) {
                //config server base uri
                connector.httpServerBaseUri = URI.create(
                        "http" + (isSecureEnabled() ? "s" : "") + "://"
                                + connector.getHost()
                                + ":" + connector.port + "/");
            }
            if (connector.secureEnabled && connector.sslKeyStoreFile != null) {
                connector.sslConfigReady = true;
            }
            return connector;
        }
    }
}
