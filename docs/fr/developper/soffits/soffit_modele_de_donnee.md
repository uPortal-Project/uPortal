# Modèle de donnée d'une Soffit

Pour être d'une quelconque utilité, une vraie Soffit doit aller plus loin qu'un simple _Hello World!_.
Une Soffit embarque avec elle un modèle de données riche pour échanger des données entre le portail et votre application.
Il y a (actuellement) cinq objets dans ce modèle de donnée :

* L'objet [Bearer][] contient les informations de l'utilisateur : _username_,
  _user attributes_, et _group affiliations_ dans le portail
* L'objet [PortalRequest][] contient les informations sur la requête que fera votre Soffit, 
  comme les _parameters_, _mode_, et _window state_
* L'objet [Preferences][] contient une collection de paramètres de publication de votre Soffit 
  choisis par l'administrateur ; Ce sont des options que vous définissez 
  pour vos besoins. l'usage de préférences est optionnelle
* L'objet [Definition][] contient les métadonnées de publication de votre Soffit 
  dans le portail ; ce sont des paramètres définis par et utilisés par le portail lui-même, comme 
  _title_ et _chrome style_

## Accèder au Modèle de Donnée depuis une JSP

Tous ces objets sont définis dans l'Expression Language (EL) Context dans
laquelle votre fichier `.jsp` s'exécute.  Utilisez une notation en camel-case pour les référencer, par 
exemple...

``` jsp
<h2>Hello ${bearer.username}</h2>
```

## L'Annotation `@SoffitModelAttribute` 

Parfois, le modèle de données fourni par une Soffit ne suffit pas - parfois vous avez besoin
de définir vos propres objets pour le rendu d'une JSP. Pour une soffit basée sur Spring Boot, 
l'annotation [@SoffitModelAttribute][] peut satisfaire ce besoin.

### Exemples `@SoffitModelAttribute`

Annotatez un bean Spring avec `@SoffitModelAttribute` pour rendre le bean entier
disponible depuis votre JSP.

``` java
@SoffitModelAttribute("settings")
@Component
public class Settings {

    public int getMaxNumber() {
        return 100;
    }

}
```

Annoter une méthode sur un bean Spring avec `@SoffitModelAttribute` pour permettre à votre Soffit
d'invoquer la méthode et retourner la valeur disponible dans votre JSP.

``` java
@Component
public class Attributes {

    @SoffitModelAttribute("bearerJson")
    public String getBearerJson(Bearer bearer) {
        String result = null;
        try {
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bearer);
        } catch (JsonProcessingException e) {
            final String msg = "Unable to write the Bearer object to JSON";
            throw new RuntimeException(msg, e);
        }
        return result;
    }

}
```

Les signatures des méthodes annotées avec `@SoffitModelAttribute` sont flexibles; 
vous pouvez prendre n'importe lequel, tous ou aucun des objets suivants en tant que paramètres, 
et ce, dans n'importe quel ordre:

* `HttpServletRequest`
* `HttpServletResponse`
* `Bearer`
* `PortalRequest`
* `Preferences`
* `Definition`

[Bearer]: ../../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/Bearer.java
[PortalRequest]: ../../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/PortalRequest.java
[Preferences]: ../../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/Preferences.java
[Definition]: ../../../../uPortal-soffit/src/main/java/org/apereo/portal/soffit/model/v1_0/Definition.java
[@SoffitModelAttribute]: ../../../../uPortal-soffit-renderer/src/main/java/org/apereo/portal/soffit/renderer/SoffitModelAttribute.java
