# Source de données LDAP des attributs utilisateur

uPortal est capable de récupérer des attributs utilisateur à partir d'une source LDAP. Pour configurer ce comportement, vous devez procéder comme suit.

## Étape 1: Configurer les paramètres de LDAP

Définissez vos paramètres de connexion LDAP dans `uPortal.properties` ou `global.properties`.

```properties
# Paramètres de connexion au serveur LDAP (facultatif)
# REMARQUE: ldap.userName doit être un DN complet
ldap.url=
ldap.baseDn=
ldap.userName=
ldap.password=
```

## Étape 2: Ajouter une source d'attribut LDAP

Vous pouvez définir un nouveau bean qui implémente `IPersonAttributeDao` dans un fichier de configuration Spring XML dans le dossier `overlays/uPortal/src/main/resources/properties/contextOverrides/'.

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
