# Usage de JNDI dans uPortal

JNDI peut être utilisé pour alimenter des données dans de nombreuses parties de la configuration d'uPortal.

- En surcharge de propriétés de Spring
- Pour definir des sources de données pour uPortal
- Pour configurer des filtres CAS

## Usage de JNDI pour surcharger certaines valeurs de propriétés de Spring

### Où est-ce applicable ?

Il est assez facile de surcharger certaines propriétés dans

+ `rdbm.properties`
+ `security.properties`
+ `portal.properties`

Ces fichiers sont chargés dans le contexte Spring via `PortalPropertySourcesPlaceholderConfigurer`. Cette Class charge aussi les valeurs `JNDI` qui se substituent aux valeurs des fichiers de propriétés de configuration.

### Exemple

La propriété `org.apereo.portal.channels.CLogin.CasLoginUrl` est définie dans
`security.properties`. Cette valeur est généralement construite à partir de
valeurs dans le fichier de filtres utilisé pour construire `uportal.war`. Cette valeur habituellement diffère selon les environnements.

#### Definir la valeur

Tout d'abord, nous devons définir la valeur. En utilisant Tomcat, modifions la ligne suivante de `conf/server.xml` dans `<GlobalNamingResources>`:

```xml
<Environment
  name="cas/login"
  value="http://localhost:8903/cas/login?service=..."
  type="java.lang.String" />
```

Là, nous définissons "cas/login" avec une valeur pour l'URL dans Tomcat.

#### Disons à uPortal d'utiliser cette valeur

Nous devons dire à uPortal d'utiliser cette valeur globale. Dans
`uportal-war/src/main/webapp/META-INF/context.xml`, nous ajoutons un resource link  et fournissons notre nommage local dans `<Context>`:

```xml
<ResourceLink
  name="org.apereo.portal.channels.CLogin.CasLoginUrl"
  global="cas/login"
  type="java.lang.String" />
```

Nous utilisons le nommage global court et l'affectons à la plus longue
clé de propriétés pour uPortal.

#### Referencer la ressource depuis `web.xml` d'uPortal

La troisième (et dernière !) modification à faire est d'ajouter la référence dans `uportal-war/src/main/webapp/WEB-INF/web.xml`:

```xml
<resource-env-ref>
  <resource-env-ref-name>org.apereo.portal.channels.CLogin.CasLoginUrl</resource-env-ref-name>
  <resource-env-ref-type>java.lang.String</resource-env-ref-type>
</resource-env-ref>
```

Et voilà!! (... Après tests, Cette dernière étape est optionnelle).

#### Déployer les modifications

ATTENTION : `ant clean` est potenciellement destructeur. Vous devez savoir ce que vous faîtes.

Lancez un `ant clean deploy-war` et redémarrez Tomcat ( pour recupérer l'entrée
JNDI ). La valeur définie dans JNDI doit être utilisée maintenant dans uPortal.

## Définir des sources de données d'uPortal

La définition de sources de données externes à une application a été l'une des premières utilisations de JNDI. uPortal prend en charge les sources de données JNDI. En outre, un Factory bean auxiliaire peut être configuré pour utiliser le premier bean. Cela permet la définition d'un JNDI DataSource bean  avec
 un bean de secours qui est défini avec des valeurs d'un fichier de filtre pour des fins de test.

Exemple depuis datasourceContext.xml:

```xml

    <!--
    <bean id="PortalDb" parent="basePooledDataSource">
        <qualifier value="PortalDb"/>
        <property name="driverClassName" value="${hibernate.connection.driver_class}" />
        <property name="url" value="${hibernate.connection.url}" />
        <property name="username" value="${hibernate.connection.username}" />
        <property name="password" value="${hibernate.connection.password}" />
    </bean>
    -->
    <bean id="PortalDb" class="org.apereo.portal.spring.beans.factory.MediatingFactoryBean">
        <property name="type" value="javax.sql.DataSource" />
        <property name="delegateBeanNames">
            <list>
                <value>PortalDb.JNDI</value>
                <value>PortalDb.direct</value>
            </list>
        </property>
    </bean>

    <bean id="PortalDb.JNDI" class="org.springframework.jndi.JndiObjectFactoryBean">
        <property name="jndiName" value="java:comp/env/jdbc/PortalDb" />
    </bean>

    <bean id="PortalDb.direct" class="org.apache.commons.dbcp.BasicDataSource" lazy-init="true">
        <property name="driverClassName" value="${hibernate.connection.driver_class}" />
        <property name="url" value="${hibernate.connection.url}" />
        <property name="username" value="${hibernate.connection.username}" />
        <property name="password" value="${hibernate.connection.password}" />

        <property name="maxActive" value="50" />
        <property name="maxIdle" value="10" />
        <property name="maxWait" value="1000" />
        <property name="removeAbandoned" value="true" />
        <property name="removeAbandonedTimeout" value="300" />
        <property name="logAbandoned" value="true" />
    </bean>

    <bean id="PortalDB.metadata" class="org.apereo.portal.jdbc.DatabaseMetaDataImpl">
        <constructor-arg index="0" ref="PortalDb" />
        <constructor-arg index="1" ref="transactionManager"/>
    </bean>

```

## Configuration des Filtres CAS

Le Client CAS est JNDI-aware. Les valeurs de JNDI se substituent aux valeurs définis dans `web.xml`.
Encore une fois, les names JNDI sont mappés en global names dans `uportal-war/src/main/webapp/META-INF/context.xml`.
Les names global (et valeurs actuelles) sont définis dans Tomcat.

```xml
    <!-- required names for CAS client -->
    <ResourceLink name="cas/casServerUrlPrefix" global="cas/casServerUrlPrefix" type="java.lang.String" />
    <ResourceLink name="cas/service" global="uportal/service" type="java.lang.String" />
    <!-- Conflicts with cas/service, which is needed for Authentication Filter
    <ResourceLink name="cas/serverName" global="uportal/casServerName" type="java.lang.String" />
    -->
    <ResourceLink name="cas/proxyCallbackUrl" global="shared/url/my" type="java.lang.String" />
    <ResourceLink name="cas/casServerLoginUrl" global="cas/casServerLoginUrl" type="java.lang.String" />
```

 Comme indiqué dans l'exemple, faites attention aux configurations conflictuelles. Chaque filtre CAS lira toutes les valeurs CAS définies.

Source: [uportal-user@ thread](https://groups.google.com/a/apereo.org/d/topic/uportal-user/IM0SnpIlJC0/discussion).
