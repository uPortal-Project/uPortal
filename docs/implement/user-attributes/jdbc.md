# JDBC User Attribute Sources

The following configuration shows the JDBC-based user attribute source beans for handling user information in a caching mechanism.

```xml
<!-- CachingPersonAttributeDaoImpl Bean Definition -->
<bean id="cachingMergedPersonAttributeDao" class="org.jasig.services.persondir.support.CachingPersonAttributeDaoImpl">
    <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
    <property name="cacheNullResults" value="true" />
    <property name="userInfoCache">
        <bean class="org.jasig.portal.utils.cache.MapCacheFactoryBean">
            <property name="cacheFactory" ref="cacheFactory" />
            <property name="cacheName" value="org.jasig.services.persondir.USER_INFO.merged" />
        </bean>
    </property>
    <property name="cacheKeyGenerator" ref="userAttributeCacheKeyGenerator" />
    <property name="cachedPersonAttributesDao">
        <bean class="org.jasig.services.persondir.support.MergingPersonAttributeDaoImpl">
            <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
            <property name="merger">
                <bean class="org.jasig.services.persondir.support.merger.ReplacingAttributeAdder" />
            </property>
            <property name="personAttributeDaos">
                <list>
                    <ref bean="cachinguPortalJdbcAttributeSource"/>
                    <ref bean="cachinguPortalJdbcUserSource"/>
                    <ref bean="myunivCachingPersonDbJdbcAttributeSource"/>
                </list>
            </property>
        </bean>
    </property>
</bean>
<!-- myunivCachingPersonDbJdbcAttributeSource Bean Definition as well -->
<bean id="myunivCachingPersonDbJdbcAttributeSource">
    <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
    <property name="cacheNullResults" value="true" />
    <property name="userInfoCache">
        <bean>
            <property name="cacheFactory" ref="cacheFactory" />
            <property name="cacheName" value="org.jasig.services.persondir.USER_INFO.myuniv_person_dir" />
        </bean>
    </property>
    <property name="cacheKeyGenerator" ref="userAttributeCacheKeyGenerator" />
    <property name="cachedPersonAttributesDao">
        <bean class="org.jasig.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao">
            <constructor-arg index="0" ref="PersonDB" />
            <constructor-arg>
                <value>
                    SELECT DISTINCT first_name||' '||Last_name first_last, role,
                    contentGroup, person_type, first_name, last_name, middle_name,
                    email_alias, college, class_year, student_status, alias,
                    work_phone, netid
                    FROM person_directory_view where {0}
                </value>
            </constructor-arg>
            <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
            <!-- Map the table fields to P3P attribute names -->
            <property name="queryAttributeMapping">
                <map>
                    <entry key="displayName" value="FIRST_LAST" />
                    <entry key="sn" value="LAST_NAME" />
                    <entry key="contentGroup" value="CONTENT_GROUP" />
                    <entry key="mail" value="EMAIL_ALIAS" />
                    <entry key="givenName" value="FIRST_NAME" />
                    <entry key="alias" value="ALIAS" />
                    <entry key="personType" value="PERSON_TYPE" />
                    <entry key="College" value="COLLEGE" />
                    <entry key="ClassYear" value="CLASS_YEAR" />
                    <entry key="StudentStatus" value="STUDENT_STATUS" />
                    <entry key="username" value="NETID" />
                    <entry key="WorkPhone" value="WORK_PHONE" />
                </map>
            </property>
            <!-- Map the table fields to P3P attribute names -->
            <property name="resultAttributeMapping">
                <map>
                    <entry key="FIRST_LAST">
                        <value>displayName</value>
                    </entry>
                    <entry key="LAST_NAME">
                        <set>
                            <value>sn</value>
                            <value>user.name.family</value>
                        </set>
                    </entry>
                    <entry key="ROLE">
                        <value>uPortalAffiliation</value>
                    </entry>
                    <entry key="CONTENTGROUP">
                        <set>
                            <value>contentGroup</value>
                        </set>
                    </entry>
                    <entry key="EMAIL_ALIAS">
                        <set>
                            <value>mail</value>
                            <value>user.home-info.online.email</value>
                        </set>
                    </entry>
                    <entry key="FIRST_NAME">
                        <value>givenName</value>
                    </entry>
                    <entry key="ALIAS">
                        <value>alias</value>
                    </entry>
                    <entry key="PERSON_TYPE">
                        <value>personType</value>
                    </entry>
                    <entry key="COLLEGE">
                        <value>College</value>
                    </entry>
                    <entry key="CLASS_YEAR">
                        <value>ClassYear</value>
                    </entry>
                    <entry key="STUDENT_STATUS">
                        <value>StudentStatus</value>
                    </entry>
                    <entry key="NETID">
                        <set>
                            <value>uid</value>
                            <value>netid</value>
                            <value>username</value>
                            <value>eduPersonPrincipalName</value>
                            <value>user.login.id</value>
                        </set>
                    </entry>
                    <entry key="WORK_PHONE">
                        <value>WorkPhone</value>
                    </entry>
                </map>
            </property>
        </bean>
    </property>
</bean>
