# Publication d'un soffit

Suivre ces étapes pour voir votre soffit dans uPortal.

## Dans le gestionnaire de portlet

1. Sélectionner _Enregistrer une nouvelle portlet_
2. Choisir _Portlet_ dans la liste des types et cliquez sur _Continuer_
3. Sélectionner _/uPortal_ et _Soffit Connector_ dans l'écran du Sommaire et cliquer sur _Continuer_
4. Entrer les métadonnées de la portlet normalement (_e.g._ nom_fichier, title, fname, groupes, catégories, état du cycle de vie, _etc._)
5. Sous les Préférences de la portlet, remplacer la valeur de `org.apereo.portal.soffit.connector.SoffitConnectorController.serviceUrl` avec l'URL de votre soffit, par exemple `http://localhost:8090/soffit/my-soffit` fonctionnant indépendamment (en dehors de Tomcat) ou `http://localhost:8080/my-porject/soffit/my-soffit` s'exécutant dans Tomcat
6. Cliquer sur _Enregister_

Après avoir terminé ces étapes, vous devriez être en mesure de trouver votre soffit en utilisant 
l'interface de recherche ou en l'ajoutant à votre layout à l'aide de la galerie de personnalisation.