# Skinning uPortal

## Table of Contents

1.  [Creating a skin](#creating-a-skin)

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
