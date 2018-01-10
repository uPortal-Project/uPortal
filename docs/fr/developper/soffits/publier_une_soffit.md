# Publication d'un soffit

Suivez ces étapes pour voir votre soffit dans uPortal.

## Dans le gestionnaire de portlet

1. Sélectionnez _Enregistrer une nouvelle portlet_
2. Choisissez _Portlet_ dans la liste des types et cliquez sur _Continuer_
3. Sélectionnez _/uPortal_ et _Soffit Connector_ dans l'écran du Sommaire 
   et cliquez sur _Continuer_
4. Entrez les métadonnées de la portlet normalement (_e.g._ nom_fichier, tile, fname, groupes, 
   catégories, état du cycle de vie, _etc._)
5. Sous les Préférences de la portlet, remplacez la valeur de
    `org.apereo.portal.soffit.connector.SoffitConnectorController.serviceUrl`
    avec l'URL de votre soffit, par exemple `http://localhost:8090/soffit/my-soffit`
    fonctionnant indépendamment (en dehors de Tomcat) ou
    `http://localhost:8080/my-porject/soffit/my-soffit` s'exécutant dans Tomcat
6. Cliquez sur _Enregister_

Après avoir terminé ces étapes, vous devriez être en mesure de trouver votre soffit en utilisant 
l'interface de recherche ou ajoutez-la à votre layout à l'aide de la galerie de personnalisation.