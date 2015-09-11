# Strategy for employing Angular.js in portal skins and/or portlets.
## Definitions
The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be
interpreted as described in RFC 2119.

## Skins/Portal
- MAY use the `$compile` service to compile DOM nodes of all loaded portlets.
  - This has the added benefit of allowing a portal to apply directives to
    existing standard portlet content if desired.
- MUST check portlet contents for the "directive" `<!-- uportal-use-angular -->`
  and, if found, use the `$compile` service to compile the DOM nodes with a new
  isolate $scope. See below for an example.

```javascript
if(portletMarkup.indexOf('uportal-use-angular') > -1) {
  var newScope = $scope.$new(true, $scope);
  $compile(iEle.find('div')[0])(newScope);
}
```

- MUST create a global window.up.ngApp directive that exposes the Angular
  functions `$controllerProvider.register` (with name controller),
  `$provide.service`, `$provide.factory`, `$provide.value`, and
  `$compileProvider.directive`. See the code directly below for an example.

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

## Portlets
- MUST check for existence of Angular in two ways.
    - On global window object
    - As an as-yet-unloaded script tag with id 'uportal-angular-script'.
- MUST NOT use the ng-app directive or attempt to bootstrap outside their boundaries/chrome.
- MUST be written as portably as possible and will be expected to work well
  with any version of Angular 1.x, as the precise version of Angular is unknown
  for most implementations.
- MUST namespace their module names if bootstrapping, as recommended in
  [JavaScript Best
  Practices](https://wiki.jasig.org/display/UPM41/JavaScript+Best+Practices)

### Behavior
- If Angular is found, (e.g. `window.angular === undefined`, or `typeof angular
  === 'undefined'`) portlets MUST check for the lazy-loader window.up.ngApp
  and, if found, use the lazy-loader to register its components.
- If Angular is found, and lazy-loader is not found, then the portlet SHOULD
  assume that another portlet is using Angular, proceed to register its own
  module, and MUST use angular.bootstrap to attach itself to the portlet
  fragment. For an example, see the bootstrap() function in the [code below](#boilerplate-portal-code).
- If Angular is not found but an existing script element with id
  'uportal-angular-script' is found, the portlet MUST NOT create a new script
  element but rather attach its event handler to the existing script tag.
- If Angular is not found on global scope, portlet MUST create a script DOM
  element with id 'uportal-angular-script', and attach to said DOM element a
  'load' event handler to bootstrap itself, or use `$(window).load` to do so.

### External Script Files
- External script files:
  - MUST safely create a `window.up.ngBootstrap` object for registration of
    bootstrap functions.
  - MUST register a `bootstrap` function that takes as a parameter the instance
    id as a string.
  - MUST check for `up.ngApp` lazy loaders and use them to register if
    available.
- JSP files:
  - MUST wait until all scripts are loaded (e.g.
    `$(window).load` , and then call the given bootstrap function, passing in
    its instance id.
  - MUST still check for the existence of Angular, and add the script tag if
    needed.


### Boilerplate Portal Code

- [Also available](https://github.com/andrewstuart/generator-ng-portlet) as a
  [yeoman](http://yeoman.io) generator for convenience.


#### Inline script
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
    if (typeof window.angular === 'undefined') {
      //No matter what, check angular and load if needed.
      var ANGULAR_SCRIPT_ID = 'angular-uportal-script';

      var scr = document.getElementById(ANGULAR_SCRIPT_ID);

      if (!scr) {
        scr = document.createElement('script');
        scr.type = 'text/javascript';
        scr.id = ANGULAR_SCRIPT_ID;
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

### External scripts
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
    if (typeof window.angular === 'undefined') {
      //No matter what, check angular and load if needed.
      var ANGULAR_SCRIPT_ID = 'angular-uportal-script';

      var scr = document.getElementById(ANGULAR_SCRIPT_ID);

      if (!scr) {
        scr = document.createElement('script');
        scr.type = 'text/javascript';
        scr.id = ANGULAR_SCRIPT_ID;
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
    //Otherwise, let jsp call your bootstrapper once angular is loaded.
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

## Administrators
- Administrators SHOULD be warned that any portlets they add to their portal
  should be checked in a safe environment to ensure functionality and
  compatibility
