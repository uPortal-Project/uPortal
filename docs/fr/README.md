<link rel="stylesheet" href="../css/reset_image_size.css">

# Documentation uPortal

**NOTE :** Cette partie est en construction. Son intention est de servir de base
pour la documentation d'uPortal version 5 et supérieures.
Les documentations pour les versions antérieures sont disponibles dans
[Confluence](https://wiki.jasig.org).  Référez vous à *Documentation des précédentes versions* \[en\]
ci-après.

<table border="0">
  <tr>
    <td>
      <a href="https://www.w3.org/TR/WCAG20/">
        <img src="https://www.w3.org/WAI/wcag2AA-blue-v.svg" alt="WCAG 2 AA Badge">
      </a>
      <a href="http://issuestats.com/github/Jasig/uPortal">
        <img src="http://issuestats.com/github/Jasig/uPortal/badge/pr" alt="Statistiques des Issues">
      </a>
      <a href="https://source.android.com/setup/contribute/code-style">
        <img src="https://img.shields.io/badge/code_style-AOSP-green.svg?style=flat" alt="AOSP Code Style">
      </a>
      <a href="https://github.com/search?q=topic%3Auportal+topic%3Asoffit&type=Repositories">
        <img src="https://img.shields.io/badge/discover-soffits-blue.svg?style=flat" alt="Découvrez les Soffits">
      </a>
      <a href="https://github.com/search?q=topic%3Auportal+topic%3Aportlet&type=Repositories">
        <img src="https://img.shields.io/badge/discover-portlets-blue.svg?style=flat" alt="Découvrez les Portlets">
      </a>
      <br>
      <table>
        <tr>
          <th>
            Version
          </th>
          <th>
            Linux
          </th>
          <th>
            Windows
          </th>
          <th>
            Couverture
          </th>
          <th>
            Dépendences
          </th>
        </tr>
        <tr>
          <td>
            <a href="https://github.com/uPortal-Project/uPortal/tree/master">
              uPortal 5
            </a>
          </td>
          <td>
            <a href="https://github.com/uPortal-Project/uPortal/actions?query=branch%3Amaster">
              <img src="https://github.com/uPortal-Project/uPortal/workflows/CI/badge.svg?branch=master" alt="Linux Build Status">
            </a>
          </td>
          <td>
            <a href="https://github.com/uPortal-Project/uPortal/actions?query=branch%3Amaster">
              <img src="https://github.com/uPortal-Project/uPortal/workflows/CI/badge.svg?branch=master" alt="Windows Build Status">
            </a>
          </td>
          <td>
            <a href="https://coveralls.io/github/Jasig/uPortal?branch=master">
              <img src="https://coveralls.io/repos/github/Jasig/uPortal/badge.svg?branch=master" alt="Coverage Status">
            </a>
          </td>
          <td>
            <a href='https://www.versioneye.com/user/projects/59e525762de28c000f9188ae'>
              <img src='https://www.versioneye.com/user/projects/59e525762de28c000f9188ae/badge.svg?style=flat-square' alt="Dependency Status" />
            </a>
          </td>
        </tr>
        <tr>
          <td>
            <a href="https://github.com/uPortal-Project/uPortal/tree/rel-4-3-patches">
              uPortal 4.3
            </a>
          </td>
          <td>
            <a href="https://travis-ci.org/Jasig/uPortal">
              <img src="https://travis-ci.org/Jasig/uPortal.svg?branch=rel-4-3-patches" alt="Linux Build Status">
            </a>
          </td>
          <td>
            <a href="https://ci.appveyor.com/project/drewwills/uportal/branch/rel-4-3-patches">
              <img src="https://ci.appveyor.com/api/projects/status/8t95sjt090mf62dh/branch/rel-4-3-patches?svg=true" alt="Windows Build Status">
            </a>
          </td>
          <td>
            <a href="https://coveralls.io/github/Jasig/uPortal?branch=rel-4-3-patches">
              <img src="https://coveralls.io/repos/github/Jasig/uPortal/badge.svg?branch=rel-4-3-patches" alt="Coverage Status">
            </a>
          </td>
          <td>
            N/A
          </td>
        </tr>
      </table>
    </td>
    <td>
      <table>
        <tr>
          <th>
            Impliquez-vous
          </th>
          <th>
            Accès
          </th>
        </tr>
        <tr>
          <td>
            Signalez un Bug
          </td>
          <td>
            <a href="https://issues.jasig.org/browse/UP">
              <img src="https://img.shields.io/badge/issue_tacker-Jira-green.svg?style=flat" alt="Issue Tracker">
            </a>
          </td>
        </tr>
        <tr>
          <td>
            Demandez une fonctionnalité
          </td>
          <td>
            <a href="https://issues.jasig.org/browse/UP">
              <img src="https://img.shields.io/badge/issue_tacker-Jira-green.svg?style=flat" alt="Issue Tracker">
            </a>
          </td>
        </tr>
        <tr>
          <td>
            Contribuez au Code
          </td>
          <td>
            <a href="CONTRIBUTING.md">
              <img src="https://img.shields.io/badge/contributing-guide-green.svg?style=flat" alt="Contributing Guide">
            </a>
          </td>
        </tr>
        <tr>
          <td>
            Rejoignez nos conversations
          </td>
          <td>
            <a href="https://gitter.im/uPortal-Project/uPortal?utm_source=badge&amp;utm_medium=badge&amp;utm_campaign=pr-badge">
              <img src="https://badges.gitter.im/uPortal-Project/uPortal.svg" alt="Gitter">
            </a>
            <br>
            <a href="https://www.irccloud.com/invite?channel=%23jasig-uportal&amp;hostname=irc.freenode.net&amp;port=6697&amp;ssl=1">
              <img src="https://img.shields.io/badge/IRC-%23jasig--uportal-1e72ff.svg?style=flat" alt="uPortal on IRC">
            </a>
            <br>
            <a href="https://twitter.com/uPortal">
              <img src="https://img.shields.io/twitter/follow/uPortal.svg?style=social&amp;label=Follow" alt="Twitter Follow">
            </a>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

## Sections

1.  [Build / Déployer uPortal](monter-et-deployer-uportal.md)
2.  [Implémenter uPortal](implementer/README.md)
3.  [Administration Système d'uPortal](sysadmin/README.md)
4.  [Guide du développeur](developper/README.md)
5.  [Navigateurs Web supportés](NAVIGATEURS_SUPPORTES.md)
6.  [Accessibilité](ACCESSIBILITE.md)
7.  [Contributeurs au projet](COMMITTERS.md)
8.  [Historique](HISTORIQUE.md)
9.  [Glossaire](GLOSSAIRE.md)

## Liens externes

* [Foundation Apereo ](https://www.apereo.org/)
* [Wiki uPortal](https://wiki.jasig.org/display/UPC/Home)

## Documentation des versions précédentes

-   [Manuel uPortal 5.9](https://github.com/uPortal-Project/uPortal/tree/v5.9.0/docs)
-   [Manuel uPortal 5.8](https://github.com/uPortal-Project/uPortal/tree/v5.8.2/docs)
-   [Manuel uPortal 5.7](https://github.com/uPortal-Project/uPortal/tree/v5.7.1/docs)
-   [Manuel uPortal 5.6](https://github.com/uPortal-Project/uPortal/tree/v5.6.1/docs)
-   [Manuel uPortal 5.5](https://github.com/uPortal-Project/uPortal/tree/v5.5.1/docs)
-   [Manuel uPortal 5.4](https://github.com/uPortal-Project/uportal/tree/v5.4.1/docs/fr)
-   [Manuel uPortal 5.3](https://github.com/uPortal-Project/uportal/tree/v5.3.2/docs/fr)
-   [Manuel uPortal 5.2](https://github.com/uPortal-Project/uportal/tree/v5.2.0/docs/fr)
-   [Manuel uPortal 5.1](https://github.com/uPortal-Project/uPortal/tree/v5.1.2/docs/fr)
-   [Manuel uPortal 5.0](https://github.com/uPortal-Project/uPortal/tree/v5.0.7/docs/fr)
-   [Manuel uPortal 4.3](https://wiki.jasig.org/display/UPM43/Home)
-   [Manuel uPortal 4.2](https://wiki.jasig.org/display/UPM42/Home)
-   [Manuel uPortal 4.1](https://wiki.jasig.org/display/UPM41/Home)
-   [Manuel uPortal 4.0](https://wiki.jasig.org/display/UPM40/Home)
-   [Manuel uPortal 3.2](https://wiki.jasig.org/display/UPM32/Home)
-   [Manuel uPortal 3.1](https://wiki.jasig.org/display/UPM31/Home)
-   [Manuel uPortal 3.0](https://wiki.jasig.org/display/UPM30/Home)
