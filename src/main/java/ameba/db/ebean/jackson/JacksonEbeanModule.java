package ameba.db.ebean.jackson;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Jackson module that uses Ebean's JsonContext for serializing and deserializing entity beans.
 */
public class JacksonEbeanModule extends SimpleModule {

    private final JsonContext jsonContext;
    private final EbeanServer server;

    private final ServiceLocator locator;

    /**
     * Construct with a JsonContext obtained from an EbeanServer.
     */
    public JacksonEbeanModule(EbeanServer server, ServiceLocator locator) {
        this.server = server;
        this.jsonContext = server.json();
        this.locator = locator;
    }

    @Override
    public String getModuleName() {
        return "jackson-datatype-ebean-server-" + server.getName();
    }

    /**
     * Register the Ebean specific serialisers and deserialisers.
     */
    @Override
    public void setupModule(SetupContext context) {
        FindSerializers serializers = new FindSerializers(jsonContext);
        locator.postConstruct(serializers);
        locator.inject(serializers);
        context.addSerializers(serializers);

        FindDeserializers deserializers = new FindDeserializers(jsonContext);
        locator.postConstruct(deserializers);
        locator.inject(deserializers);
        context.addDeserializers(deserializers);
    }

}
