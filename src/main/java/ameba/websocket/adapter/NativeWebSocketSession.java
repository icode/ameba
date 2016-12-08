/*
 * Copyright 2002-2013 the original author or authors.
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

package ameba.websocket.adapter;

import ameba.websocket.WebSocketSession;

/**
 * A {@link ameba.websocket.WebSocketSession} that exposes the underlying, native WebSocketSession
 * through a getter.
 *
 * @author Rossen Stoyanchev
 * @author icode
 *
 */
public interface NativeWebSocketSession extends WebSocketSession {

    /**
     * Return the underlying native WebSocketSession, if available.
     *
     * @return the native session or {@code null}
     */
    Object getNativeSession();

    /**
     * Return the underlying native WebSocketSession, if available.
     *
     * @param requiredType the required type of the session
     * @return the native session of the required type or {@code null}
     * @param <T> a T object.
     */
    <T> T getNativeSession(Class<T> requiredType);

}
