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
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%-- Portlet Namespace  --%>
<c:set var="n"><portlet:namespace/></c:set>

<%-- Parameters --%>
<portlet:actionURL var="queryUrl">
	<portlet:param name="execution" value="${flowExecutionKey}" />
</portlet:actionURL>

<%--
PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
| For the standards and guidelines that govern
| the user interface of this portlet
| including HTML, CSS, JavaScript, accessibilty,
| naming conventions, 3rd Party libraries
| (like jQuery and the Fluid Skinning System)
| and more, refer to:
| http://www.ja-sig.org/wiki/x/cQ
--%>

<%-- Styles specific to this portlet --%>
<style type="text/css">
  #${n} .portlet-section {
    margin-bottom: 2em;
  }

  #${n} .portlet-section .titlebar {
    margin: 2em 0;
  }

  #${n} .portlet-section .titlebar .title {
    background-color: #f5f6f7;
    border-bottom: 2px solid #eee;
    border-radius: 4px;
    font-weight: 400;
    margin: 0;
    padding: 0.25em 1em;
  }

  #${n} .form-group label {
    color: #000;
  }

  #${n} .buttons {
    border-top: 1px dotted #ccc;
    border-bottom: 1px dotted #ccc;
    margin: 1em 0;
    padding: 1em 0;
  }

  #${n} .glyphicon-info-sign {
     color: #3a7eef;
  }

  #${n} .name-title-mismatch-warn {
      display: none;
  }
</style>

<!-- Portlet -->
<div class="fl-widget portlet ptl-mgr view-basicinfo" id="${n}" role="section">

	<!-- Portlet Titlebar -->
  <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
  	<h2 class="title" role="heading">
        <spring:message code="${ completed ? 'edit.portlet' : 'register.new.portlet' }"/>
    </h2>
  </div> <!-- end: portlet-titlebar -->
  
  <!-- Portlet Content -->
  <div class="fl-widget-content content portlet-content" role="main">
    
    <form:form modelAttribute="portlet" action="${queryUrl}" method="POST" role="form" class="form-horizontal">
	
	<!-- Portlet Messages -->
    <spring:hasBindErrors name="portlet">
        <!--div class="portlet-msg-error portlet-msg error text-danger" role="alert">
            <form:errors path="*" element="div"/>
        </div--> <!-- end: portlet-msg -->

        <div class="alert alert-danger" role="alert">
          <form:errors path="*" element="div"/>
        </div>
    </spring:hasBindErrors>
		
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="summary.information"/></h3>
      </div>
      <div class="content">

        <div class="form-group">
          <span class="col-sm-3 control-label">
              <label for="portletTitle"><spring:message code="portlet.title"/></label>
              <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.title.tooltip'/>" data-toggle="tooltip" data-placement="top"></span>
          </span>
          <div class="col-sm-9">
            <form:input path="title" type="text" class="form-control" id="portletTitle"/>
          </div>
        </div>
        <div class="form-group name-title-mismatch-warn">
            <div class="col-sm-offset-3 col-sm-9">
                <div class="alert alert-info">
                    <spring:message code="portlet.name.title.mismatch"/>
                </div>
            </div>
        </div>
        <div class="form-group">
          <span class="col-sm-3 control-label">
              <label for="portletName"><spring:message code="portlet.name"/></label>
                <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.name.tooltip'/>" data-toggle="tooltip" data-placement="top"></span>
          </span>
          <div class="col-sm-9">
            <form:input path="name" type="text" class="form-control" id="portletName"/>
          </div>
        </div>
        <div class="form-group">
            <span class="col-sm-3 control-label">
              <label for="portletFname"><spring:message code="portlet.functional.name"/></label>
              <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.functional.name.tooltip'/>" data-toggle="tooltip" data-placement="top"></span>
            </span>
          <div class="col-sm-9">
            <form:input path="fname" type="text" class="form-control" id="portletFname"/>
          </div>
        </div>
        <div class="form-group">
            <span class="col-sm-3 control-label">
                <label for="portletDescription"><spring:message code="portlet.description"/></label>
                <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.description.tooltip'/>" data-toggle="tooltip" data-placement="top"></span>
            </span>
          <div class="col-sm-9">
            <form:input path="description" type="text" class="form-control" id="portletDescription"/>
          </div>
        </div>
        <div class="form-group">
            <span class="col-sm-3 control-label">
                <label for="portletTimeout"><spring:message code="portlet.timeout"/></label>
                <span class="glyphicon glyphicon-info-sign" title="<spring:message code='portlet.timeout.tooltip'/>" data-toggle="tooltip" data-placement="top"></span>
            </span>
          <div class="col-sm-9">
            <form:input path="timeout" type="text" class="form-control" id="portletTimeout"/>
          </div>
        </div>
        
			</div>
		</div> <!-- end: portlet-section -->
    
    <!-- Portlet Section -->
    <div class="portlet-section" role="region">
      <div class="titlebar">
        <h3 class="title" role="heading"><spring:message code="controls"/></h3>
      </div>
      <div class="content">
      
        <div class="form-group">
          <label for="portletControls" class="col-sm-3 control-label"><spring:message code="portlet.controls"/></label>
          <div class="col-sm-9">
            <div class="checkbox">
              <label for="hasHelp">
                <form:checkbox path="hasHelp"/>
                <spring:message code="hasHelp"/>
              </label>
            </div>
            <div class="checkbox">
              <label for="editable">
                <form:checkbox path="editable"/>
                <spring:message code="editable"/>
              </label>
            </div>
            <div class="checkbox">
              <label for="configurable">
                <form:checkbox path="configurable"/>
                <spring:message code="configurable"/>
              </label>
            </div>
            <div class="checkbox">
              <label for="hasAbout">
                <form:checkbox path="hasAbout"/>
                <spring:message code="hasAbout"/>
              </label>
            </div>
          </div>
        </div>
        
      </div>
    </div> <!-- end: portlet-section -->
    
    <!-- Buttons -->
    <div class="buttons form-group">
      <div class="col-sm-9 col-sm-offset-3">
        <c:choose>
          <c:when test="${ completed }">
            <input class="button btn btn-primary" type="submit" value="<spring:message code="review"/>" name="_eventId_review"/>
          </c:when>
          <c:otherwise>
            <input class="button btn btn-primary" type="submit" value="<spring:message code="continue"/>" name="_eventId_next"/>
            <input class="button btn" type="submit" value="<spring:message code="back"/>" name="_eventId_back"/>
          </c:otherwise>
        </c:choose>
        <input class="button btn btn-link" type="submit" value="<spring:message code="cancel"/>" name="_eventId_cancel"/>
      </div>
    </div>
    
    </form:form> <!-- End Form -->
            
	</div> <!-- end: portlet-content -->
        
</div> <!-- end: portlet -->

<script type="application/javascript">
    (function($, _, Backbone) {
        var selector = '#${n} .glyphicon-info-sign',
                titleSelector = '#portletTitle',
                nameSelector = '#portletName',
                fnameSelector = '#portletFname',
                nameTitleMismatchSelector = '.name-title-mismatch-warn',
                NameTitleModel,
                nameTitleModel,
                nameTitleMismatchTimer;

        /**
         * Define a backbone model to capture the name/title/fname values and
         * orchestrate their updates.  Changes on the page should update the model
         * and allow listeners attached to the model to handle propagating the changes
         * to the appropriate places in the UI
         */
        NameTitleModel = Backbone.Model.extend({
            defaults: {
                name: '',
                title: '',
                titleWarn: false,
                titleEdited: false,
                fname: '',
                fnameEdited: false,
                timeout: '5000'
            },


            /**
             * Initialize the model.
             */
            initialize: function() {
                this.on('change:title', this._updateTitle, this);
                this.on('change:name', this._updateName, this);
                this.on('change:fname', this._updateFName, this);
            },


            /**
             * import existing settings into the model.  Will attempt to determine if the
             * title and name match and if the title and fname are in sync.  If so, it will
             * enable auto-updates to those fields.  If not, changing title will not affect
             * the fields that appear to have been modified.
             */
            import: function(name, title, fname) {
                var calcFName;

                if (title && name) {
                    if (title !== name) {
                        titleEdited = true;
                    }
                }

                if (title && fname) {
                    calcFName = this._toFName(title);
                    if (fname !== calcFName) {
                        fnameEdited = true;
                    }
                }
            },


            /**
             * Do validation to check for name/title mismatches and set the 'titleWarn' attribute.
             */
            validate: function(attrs, options) {
                var finalAttrs = _.extend({}, this.attributes, attrs);
                this.set('titleWarn', finalAttrs.title !== finalAttrs.name);
            },


            /**
             * Given a title, calculate the fname to use.
             */
            _toFName: function(name) {
                var tmp = name.toLowerCase();
                return tmp ? tmp.replace(/[^a-z]+/g, '-') : '';
            },


            /**
             * Internal event for when 'title' changes.  Will try to update name and fname if
             * they have not been edited.
             */
            _updateTitle: function(model, value) {
                if (!model.get('nameEdited')) {
                    model.set('name', value, {validate: true});
                }

                if (!model.get('fnameEdited')) {
                    model.set('fname', this._toFName(value));
                }
            },


            /**
             * Internal event handler for when 'name' changes.
             */
            _updateName: function(model, value) {
                model.attributes.nameEdited = !(model.get('title') === value);
            },


            /**
             * Internal event handler for when fname changes.
             */
            _updateFName: function(model, value) {
                var calcFName = this._toFName(model.get('title'));
                model.attributes.fnameEdited = !(calcFName === value);
            }
        });

        // instantiate the model...
        nameTitleModel = new NameTitleModel();
        nameTitleModel.import($(nameSelector).val(), $(titleSelector).val(), $(fnameSelector).val());

        /* When the model changes, update the UI */
        nameTitleModel.on('change:title', function(model, value) {
            $(titleSelector).val(value);
        });

        nameTitleModel.on('change:name', function(model, value) {
            $(nameSelector).val(value);
        });

        nameTitleModel.on('change:fname', function(model, value) {
            $(fnameSelector).val(value);
        });

        nameTitleModel.on('change:titleWarn', function(model, value) {
            clearTimeout(nameTitleMismatchTimer);
            // don't re-render on every keystroke to avoid flashing.
            nameTitleMismatchTimer = setTimeout(function() {
                if (value) {
                    $(nameTitleMismatchSelector).slideDown('slow');
                } else {
                    $(nameTitleMismatchSelector).slideUp('slow');
                }
            }, 500);
        });

        // When the user enters values, update the model...
        $(titleSelector).keyup(function() {
            nameTitleModel.set('title', $(titleSelector).val(), {validate: true});
        });

        $(nameSelector).keyup(function() {
            nameTitleModel.set('name', $(nameSelector).val(), {validate: true});
        });

        $(fnameSelector).keyup(function() {
            nameTitleModel.set('fname', $(fnameSelector).val());
        });

        /* Enable the help tooltips */
        $(selector).bootstrapTooltip({
            container: 'body',
            trigger: 'click'
        });

        // clicking anywhere on the page should dismiss the currently visible
        // tooltip.
        $('body').click(function(evt) {
            var $target = $(evt.target);

            $(selector).each(function(idx, el) {
                if (!$(el).is($target)) {
                    $(el).bootstrapTooltip('hide');
                }
            });
        });
    })(up.jQuery, up._, up.Backbone);
</script>