# Stratégie pour utiliser Angular.js (1.xx) dans les skins et/ou les portlets du portail.

## Définitions

Les mots clés "DOIT OBLIGATOIREMENT", "NE DOIT SURTOUT PAS", "OBLIGATOIRE", "DOIT", "NE DOIT PAS", "DEVRAIT",
"NE DEVRAIT PAS", "RECOMMANDÉ", "POURRAIT", et "FACULTATIF" dans ce document doivent être
interprété comme décrit dans RFC 2119.

## Abstract

AngularJS est devenu un framework très populaire pour le développement côté client. En raison de la façon dont Angular interdit le "bootstrapping" imbriqué de
modules, un soin particulier doit obligatoirement être requis pour s'assurer que plusieurs  programmes Angular ne rentrent 
pas en conflit, ce qui dégraderait l'usage, indépendamment de la façon dont la skin ou
les fragments de page sont combinés.

Ce document présente une stratégie dans laquelle uPortal et/ou des portlets peuvent utiliser Angular et coexister sans conflit.

Concrètement cela signifie que:

- Angular peut être utilisé dans les Skins d'uPortal
- Les portlets Angular peuvent être utilisées, même à plusieurs sur la même page sans conflit
- Les portlets Angular peuvent être utilisées dans une Skin d'un portail Angular sans conflit

## Skins/Portail

- On DOIT activer les portlets Angular en effectuant l'une des opérations suivantes:
    - Vérifier dans le contenu de la portlet, le commentaire de la "directive" 
    `<!-- uportal-use-angular -->`, et s'il est trouvé, utilisez le service de $compile sur les noeuds DOM.
    - Utilisez le service `$compile` pour compiler les nœuds DOM de toutes les portlets chargées.
    - En supplément cela à l'avantage de permettre au portail d'appliquer des directives à des contenus standard de portlets existants si vous le souhaitez.
    
- On DOIT Obligatoirement créer une directive globale window.up.ngApp qui va exposer les fonctions Angular `$controllerProvider.register` (avec un nom de contrôleur), `$provide.service`, `$provide.factory`, `$provide.value`, et `$compileProvider.directive`. Voir le code directement ci-dessous en exemple.

```javascript
angular.module('foo').config(function getLazyLoaders($compileProvider,
$controllerProvider, $provide) {
  //Register controller helper
  window.up.ngApp.controller = function(name, ctrl) {
    $controllerProvider.register(name, ctrl);
      return window.up.ngApp;
  };

  //Register $provide helpers
  ['service', 'factory', 'value'].forEach(function(t) {
    window.up.ngApp[t] = function(name, thing) {
      $provide[t](name, thing);
      return window.up.ngApp;
    };
  });

  //Register directive helper
  window.up.ngApp.directive = function(name, dirFactory) {
    $compileProvider.directive(name, dirFactory);
    return window.up.ngApp;
  };
```

## Les Portlets

- DOIVENT OBLIGATOIREMENT vérifier l'existence d'Angular de deux manières.
    - Sur l'objet `fenêtre` global (`window.angular`)
    - En tant que balise script non encore chargée avec l'identifiant 'uportal-Angular-script'.
- NE DOIVENT SURTOUT PAS utiliser la directive ng-app ou tenter de s'amorcer en dehors de leurs limites/cadre.
- DOIVENT être écrites de façon la plus portable possible et devraient fonctionner correctement avec n'importe quelle version d'Angular 1.x, puisque la version précise d'Angular sera inconnue pour la plupart des implémentations.
- DOIVENT avoir un namespace dans leurs noms de module en cas de "bootstrapping", comme recommandé dans [JavaScript Best
    Practices](https://wiki.jasig.org/display/UPM41/JavaScript+Best+Practices)
- DEVRAIENT utiliser la directive `<!-- uportal-use-angular -->`comme indicateur au portail de ne pas compiler (avec $compile) tout le contenu de toutes les portlets.

### Comportement pour les contrôles Angular

- Si Angular est trouvé, (e.g. `window.Angular === undefined`, or `typeof Angular === 'undefined'`) les portlets DOIVENT chercher le lazy-loader window.up.ngApp et, s'il est trouvé, utiliser the lazy-loader pour enregistrer ses composants.
- Si Angular est trouvé, et le lazy-loader  n'est pas trouvé, alors le portlet DEVRAIT assumer qu'un autre portlet utilise Angular, et procéder à enregistrer son propre  module, et DOIT utiliser Angular.bootstrap pour se joindre elle-même itself au fragment. Pour un exemple, voir la fonction bootstrap() dans le [code ci-dessous](#boilerplate-portal-code).
- Si Angular n'est pas trouvé mais un élément de script existant avec id 'uportal-Angular-script' est trouvé, le portlet NE DOIT PAS créer un nouvel élément script, mais attacher plutôt son gestionnaire d'événements à la balise de script existante.
- Si Angular n'est pas trouvé sur la portée globale, le portlet DOIT créer une balise script DOM avec id 'uportal-Angular-script', et attacher au dit élément DOM un event handler 'load' pour s'amorcer, ou utiliser `$(window).load` pour le faire.

### Fichiers de script externes

-   Les Fichiers de script externes :
    -   DOIVENT créer en toute sécurité un objet `window.up.ngBootstrap` pour enregistrer leur fonctions d'amorçage.
    -   DOIVENT comporter une fonction `bootstrap` qui prendra en paramètre une instance d'id comme une chaîne `string`.
    -   DOIVENT vérifier l'existence des lazy loaders `up.ngApp`  et les utiliser pour s'enregistrer si ils sont disponibles.
    
-   Les fichiers JSP :
    -   DOIVENT attendre que tous les scripts soient chargés (e.g.
        `$(window).load` , et alors après seulement appeler la dite function d'amorçage en lui passant son instance d'id.
    -   DOIVENT toujours vérifier l'existence d'Angular, et ajouter la balise de script si nécessaire.

### Boilerplate Portal Code

-   [Aussi disponible](https://github.com/andrewstuart/generator-ng-portlet) comme générateur
    [yeoman](http://yeoman.io) pour plus de commodité.


#### Script Inline

```jsp
<%@ page contentType="text/html" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>

<portlet:defineObjects/>

<c:set var="nc"><portlet:namespace/></c:set>
<c:set var="lc" value="${fn:toLowerCase(nc)}" />
<c:set var="n" value="${fn:replace(lc, '_', '')}"/>



<script type="text/javascript">
  (function(window, $) {
    if (typeof window.Angular === 'undefined') {
      //No matter what, check Angular and load if needed.
      var Angular_SCRIPT_ID = 'Angular-uportal-script';

      var scr = document.getElementById(Angular_SCRIPT_ID);

      if (!scr) {
        scr = document.createElement('script');
        scr.type = 'text/javascript';
        scr.id = Angular_SCRIPT_ID;
        scr.async = true;
        scr.charset = 'utf-8';
        scr.src = 'https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular.js';
        document.body.appendChild(scr);
      }
      //Working inline, so call our bootstrap function.
      $(window).load(bootstrap);
    } else {
      if (window.up.ngApp) {
        register(window.up.ngApp);
      } else {
        bootstrap();
      }
    }

    function bootstrap() {
      var app = angular.module('${n}-test2', []);
      register(app);
      angular.bootstrap(document.getElementById('${n}-test2'), ['${n}-test2']);
    }

    function register(app) {
      app.controller('test2Controller', function($scope) {
        $scope.awesomeThings = ['AngularJS', 'Bower', 'Grunt', 'Yeoman', 'uPortal', 'Open Source!'];
      });
    }
  })(window, up.jQuery);
</script>

<style>
  #${n}-test2[ng-cloak] {
    display: none;
  }
</style>

<div id="${n}-test2" ng-cloak ng-controller="test2Controller">
  <h1>Awesome Things</h1>
  <ul>
    <li ng-repeat="thing in awesomeThings"> {{$index + 1}}. {{thing}} </li>
  </ul>
</div>
```

### Scripts externes

```jsp
<%@ page contentType="text/html" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>

<portlet:defineObjects/>

<c:set var="nc"><portlet:namespace/></c:set>
<c:set var="lc" value="${fn:toLowerCase(nc)}" />
<c:set var="n" value="${fn:replace(lc, '_', '')}"/>

<script type="text/javascript" src="/jasig-widget-portlets/test/scripts/module.js"></script>


<script type="text/javascript">
  (function(window, $) {
    if (typeof window.Angular === 'undefined') {
      //No matter what, check Angular and load if needed.
      var Angular_SCRIPT_ID = 'Angular-uportal-script';

      var scr = document.getElementById(Angular_SCRIPT_ID);

      if (!scr) {
        scr = document.createElement('script');
        scr.type = 'text/javascript';
        scr.id = Angular_SCRIPT_ID;
        scr.async = true;
        scr.charset = 'utf-8';
        scr.src = 'https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular.js';
        document.body.appendChild(scr);
      }
    }

    $(window).load(function() {
      if ( up.ngBootstrap ) {
        //Once the needed scripts ready, bootstrap if needed.
        up.ngBootstrap.test('${n}');
      }
    });

  })(window, up.jQuery);
</script>

<style>
  #${n}-test[ng-cloak] {
    display: none;
  }
</style>

<div id="${n}-test" ng-cloak ng-controller="testController">
  <h1>Awesome Things</h1>
  <ul>
    <li ng-repeat="thing in awesomeThings"> {{$index + 1}}. {{thing}} </li>
  </ul>
</div>
```

```javascript
(function(window, _) {
  'use strict';

  if (window.up.ngApp) {
    //If loaded, register right away.
    register(window.up.ngApp);
  } else {
    //Otherwise, let jsp call your bootstrapper once Angular is loaded.
    window.up = window.up || {};
    window.up.ngBootstrap = window.up.ngBootstrap || {};

    window.up.ngBootstrap.test = function(n) {
      var app = angular.module(n + '-test', []);
      register(app);

      var bootEle = document.getElementById(n + '-test');
      angular.bootstrap(bootEle, [n + '-test']);
    }
  }


  function register(app) {
    app.controller('testController', function($scope) {
      $scope.awesomeThings = ['AngularJS', 'Bower', 'Grunt', 'Yeoman', 'uPortal', 'Open Source!'];
    });
  }
})(window, up.underscore);
```

## Administrateurs

-   Les administrateurs DEVRAIENT être avertis que toutes les portlets qu'ils ajoutent à leur portail devraient être vérifiées dans un environnement sûr pour s'assurer de leur fonctionnalité et leur compatibilité
