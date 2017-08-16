# LDAP User Attribute Sources

uPortal is capable of retrieving user attributes from an LDAP source. To configure this behavior, you'll need to do the following.

## Step 1:  Configure Ldap Settings

Define your LDAP connection settings in `uPortal.properties` or `global.properties`.

```properties
# LDAP server connection settings (optional)
# NOTE:  ldap.userName must be a fully-qualified DN
ldap.url=
ldap.baseDn=
ldap.userName=
ldap.password=
```

## Step 2:  Add an LDAP Attribute source

You can define a new bean that implements `IPersonAttributeDao` in a Spring XML configuration file
within the `overlays/uPortal/src/main/resources/properties/contextOverrides/' directory.

```xml
<bean id="uPortalLdapAttributeSource" class="org.apereo.services.persondir.support.ldap.LdapPersonAttributeDao">
    <property name="contextSource" ref="defaultLdapContext" />
 
    <!--
     | Enter all keys that you want users to search with in the Directory Search portlet or portal
     | search capability. This should contain values from list directoryQueryAttributes in
     | properties/contexts/userContext.xml.  (Key is internal name, value is ldap attribute name.)
    -->
    <property name="queryAttributeMapping">
        <map>
            <entry key="username" value="uid" /><!-- should match the uid attribute in your directory;  e.g. sAMAccountName for Active Directory. -->
            <entry key="cn"  value="cn" />
            <entry key="givenName" value="givenName"/>
            <entry key="sn" value="sn" />
            <entry key="mail" value="mail" />
        </map>
    </property>
    <property name="queryType" value="OR"/>
    <!-- key is ldap attribute name, values are internal names. -->
    <property name="resultAttributeMapping">
        <map>
            <entry key="uid">
                <set>
                    <value>uid</value>
                    <value>username</value> <!-- UP-4185 populate username in case user hasn't logged in yet -->
                    <value>user.login.id</value>  <!-- UP-4177 LDAP needs to fill in user.login.id -->
                </set>
            </entry>
            <entry key="eduPersonAffiliation">
                <value>eduPersonAffiliation</value>
            </entry>
            <entry key="eduPersonPrimaryAffiliation">
                <value>eduPersonPrimaryAffiliation</value>
            </entry>
            <entry key="eduPersonNickname">
                <set>
                    <value>eduPersonNickname</value>
                    <value>user.name.nickName</value>
                </set>
            </entry>
            <entry key="eduPersonOrgDN">
                <set>
                    <value>eduPersonOrgDN</value>
                    <value>user.employer</value>
                </set>
            </entry>
            <entry key="eduPersonOrgUnitDN">
                <set>
                    <value>eduPersonOrgUnitDN</value>
                    <value>user.department</value>
                </set>
            </entry>
            <entry key="eduPersonPrincipalName">
                <value>eduPersonPrincipalName</value>
            </entry>
            <entry key="c">
                <value>c</value>
            </entry>
            <entry key="cn">
                <value>cn</value>
            </entry>
            <entry key="description">
                <value>description</value>
            </entry>
            <entry key="displayName">
                <value>displayName</value>
            </entry>
            <entry key="givenName">
                <set>
                    <value>givenName</value>
                    <value>user.name.given</value>
                </set>
            </entry>
            <entry key="homePhone">
                <value>homePhone</value>
            </entry>
            <entry key="jpegPhoto">
                <value>jpegPhoto</value>
            </entry>
            <entry key="l">
                <value>l</value>
            </entry>
            <entry key="mail">
                <set>
                    <value>mail</value>
                    <value>user.home-info.online.email</value>
                </set>
            </entry>
            <entry key="o">
                <value>o</value>
            </entry>
            <entry key="ou">
                <value>ou</value>
            </entry>
            <entry key="postalAddress">
                <value>postalAddress</value>
            </entry>
            <entry key="postalCode">
                <value>postalCode</value>
            </entry>
            <entry key="sn">
                <set>
                    <value>sn</value>
                    <value>user.name.family</value>
                </set>
            </entry>
            <entry key="street">
                <value>street</value>
            </entry>
            <entry key="telephoneNumber">
                <value>telephoneNumber</value>
            </entry>
        </map>
    </property>
</bean>
```
