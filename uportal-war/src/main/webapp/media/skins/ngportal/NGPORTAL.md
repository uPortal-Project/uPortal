# Strategy for employing Angular.js in portal skins and/or portlets.
## Definitions
The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be
interpreted as described in RFC 2119.

## Skins/Portal
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
### Behavior
- If Angular is found, (e.g. `window.angular === undefined`, or `typeof angular
  === 'undefined'`) portlets MUST check for the lazy-loader window.up.ngApp
  and, if found, use the lazy-loader to register its components.
- If Angular is found, and lazy-loader is not found, then the portlet SHOULD
  assume that another portlet is using Angular, proceed to register its own
  module, and MUST use angular.bootstrap to attach itself to the portlet
  fragment. For an example, see the bootstrap() function in the code below.
- If Angular is not found but an existing script element with id
  'uportal-angular-script' is found, the portlet MUST NOT create a new script
  element but rather attach its event handler to the existing script tag.
- If Angular is not found on global scope, portlet MUST create a script DOM
  element with id 'uportal-angular-script', and attach to said DOM element a
  'load' event handler to bootstrap itself.

### Boilerplate Portal Code

- [Also available](https://github.com/andrewstuart/generator-ng-portlet) as a
  [yeoman](http://yeoman.io) generator for convenience.

```javascript
(function(window, _) {
    if (typeof window.angular === 'undefined') {
      var ANGULAR_SCRIPT_ID = 'angular-uportal-script';

      //Search for pre-existing script tag
      var scr = document.getElementById(ANGULAR_SCRIPT_ID);

      if (!scr) {
        //Create a new script tag if one is not found.
        scr = document.createElement('script');
        scr.type = 'text/javascript';
        scr.id = ANGULAR_SCRIPT_ID;
        scr.async = true;
        scr.charset = 'utf-8';
        scr.src = 'https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.5/angular.js';
        document.body.appendChild(scr);
      }

      //Add an event listener to the script tag whether created or found.
      scr.addEventListener('load', bootstrap);
    } else {
      //If Angular is already loaded, use the window.up.ngApp lazy-loader to
      //register components.
      register(window.up.ngApp);
    }

    function bootstrap() {
      //If angular was not present, you must assume no current module and
      //lazy-loaders are present, and your module must bootstrap itself with
      //its own module.
      var app = angular.module('career-explore', []);
      register(app);
      angular.bootstrap(document.getElementById('ceContent1'), ['career-explore']);
    }

    function register(app) {
      app
      .service('Explorer', function($http, $q, $timeout) {
        //Service declaration here

        //Standard Angular 1.x.x component declarations can be chained or
        //called from `app` again
      });
    }
})(window, uP._);
```

## Administrators
- Administrators SHOULD be warned that any portlets they add to their portal
  should be checked in a safe environment to ensure functionality and
  compatibility
