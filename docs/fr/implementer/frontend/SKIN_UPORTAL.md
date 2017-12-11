# Skin uPortal

## Table des matières

1.  [Créer une Skin](#creating-a-skin)
2.  [Configuration de Skin](#skin-configuration)
3.  [Notes additionnelles](#special-notes)
    1.  [Dynamic Respondr Skin](#dynamic-respondr-skin)
    2.  [Page Effects](#page-effects)

## Créer une Skin

1. Commencez par le dossier racine du code source uPortal
2. Accédez au dossier *uportal-war/src/main/webapp/media/skins*
3. Copiez le dossier *defaultSkin/* et attribuez-lui un nom spécifique à votre institution (par exemple *wolverine/*)
4. Copiez le fichier *defaultSkin.less* et attribuez-lui le même nom (par exemple, *wolverine.less*)
5. Modifiez les imports dans le fichier de Skin pour pointer vers le dossier de skin. par exemple. *wolverine.less*

    ``` less
    /** DO NOT REMOVE OR ALTER THESE INCLUDES **/
    @import "defaultSkin/less/variables.less";
    @import "common/common.less";
    /*******************************************/

    @import "wolverine/less/variables.less";
    @import "wolverine/less/skin.less";
    ```

6. Accédez au dossier *uportal-war/src/main/webapp/media/skins/respondr*
7. Editez *skinList.xml* pour pointer les noms `<skin-name>` et `<skin-key>` vers le nouveau nom de skin. Par exemple.

    ``` xml
    <skin>
        <skin-key>wolverine</skin-key>
        <skin-name>wolverine</skin-name>
        <skin-description>
            Basic skin for the Respondr theme based on Twitter Bootstrap and Responsive Design
        </skin-description>
    </skin>
    ```

8.  Accédez au dossier *uportal-war/src/main/data/default_entities/portlet-definition*
9.  Editez *dynamic-respondr-skin.portlet-definition.xml* et ajouter une `<portal-preference>` avec un `<name>` de `PREFdynamicSkinName` et une `<value>` avec le nom de la Skin. Par exemple.

    ``` xml
    <portlet-preference>
        <name>PREFdynamicSkinName</name>
        <value>wolverine</value>
    </portlet-preference>
    ```

10. Accédez au dossier *uportal-war/src/main/data/required_entities/stylesheet-descriptor*
11. Editer *Respondr.stylesheet-descriptor.xml* et changer la `<default-value>` pour le nom de la Skin. Par exemple. 

    ``` xml
    <stylesheet-parameter>
        <name>skin</name>
        <default-value>wolverine</default-value>
        <scope>PERSISTENT</scope>
        <description>Skin name</description>
    </stylesheet-parameter>
    ```

12. Lancer `ant initdb` pour appliquer ces changement en base.
13. Lancer `ant clean deploy-war` pour lancer un Build du portail avec la nouvelle Skin.
14. :Attention: **N'oubliez pas d'ajouter la Skin à Git!**

## Configuration de Skin

uPortal utilise des [variables Less](http://lesscss.org/features/#variables-feature) pour gérer les changement globaux de Skin.
Des Changements peuvent être fait pour surcharger les [variables Bootstrap](/uportal-war/src/main/webapp/media/skins/respondr/common/bootstrap/variables.less) ou les [variables uPortal](/uportal-war/src/main/webapp/media/skins/respondr/defaultSkin/less/variables.less), les changement devraient surtout être fait au niveau du fichier `variable.less`.

## Notes additionnellles

### Dynamic Respondr Skin

Les variables de `@color` 1-6 sont les valeurs que personnalise la portlet dynamic respondr skin.

``` less
@color1
@color2
@color3
@color4
@color5
@color6
```

![Dynamic Respondr Skin Portlet Page](images/dynamic-respondr-skin.png)

### Page Effects

La couleur de fond et l'image de fond du portail peuvent recevoir des effets spéciaux.
Modifier `@portal-page-body-background-image-filter` permet toutes les combinaisons possible de [filtres css](https://developer.mozilla.org/en-US/docs/Web/CSS/filter) d'être appliqué.

![Effet sans background](images/background-filter-none.png)

![Effet Sepia](images/background-filter-sepia.png)
