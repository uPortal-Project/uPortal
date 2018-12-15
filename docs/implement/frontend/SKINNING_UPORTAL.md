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

### Default Skin

**Selecting a default skin for a user based on a user attribute can be done by following these steps:**
All bean should be creates into one file of *uPortal-webapp/src/main/resources/properties/contextOverrides/\*.xml*

***1. Create a bean that will map to user attribute*** 
```xml 
 <bean id="customskinServerName" class="org.apereo.portal.rendering.xslt.UserAttributeSkinMappingTransformerConfigurationSource">
    <property name="stylesheetDescriptorNames">
      <set>
        <value>Respondr</value>
      </set>
    </property>
    <property name="skinAttributeName" value="serverName" />
    <property name="attributeToSkinMap">
        <map>
            <entry key=".*\.example\.com" value="example.com" />
        </map>
    </property>
</bean>
```
***2. Specify the Stylesheet to apply***

List all stylesheet into `stylesheetDescriptorNames` to apply the skin customization in a tag `<value></value>`.
***3. Specify the User Attribute Name***

Modify `<property name="skinAttributeName" value="serverName" />` changing the value to the user attribute name to base the skin choice on.
***4. Map Attribute Value Patterns to Skin Names***
 
Add one `<entry key=".*\.example\.com" value="example.com" />` in the attributeToSkinMap for each mapped skin. The key is a regular expression pattern, the value is the name of the skin to set if the pattern matches.


**To selecting a default skin for a user based on group memebership can be done by using a such bean**

```xml
  <bean class="org.apereo.portal.rendering.xslt.UserGroupSkinMappingTransformerConfigurationSource">
    <property name="stylesheetDescriptorNames">
        <set>
          <value>Respondr</value>
        </set>
    </property>
    <property name="groupToSkinMap">
        <map>
            <entry key="pags.studends" value="students" />
            <entry key="pags.staff" value="staff" />
            <entry key="pags.faculty" value="staff" />
        </map>
    </property>
  </bean>
```
*Map Group Keys to Skin Names*

Add one `<entry key="pags.faculty" value="staff" />` in the groupToSkinMap for each mapped skin. The key is a uPortal group key, qualified with the group store name, the value is the name of the skin to set if the user is a deep member of the specified group.

**You can repeat theses steps to have several beans to map skin on different criteria**

**Last Step**
To apply your customization create a bean that will reference your beans and integrate them into the renderingPipeline process :
```xml
  <util:list id="customSkinsTransformers">
    <ref bean="guestskinTransformer"/>
    <ref bean="defaultskinTransformer"/>
    <ref bean="agriSkinTransformer"/>
  </util:list>
```
- The id should not be changed as it permit to override the default empty configuration
- Add all your referenced bean created (they should have a unique id) as a `<ref bean="ID"/>`
- The order is important as the skin name property will be overriden by each beans defined, the order follow their position into this bean. 
 
### Page Effects

Portal background color and image can have special effects applied.
Setting `@portal-page-body-background-image-filter` allows for any combination [css filters](https://developer.mozilla.org/en-US/docs/Web/CSS/filter) to be applied.

![No background effect](images/background-filter-none.png)

![Sepia background effect](images/background-filter-sepia.png)
