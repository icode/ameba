/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ameba.websocket.sockjs.frame;

import ameba.util.Assert;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Jackson 2.6+ codec for encoding and decoding SockJS messages.
 * <p>It customizes Jackson's default properties with the following ones:</p>
 * <ul>
 * <li>{@link com.fasterxml.jackson.databind.MapperFeature#DEFAULT_VIEW_INCLUSION} is disabled</li>
 * <li>{@link com.fasterxml.jackson.databind.DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} is disabled</li>
 * </ul>
 * <p>Note that Jackson's JSR-310 and Joda-Time support modules will be registered automatically</p>
 * when available (and when Java 8 and Joda-Time themselves are available, respectively).
 *
 * @author Rossen Stoyanchev
 * @author icode
 */
public class Jackson2SockJsMessageCodec extends AbstractSockJsMessageCodec {

    private final ObjectMapper objectMapper;

    /**
     * <p>Constructor for Jackson2SockJsMessageCodec.</p>
     *
     * @param objectMapper a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     */
    @Inject
    public Jackson2SockJsMessageCodec(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        this.objectMapper = objectMapper;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String[] decode(String content) throws IOException {
        return this.objectMapper.readValue(content, String[].class);
    }

    /** {@inheritDoc} */
    @Override
    public String[] decodeInputStream(InputStream content) throws IOException {
        return this.objectMapper.readValue(content, String[].class);
    }

    /** {@inheritDoc} */
    @Override
    protected char[] applyJsonQuoting(String content) {
        return JsonStringEncoder.getInstance().quoteAsString(content);
    }

}
