<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<div id="${n}google-analytics-config" class="google-analytics-config">
    <div class="property-configs">
    </div>
    <button class="addPropertyConfig">Add Host</button> <button class="saveConfig">Save</button> <button class="done">Done</button>
</div>

<script id="${n}propertyConfigTemplate" type="text/template">
   <a class="property destroy"></a>
   <div class="default-property-name">Default Property</div>
   <input name="propertyName" value="{{= name }}"/>

   <div>
       <label>Property Id:</label><input name="propertyId" value="{{= propertyId }}"/>
   </div>
   <div class="property-config">
   </div>
   <div class="property-dimensions">
   </div>
</script>

<script id="${n}settingsTemplate" type="text/template">
   <div>
      <label><a href="{{= settingsUrl }}" target="_blank" rel="noopener noreferrer">{{= settingsName }}:</a></label>
      <input class="settingName" name="settingName" value=""/>:
      <input class="settingValue" name="settingValue" value=""/>
      <button class="addSetting">+</button>
   </div>
   <div class="setting-data"></div>
</script>

<script id="${n}settingTemplate" type="text/template">
   <span class="view name">
      <label>{{= name }}</label>:
   </span>

   <span class="view value">
      <label>{{= value }}</label>
   </span>

   <a class="setting destroy"></a>
</script>

<portlet:resourceURL var="getDataUrl" id="getData" escapeXml="false" />
<portlet:resourceURL var="storeDataUrl" id="storeData" escapeXml="false" />
<portlet:actionURL var="configDoneUrl" name="configDone" escapeXml="false" />

<script type="text/javascript">
up.analytics = up.analytics || {};
up.analytics.config = up.analytics.config || {};
up.analytics.config.model = up.analytics.config.model || {};
up.analytics.config.view = up.analytics.config.view || {};

(function($, Backbone, _) {
   'use strict';

   var data = ${up:json(data)};

   /**
    * Centralized sync method
    */
   up.analytics.sync = function(type) {
      return function(method, model, options) {
         //DOES NOTHING FOR NOW, SOMEDAY WE WILL HAVE REAL-TIME SYNC/SAVE
      };
   };

   up.analytics.config.model.Setting = Backbone.Model.extend({
      sync : up.analytics.sync("Setting")
   });

   up.analytics.config.model.SettingsList = Backbone.Collection.extend({
      model : up.analytics.config.model.Setting,
      sync : up.analytics.sync("SettingsList")
   });

   up.analytics.config.model.PropertyConfiguration = Backbone.Model.extend({
      sync : up.analytics.sync("PropertyConfiguration"),
      initialize : function(data) {
         this.set("config", new up.analytics.config.model.SettingsList(data.config || []));
         this.set("dimensionGroups", new up.analytics.config.model.SettingsList(data.dimensionGroups || []));
      },
      defaults : {
         name : "",
         propertyId : ""
      }
   });

   up.analytics.config.model.PropertyConfigurationList = Backbone.Collection.extend({
      model : up.analytics.config.model.PropertyConfiguration,
      sync : up.analytics.sync("PropertyConfigurationList")
   });

   up.analytics.config.model.GlobalConfiguration = Backbone.Model.extend({
      sync : up.analytics.sync("GlobalConfiguration"),
      initialize : function(data) {
         data = data || {};
         this.set("defaultConfig", new up.analytics.config.model.PropertyConfiguration(data.defaultConfig || {}));
         this.set("hosts", new up.analytics.config.model.PropertyConfigurationList(data.hosts || []));
      }
   });

   up.analytics.config.view.SettingView = Backbone.View.extend({
      tagName : "div",
      className : "setting-container",
      template : _.template($('#${n}settingTemplate').html()),

      events : {
         "click a.setting.destroy" : "clear"
      },

      initialize : function() {
         this.listenTo(this.model, 'change', this.render);
         this.listenTo(this.model, 'destroy', this.remove);
      },

      render : function() {
         $(this.el).html(this.template(this.model.toJSON()));
         return this;
      },

      // Remove the item, destroy the model.
      clear : function() {
         this.model.destroy();
      }
   });

   up.analytics.config.view.SettingsView = Backbone.View.extend({
      tagName : "div",
      className : "settings",
      template : _.template($("#${n}settingsTemplate").html()),
      initialize : function(options) {
         this.settingsName = options.settingsName;
         this.settingsUrl = options.settingsUrl;

         this.listenTo(this.model, 'add', this.addOne);
         this.listenTo(this.model, 'remove', this.remove);
         this.listenTo(this.model, 'reset', this.addAll);
      },
      events : {
         "click .addSetting" : "create"
      },
      render : function() {
         var modelData = this.model.toJSON();
         modelData["settingsName"] = this.settingsName;
         modelData["settingsUrl"] = this.settingsUrl;
         $(this.el).html(this.template(modelData));


         var container = document.createDocumentFragment();
         _.forEach(this.model.models, function(setting) {
            var view = new up.analytics.config.view.SettingView({
               model : setting
            });
            container.appendChild(view.render().el);
         }, this);
         this.$(".setting-data").html(container);


         return this;
      },

      // Add a single setting item to the list by creating a view for it
      addOne : function(setting) {
         var view = new up.analytics.config.view.SettingView({
            model : setting
         });
         this.$(".setting-data").append(view.render().el);
      },

      // Add all items in the setting collection at once.
      addAll : function() {
         this.model.each(this.addOne, this);
      },

      create : function(e) {
         this.newName = this.$(".settingName");
         this.newValue = this.$(".settingValue");

         if (!this.newName.val() || !this.newValue.val()) {
            return;
         }

         this.model.create({
            name : this.newName.val(),
            value : this.newValue.val()
         });

         this.newName.val('');
         this.newValue.val('');
      },

      remove : function(e) {
         this.render();
      }
   });

   up.analytics.config.view.PropertyConfigurationView = Backbone.View.extend({
      tagName : "div",
      className : "analytics-host",
      template : _.template($("#${n}propertyConfigTemplate").html()),
      initialize : function(options) {
          this.isDefaultConfig = (options.isDefaultConfig == true);
      },
      events : {
          "click a.property.destroy" : "clear",
          "blur input[name=propertyId]" : "updatePropertyId",
          "blur input[name=propertyName]" : "updatePropertyName"
      },
      render : function() {
         $(this.el).html(this.template(this.model.toJSON()));

         if (this.isDefaultConfig) {
             this.$("div.default-property-name").show();
             this.$("input[name=propertyName]").hide();
         }
         else {
             this.$("div.default-property-name").hide();
             this.$("input[name=propertyName]").show();
         }

         var configSettings = new up.analytics.config.view.SettingsView({
                  model : this.model.get("config"),
                  settingsName : "Settings",
                  settingsUrl : "https://developers.google.com/analytics/devguides/collection/analyticsjs/field-reference#create"
               });
         this.$(".property-config").html((configSettings.render().el));

         var dimensionSettings = new up.analytics.config.view.SettingsView({
                  model : this.model.get("dimensionGroups"),
                  settingsName : "Dimensions",
                  settingsUrl : "https://support.google.com/analytics/answer/2709829"
               });
         this.$(".property-dimensions").html((dimensionSettings.render().el));

         return this;
      },
      updatePropertyId : function() {
          var propertyId = this.$("input[name=propertyId]").val();
          this.model.set("propertyId", propertyId);
      },
      updatePropertyName : function() {
          var propertyName = this.$("input[name=propertyName]").val();
          this.model.set("name", propertyName);
      },
      // Remove the item, destroy the model.
      clear : function() {
         this.model.destroy();
      }
   });

   up.analytics.config.view.GlobalConfigurationView = Backbone.View.extend({
      el : $("div#${n}google-analytics-config"),

      initialize : function() {
         this.listenTo(this.model, 'change', this.render);
         this.listenTo(this.model.get("hosts"), 'add', this.render);
         this.listenTo(this.model.get("hosts"), 'remove', this.render);

         this.render();
      },
      events : {
         "click .addPropertyConfig" : "newPropertyConfig",
         "click .saveConfig" : "saveConfig",
         "click .done" : "completeConfig"
      },
      render : function() {
         var container = document.createDocumentFragment();

         var defaultConfig = this.model.get("defaultConfig");
         var defaultConfigView = new up.analytics.config.view.PropertyConfigurationView({
            model : defaultConfig,
            isDefaultConfig: true
         });
         container.appendChild(defaultConfigView.render().el);

         var hosts = this.model.get("hosts");
         _.forEach(hosts.models, function(hostConfig) {
            var hostView = new up.analytics.config.view.PropertyConfigurationView({
               model : hostConfig
            });
            container.appendChild(hostView.render().el);
         }, this);

         this.$el.find(".property-configs").html(container);

         return this;
      },
      newPropertyConfig : function() {
         var instName = window.prompt("Host Name:");
         if (!instName)
            return;
         this.model.get("hosts").create({
            "name" : instName
         });
      },
      saveConfig : function() {
          $.ajax({
              type: "POST",
              url: "${storeDataUrl}",
              dataType: 'json',
              data: {config:JSON.stringify(globalConfig)},
              success: function (data) {
                  //TODO force sync of globalConfig with returned data
              }
          });
      },
      completeConfig : function() {
          window.location.href = "${configDoneUrl}";
      }
   });

   var globalConfig = new up.analytics.config.model.GlobalConfiguration(data);

   new up.analytics.config.view.GlobalConfigurationView({
      model : globalConfig
   });



})(up.jQuery, up.Backbone, up._);
</script>
