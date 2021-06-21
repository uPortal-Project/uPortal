# Attributs Utilisateur

Cette section décrit comment configurer vos propres sources d'attributs utilisateur et les intégrer dans uPortal.

## Première connexion des utilisateurs

:warning: Les trois attributs suivants sont couramment utilisés dans uPortal et dans les portlets Apereo:

 * `uid`
 * `username`
 * `user.login.id`

La configuration par défaut (démarrage) fournira ces attributs pour tous les utilisateurs via le bean `uPortalJdbcUserSource`.
Ce bean, cependant, **ne reconnaît que les utilisateurs qui se sont connectés au
portail au moins une fois** ou qui ont été importés via Import / Export. Il est très important de mapper ces mêmes attributs à une source externe, telle que LDAP ou SAML.

## Configuration des sources de données d'attributs utilisateur

La classe de base pour un utilisateur uPotal est une implémentation de l'interface `IPerson`. Le _uPortal Person Directory Service_ est utilisé pour remplir et récupérer les attributs de l'utilisateur. Person Directory est géré comme un [projet indépendant avec son propre code source appelé PersonDirectory][]. Les attributs peuvent être acquis à partir de sources multiples via LDAP, JDBC ou d'autres sources selon les besoins.

Le sous-système Person Directory est basé sur des implémentations concrètes de l'interface `IPersonAttributeDao`. Ces objets sont des beans gérés par Spring. uPortal 5 est préconfiguré avec plusieurs instances de `IPersonAttributeDao`, mais les sources d'informations les plus intéressantes (et les plus importantes!) des attributs utilisateur sont celles que vous fournissez.

Ajoutez vos sources d'attributs utilisateur à uPortal en configurant des beans implémentant `IPersonAttributeDao` et en les ajoutant au contexte d'application Spring. uPortal trouvera les beans que vous déclarez et les ajoutera au sous-système User Attributes de manière appropriée.


Il existe plusieurs façons d'ajouter des beans au contexte d'application uPortal en utilisant [uPortal-start][]. L'un des moyens les plus courants consiste à les déclarer dans un fichier de configuration Spring XML au sein du dossier
`overlays/uPortal/src/main/resources/properties/contextOverrides/'.

### Exemple de Bean Definition `IPersonAttributeDao`

```xml
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

    <bean id="layoutNodesCountPersonAttributeDao" class="org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao">
        <constructor-arg index="0" ref="PersonDB" />
        <constructor-arg>
            <value>
                select count(*) as layoutNodesCount
                    from up_layout_struct
                    where user_id = (select user_id
                        from up_user
                        where {0})
            </value>
        </constructor-arg>
        <property name="usernameAttributeProvider" ref="usernameAttributeProvider" />
        <property name="queryAttributeMapping">
            <map>
                <entry key="username" value="USER_NAME" />
            </map>
        </property>
        <property name="resultAttributeMapping">
            <map>
                <entry key="layoutNodesCount">
                    <set>
                        <value>layoutNodesCount</value>
                    </set>
                </entry>
            </map>
        </property>
    </bean>

</beans>
```

## Sources de données des attributs utilisateur

 * [Source de données LDAP des attributs utilisateur](ldap.md)
 * [Source de données JDBC des attributs utilisateur](jdbc.md)

[projet indépendant avec son propre code source appelé PersonDirectory]: https://github.com/apereo/person-directory
[uPortal-start]: https://github.com/uPortal-project/uPortal-start
