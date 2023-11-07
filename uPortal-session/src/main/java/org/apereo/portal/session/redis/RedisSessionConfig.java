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
package org.apereo.portal.session.redis;

import static org.apereo.portal.session.PortalSessionConstants.REDIS_CLUSTER_MODE;
import static org.apereo.portal.session.PortalSessionConstants.REDIS_SENTINEL_MODE;
import static org.apereo.portal.session.PortalSessionConstants.REDIS_STANDALONE_MODE;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@Conditional(SpringSessionRedisEnabledCondition.class)
@EnableRedisHttpSession
public class RedisSessionConfig {

    @Value("${org.apereo.portal.session.redis.mode:standalone}")
    private String redisMode;

    @Value("${org.apereo.portal.session.redis.host:#{null}}")
    private String redisHost;

    @Value("${org.apereo.portal.session.redis.port:#{null}}")
    private Integer redisPort;

    @Value("${org.apereo.portal.session.redis.cluster.nodes:#{null}}")
    private String redisClusterNodes;

    @Value("${org.apereo.portal.session.redis.cluster.maxredirects:#{null}}")
    private Integer redisClusterMaxRedirects;

    @Value("${org.apereo.portal.session.redis.sentinel.master:#{null}}")
    private String redisSentinelMaster;

    @Value("${org.apereo.portal.session.redis.sentinel.nodes:#{null}}")
    private String redisSentinelNodes;

    @Value("${org.apereo.portal.session.redis.database:#{null}}")
    private Integer redisDatabase;

    @Value("${org.apereo.portal.session.redis.password:#{null}}")
    private String redisPassword;

    @Value("${org.apereo.portal.session.redis.timeout:#{null}}")
    private Integer redisTimeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory result;
        if (REDIS_CLUSTER_MODE.equalsIgnoreCase(this.redisMode)) {
            result = this.createBaseClusterConnectionFactory();
        } else if (REDIS_SENTINEL_MODE.equalsIgnoreCase(this.redisMode)) {
            result = this.createBaseSentinelConnectionFactory();
        } else if (REDIS_STANDALONE_MODE.equalsIgnoreCase(this.redisMode)) {
            result = this.createBaseStandaloneConnectionFactory();
        } else {
            throw new IllegalArgumentException(
                    "Invalid value for org.apereo.portal.session.redis.mode: " + this.redisMode);
        }
        this.setAdditionalProperties(result);
        return result;
    }

    private JedisConnectionFactory createBaseClusterConnectionFactory() {
        List<String> nodesList = Arrays.asList(this.redisClusterNodes.split(",", -1));
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodesList);
        if (this.redisClusterMaxRedirects != null) {
            clusterConfig.setMaxRedirects(this.redisClusterMaxRedirects);
        }
        return new JedisConnectionFactory(clusterConfig);
    }

    private JedisConnectionFactory createBaseSentinelConnectionFactory() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.master(this.redisSentinelMaster);
        String[] nodesArray = this.redisSentinelNodes.split(",", -1);
        for (String node : nodesArray) {
            String[] hostAndPort = node.split(":", -1);
            sentinelConfig.sentinel(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
        }
        return new JedisConnectionFactory(sentinelConfig);
    }

    private JedisConnectionFactory createBaseStandaloneConnectionFactory() {
        final JedisConnectionFactory result = new JedisConnectionFactory();
        if (this.redisHost != null) {
            result.setHostName(this.redisHost);
        }
        if (this.redisPort != null) {
            result.setPort(this.redisPort);
        }
        return result;
    }

    private void setAdditionalProperties(JedisConnectionFactory factory) {
        if (this.redisDatabase != null) {
            factory.setDatabase(this.redisDatabase);
        }
        if (this.redisPassword != null) {
            factory.setPassword(this.redisPassword);
        }
        if (this.redisTimeout != null) {
            factory.setTimeout(this.redisTimeout);
        }
    }
}
