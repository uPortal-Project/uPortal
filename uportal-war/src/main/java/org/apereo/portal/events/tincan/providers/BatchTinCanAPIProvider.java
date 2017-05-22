/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.tincan.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apereo.portal.events.tincan.om.LrsStatement;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Batch up a set of statements and send them in a single request. This probably won't have a huge
 * impact on the portal performance, but may reduce some of the overhead to the LRS.
 *
 * <p>IMPORTANT: If you configure a BatchTinCanAPIProvider you MUST also add a scheduler that calls
 * the sendBatch() method regularly. An example configuration is available in tincanAPIContext.xml.
 *
 */
public class BatchTinCanAPIProvider extends DefaultTinCanAPIProvider {
    private final Queue<LrsStatement> statementQueue = new ConcurrentLinkedQueue<LrsStatement>();

    @Override
    public boolean sendEvent(LrsStatement statement) {
        if (!isEnabled()) {
            return false;
        }

        statementQueue.add(statement);
        return true;
    }

    /**
     * Send a batch of LRS statements. MUST BE SCHEDULED! Failure to properly configure this class
     * will result in memory leaks.
     */
    public void sendBatch() {
        LrsStatement statement = null;
        List<LrsStatement> list = new ArrayList<LrsStatement>();

        while ((statement = statementQueue.poll()) != null) {
            list.add(statement);
        }

        if (!list.isEmpty()) {
            postStatementList(list);
        }
    }

    /**
     * Send the list of batched LRS statements to the LRS.
     *
     * @param list the list of statements.
     */
    private void postStatementList(List<LrsStatement> list) {
        try {
            ResponseEntity<Object> response =
                    sendRequest(
                            STATEMENTS_REST_ENDPOINT, HttpMethod.POST, null, list, Object.class);
            if (response.getStatusCode().series() == Series.SUCCESSFUL) {
                logger.trace(
                        "LRS provider successfully sent to {}, statement list: {}",
                        getLRSUrl(),
                        list);
                logger.trace("Sent batch statement.  RESULTS: " + response.getBody().toString());
            } else {
                logger.error(
                        "LRS provider failed to send to {}, statement list: {}", getLRSUrl(), list);
                logger.error("- Response: {}", response);
            }
            // todo: Need to think through a strategy for handling errors submitting
            // to the LRS.
        } catch (HttpClientErrorException e) {
            // log some additional info in this case...
            logger.error(
                    "LRS provider for URL " + getLRSUrl() + " failed to send statement list", e);
            logger.error(
                    "- Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());

        } catch (Exception e) {
            logger.error(
                    "LRS provider for URL " + getLRSUrl() + " failed to send statement list", e);
        }
    }
}
