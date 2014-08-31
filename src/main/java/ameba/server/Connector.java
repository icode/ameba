package ameba.server;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Properties;

/**
 * @author icode
 */
public class Connector {
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
    protected Properties rawProperties;

    protected Connector() {
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public URI getHttpServerBaseUri() {
        return httpServerBaseUri;
    }

    public boolean isSecureEnabled() {
        return secureEnabled;
    }

    public boolean isSslClientMode() {
        return sslClientMode;
    }

    public boolean isSslNeedClientAuth() {
        return sslNeedClientAuth;
    }

    public boolean isSslWantClientAuth() {
        return sslWantClientAuth;
    }

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    public byte[] getSslKeyStoreFile() {
        return sslKeyStoreFile;
    }

    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public String getSslTrustPassword() {
        return sslTrustPassword;
    }

    public byte[] getSslTrustStoreFile() {
        return sslTrustStoreFile;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    public String getSslTrustStoreType() {
        return sslTrustStoreType;
    }

    public boolean isSslConfigReady() {
        return sslConfigReady;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public String getSslKeyStoreProvider() {
        return sslKeyStoreProvider;
    }

    public String getSslTrustStoreProvider() {
        return sslTrustStoreProvider;
    }

    public String getSslKeyManagerFactoryAlgorithm() {
        return sslKeyManagerFactoryAlgorithm;
    }

    public String getSslTrustManagerFactoryAlgorithm() {
        return sslTrustManagerFactoryAlgorithm;
    }

    public boolean isAjpEnabled() {
        return ajpEnabled;
    }

    public String getName() {
        return name;
    }

    public Properties getRawProperties() {
        return rawProperties;
    }

    public static class Builder {
        Connector connector = new Connector();

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

        public Builder rawProperties(Properties properties) {
            connector.rawProperties = properties;
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
            if (connector.secureEnabled && connector.sslKeyStoreFile != null &&
                    StringUtils.isNotBlank(connector.sslKeyPassword) &&
                    StringUtils.isNotBlank(connector.sslKeyStorePassword)) {
                connector.sslConfigReady = true;
            }
            return connector;
        }
    }
}
