# Skinning uPortal

## Table of Contents

1.  [Creating a skin](#creating-a-skin)
2.  [Skin Configuration](#skin-configuration)
3.  [Special Notes](#special-notes)
    1.  [Dynamic Respondr Skin](#dynamic-respondr-skin)
    2.  [Page Effects](#page-effects)

## Creating a skin

1.  Start at the root folder of the uPortal source code
2.  Navigate to the *uportal-war/src/main/webapp/media/skins* folder
3.  Copy the *defaultSkin/* folder and give it an institution specific name (e.g. *wolverine/*)
4.  Copy the *defaultSkin.less* file and give it the same name (e.g. *wolverine.less*)
5.  Edit the imports in the skin file to point to the skin folder. e.g. *wolverine.less*

    ``` less
    /** DO NOT REMOVE OR ALTER THESE INCLUDES **/
    @import "defaultSkin/less/variables.less";
    @import "common/common.less";
    /*******************************************/

    @import "wolverine/less/variables.less";
    @import "wolverine/less/skin.less";
    ```

6.  Navigate to the *uportal-war/src/main/webapp/media/skins/respondr* folder
7.  Edit *skinList.xml* to point the `<skin-name>` and `<skin-key>` to the new skin name. e.g.

    ``` xml
    <skin>
        <skin-key>wolverine</skin-key>
        <skin-name>wolverine</skin-name>
        <skin-description>
            Basic skin for the Respondr theme based on Twitter Bootstrap and Responsive Design
        </skin-description>
    </skin>
    ```

8.  Navigate to the *uportal-war/src/main/data/default_entities/portlet-definition* folder
9.  Edit *dynamic-respondr-skin.portlet-definition.xml* and add a `<portal-preference>` with a `<name>` of `PREFdynamicSkinName` and a `<value>` with the skin name. e.g.

    ``` xml
    <portlet-preference>
        <name>PREFdynamicSkinName</name>
        <value>wolverine</value>
    </portlet-preference>
    ```

10. Navigate to the *uportal-war/src/main/data/required_entities/stylesheet-descriptor* folder
11. Edit *Respondr.stylesheet-descriptor.xml* and change the `<default-value>` to the skin name. e.g.

    ``` xml
    <stylesheet-parameter>
        <name>skin</name>
        <default-value>wolverine</default-value>
        <scope>PERSISTENT</scope>
        <description>Skin name</description>
    </stylesheet-parameter>
    ```

12. Run `ant initdb` to apply the changes to the database.
13. Run `ant clean deploy-war` to build uPortal with the new skin
14. :warning: **Don’t forget to add the new skin to Git!**

## Skin Configuration

uPortal uses [Less variables](http://lesscss.org/features/#variables-feature) to handle global skin changes.
Changes can be made to override the [Bootstrap variables](/uportal-war/src/main/webapp/media/skins/respondr/common/bootstrap/variables.less) or the [uPortal variables](/uportal-war/src/main/webapp/media/skins/respondr/defaultSkin/less/variables.less), changes should be made to the skin's `variable.less` file.

## Special Notes

### Dynamic Respondr Skin

The color variables 1-6 are the values that the dynamic respondr skin portlet customizes.

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

Portal background color and image can have special effects applied.
Setting `@portal-page-body-background-image-filter` allows for any combination [css filters](https://developer.mozilla.org/en-US/docs/Web/CSS/filter) to be applied.

![No background effect](images/background-filter-none.png)

![Sepia background effect](images/background-filter-sepia.png)
