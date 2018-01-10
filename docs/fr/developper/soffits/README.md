# Soffits uPortal

Une Soffit est une technologie pour créer du contenu fonctionnant dans [Apereo uPortal][]. Elle
est conçu comme une alternative au [développement de portlet JSR-286][].

## Pourquoi voudrais-je développer des soffits ?

Vous êtes un développeur d'applications Web Java. Vous êtes chargé de développer du contenu
pour [Apereo uPortal][].

Vous n'êtes pas enthousiasmé par le développement de Java Portlet
[de manière traditionnelle][] ou même en utilisant [Spring Portlet MVC][]. À raison vous en avez conclu que les API Java Portlet sont _lourdes_, _obtues_ et _interférent activement_ avec les pratiques 
de développement web contemporain et les frameworks que vous voulez utiliser.

Apereo Soffit est une approche alternative à la production de contenu pour uPortal qui
n'est pas basé sur JSR-286 ou le conteneur de portlet.

## Point clefs

1. [Soffit minimale](soffit_minimale.md)
2. [Publication d'une soffit](publier_une_soffit.md)
3. [Modèle de données de soffit](soffit_modele_de_donnee.md)
4. [Options de configuration](options-de-configuration.md)

## Un mot sur les interfaces utilisateur Web modernes

Soffit suppose que vous voulez développer des interfaces utilisateur en utilisant Javascript et
frameworks / librairies modernes comme [React][], [AngularJS][], [Backbone.js][], _etc_.
Ainsi, un composant Soffit ne sera rendu qu'une fois; considérant que l'état (state), les
changements, les transactions, la persistance, _etc_. seront généralement gérés avec le Javascript
et l'API REST.

## Exemples d'applications de soffits

Il y a plusieurs exemples d'applications dans [ce repo][].

[Apereo uPortal]: https://www.apereo.org/projects/uportal
[Développement de portlet JSR-286]: https://jcp.org/en/jsr/detail?id=286
[de la manière traditionnelle]: http://www.theserverside.com/tutorial/JSR-286-development-tutorial-An-introduction-to-portlet-programming
[Spring Portlet MVC]: http://docs.spring.io/autorepo/docs/spring/3.2.x/spring-framework-reference/html/portlet.html
[React]: https://facebook.github.io/react/
[AngularJS]: https://angularjs.org/
[Backbone.js]: http://backbonejs.org/
[ce repo]: https://github.com/drewwills/soffit-samples