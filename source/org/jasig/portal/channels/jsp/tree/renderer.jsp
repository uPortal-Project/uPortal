<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %><%-- 

This JSP renders rows that blend together to portray a heirarchical view. Such a 
view can be rendered as a containment hierarchy much like mathemantical or boolean
expressions appear or as a tree of nodes like is seen in Windows Explorer. When
viewing in containment mode an additional row is appended after each container 
node's children showing a container closing image. Branches can be shown in 
both tree and containment views. A single pass through this JSP in containment
mode contributes two UI rows for container nodes and one for non-container 
nodes. Nested between the two container rows will be rows generated
by recursive calls to this JSP to render each child of a container. The paths
to recursibely call this JSP are fully qualified so that this JSP can be 
included by another JSP to embedd the resulting tree within that JSP's output.

Model expectations of this JSP are:
- requestScope will have an attribute 'treeModel'
that is an instance of org.jasig.portal.channels.jsp.tree.Model. See that class
for related objects available in this JSP via the model's accessors.

--%><c:set var="model" value="${requestScope.treeModel}"/><%--

==== if rendering has not started then set the root as the node to be rendered
==== and generate the containing html table.

--%><c:choose><%--
--%><c:when test="${model.startRendering}"><%-- rendering is starting
 --%><c:set target="${model}" property="node" value="${model.root}" /><%--
 --%><c:set var="isRenderingRoot" value="${true}" /><%--
 --%><table cellpadding="0" cellspacing="0" border="0" width="100%"><%--
--%></c:when><%--
--%><c:otherwise><%-- in the midst of rendering
 --%><c:set var="isRenderingRoot" value="${false}" /><%--
--%></c:otherwise><%--
--%></c:choose><%--

==== create local variable to hold current node since this will change on the model
==== as rendering proceeds with recurrent calls to this page using that same model.

--%><c:set var="node" value="${model.node}"/><%--

==== START of current node row
--%><tr><%--

==== insert action labels delegating to the plugged-in label renderer 

 --%><c:set target="${model}" property="labelType" value="${model.actionLabelType}" /><%--
 --%><c:forEach items="${model.config.domainActions}" var="currentAction"><%--
  --%><td nowrap="nowrap" style="padding-left:10px;padding-right=10px"><%--
 --%><c:set target="${model}" property="domainAction" value="${currentAction}" /><%--
   --%><c:import url="${model.config.labelRenderer}" /><%--
  --%></td><%--
 --%></c:forEach><%--
 --%><td>&nbsp;</td><%--

==== start row with indentation images that precede the node's parent. Note that
==== the root node by definition should render as having no parent so the list 
==== will be empty. 

--%><td width="100%"><%--
 --%><table border="0" cellspacing="0" cellpadding="0"><%--
  --%><tr><%--
   --%><c:forEach items="${model.indentImages}" var="image"><%--
    --%><c:choose><%--
    --%><c:when test="${image == 'skippedChild'}"><%--
     --%><td valign="top" style="background-image: url(<c:out value='${model.config.images.skippedChildRpt}'/>); background-repeat: repeat-y; background-position: top left;"><%--
     --%><img src="<c:out value='${model.config.images.noBranch}'/>"/></td><%--
   --%></c:when><%--
    --%><c:when test="${image == 'middleChild' 
    || image == 'middleBranchExpanded'
    || image == 'middleBranchCollapsed'}"><%--
     --%><td valign="top" style="background-image: url(<c:out value='${model.config.images.skippedChildRpt}'/>); background-repeat: repeat-y; background-position: top left;"><img src="<c:out value='${model.config.images[image]}'/>"/></td><%--
   --%></c:when><%--
    --%><c:otherwise><%--
     --%><td valign="top" ><img src="<c:out value='${model.config.images[image]}'/>"/></td><%--
   --%></c:otherwise><%--
    --%></c:choose><%--
    --%></c:forEach><%-- 

==== handle the node's immediately preceding branch or indentation image 
==== immediately following the parent's indentation level since this changes for 
==== each child node based on if they have a following sibling. Note that the root 
==== node has no preceding branch. 

--%><c:if test="${! isRenderingRoot && node.isAspect == false}"><%--
 --%><c:choose><%--
  --%><c:when test="${model.config.showBranches}"><%--
   --%><c:choose><%--
    --%><c:when test="${node.hasNextSibling}"><%--
     --%><c:choose><%--
      --%><c:when test="${! node.hasChildren}"><%--
--%><td valign="top" style="background-image: url(<c:out value='${model.config.images.skippedChildRpt}'/>); background-repeat: repeat-y; background-position: top left;"><%--
--%><img src="<c:out value='${model.config.images.middleChild}'/>"/><%--
--%></td><%--
      --%></c:when><%--
      --%><c:when test="${node.isExpanded}"><%--
--%><td valign="top" style="background-image: url(<c:out value='${model.config.images.skippedChildRpt}'/>); background-repeat: repeat-y; background-position: top left;"><%--
--%><a href="<c:out value='${model.treeUrls.collapse[node.id]}'/>"><%--
--%><img border="0" src="<c:out value='${model.config.images.middleBranchExpanded}'/>"/><%--
--%></a><%--
--%></td><%--
      --%></c:when><%--
      --%><c:otherwise><%--
--%><td valign="top" style="background-image: url(<c:out value='${model.config.images.skippedChildRpt}'/>); background-repeat: repeat-y; background-position: top left;"><%--
--%><a href="<c:out value='${model.treeUrls.expand[node.id]}'/>"><%--
--%><img border="0" src="<c:out value='${model.config.images.middleBranchCollapsed}'/>"/><%--
--%></a><%--
--%></td><%--
      --%></c:otherwise><%--
     --%></c:choose><%--
    --%></c:when><%--
    --%><c:otherwise><%--
     --%><c:choose><%--
      --%><c:when test="${! node.hasChildren}"><%--
--%><td valign="top" ><%--
--%><img src="<c:out value='${model.config.images.lastChild}'/>"/><%--
--%></td><%--
      --%></c:when><%--
      --%><c:when test="${node.isExpanded}"><%--
--%><td valign="top" ><%--
--%><a href="<c:out value='${model.treeUrls.collapse[node.id]}'/>"><%--
--%><img border="0" src="<c:out value='${model.config.images.lastBranchExpanded}'/>"/><%--
--%></a><%--
--%></td><%--
      --%></c:when><%--
      --%><c:otherwise><%--
--%><td valign="top" ><%--
--%><a href="<c:out value='${model.treeUrls.expand[node.id]}'/>"><%--
--%><img border="0" src="<c:out value='${model.config.images.lastBranchCollapsed}'/>"/><%--
--%></a><%--
--%></td><%--
      --%></c:otherwise><%--
     --%></c:choose><%--
    --%></c:otherwise><%--
   --%></c:choose><%--
  --%></c:when><%--
  --%><c:otherwise><%--
--%><img src="<c:out value='${model.config.images.noBranch}'/>"/><%--
 --%></c:otherwise><%--
 --%></c:choose><%--
 --%></c:if><%-- 

==== if viewing containment, inject container-start image for containers

--%><c:if test="${node.canContainChildren and model.config.viewContainment}"><%--
 --%><td valign="top"><img src="<c:out value='${model.config.images.startOfContainer}'/>"/></td><%--
--%></c:if><%-- 

==== add in the node's labeling by delegating to the plugged-in renderer

 --%><c:choose><%--
  --%><c:when test="${node.isAspect}"><%--
   --%><c:set target="${model}" property="labelType" value="${model.aspectLabelType}" /><%--
 --%></c:when><%--
  --%><c:otherwise><%--
   --%><c:set target="${model}" property="labelType" value="${model.nodeLabelType}" /><%--
 --%></c:otherwise><%--
 --%></c:choose><%--
 --%><td valign="middle"><c:import url="${model.config.labelRenderer}" /></td><%--
 
==== add in the node's aspects expand/collapse image if applicable

 --%><c:if test="${node.hasAspects}"><%--
 --%><c:choose><%--
  --%><c:when test="${node.isShowingAspects}"><%--
--%><td valign="top"><%--
--%><a href="<c:out value='${model.treeUrls.hideAspects[node.id]}'/>"><%--
--%><img border="0" align="bottom" src="<c:out value='${model.config.images.hideAspects}'/>" /><%--
--%></a><%--
--%></td><%--
  --%></c:when><%--
  --%><c:otherwise><%--
--%><td valign="top"><%--
--%><a href="<c:out value='${model.treeUrls.showAspects[node.id]}'/>"><%--
--%><img border="0" align="bottom" src="<c:out value='${model.config.images.showAspects}'/>" /><%--
--%></a><%--
--%></td><%--
  --%></c:otherwise><%--
 --%></c:choose><%--
 --%></c:if><%--
--%></tr><%--
--%></table><%--
 --%></td><%--
 
==== END of current node's UI row 
--%></tr><%--

==== START OF CHILD AND ASPECT RENDERING

--%><c:if test="${node.isExpanded || node.isShowingAspects}"><%--
 
==== inject into indentation images list appropriate image for children. This is
==== only done by non-root parents since the root node does not have a parent
==== and the list is meant to hold indentation images for the parent of a node
==== being rendered. 

 --%><c:if test="${! isRenderingRoot}"><%--
  --%><c:choose><%--
   --%><c:when test="${model.config.showBranches}"><%--
    --%><c:choose><%--
     --%><c:when test="${node.hasNextSibling}"><%--
      --%><c:set target="${model}" property="pushIndent" value="skippedChild" /><%--
     --%></c:when><%--
     --%><c:otherwise><%--
      --%><c:set target="${model}" property="pushIndent" value="noBranch" /><%--
    --%></c:otherwise><%--
    --%></c:choose><%--
   --%></c:when><%--
   --%><c:otherwise><%--
    --%><c:set target="${model}" property="pushIndent" value="noBranch" /><%--
  --%></c:otherwise><%--
  --%></c:choose><%--
 --%></c:if><%--
  
==== LOOP for aspect rendering contains these steps 
==== 1) push the branch image for aspects since they can't tell like children
        since children base theirs on if they have a next sibling whereas
        aspects having a following aspect makes no difference on its branch 
        image. It is solely dependant on if the parent is expanded or not.
==== 2) set the model's current node to the child
==== 3) make call to this jsp for rendering each aspect
==== 4) pop the branch image set for aspects.

 --%><c:if test="${node.isShowingAspects}"><%--
 --%><c:choose><%--
  --%><c:when test="${node.isExpanded}"><%--
   --%><c:set target="${model}" property="pushIndent" value="skippedChild" /><%--
  --%></c:when><%--
  --%><c:otherwise><%--
   --%><c:set target="${model}" property="pushIndent" value="noBranch" /><%--
  --%></c:otherwise><%--
 --%></c:choose><%--
 --%><c:forEach items="${node.aspects}" var="aspect" varStatus="status"><%--
  --%><c:set target="${model}" property="node" value="${aspect}" /><%--
   --%><c:import url="${model.config.renderer}" /><%--
 --%></c:forEach><%--
  --%><c:set var="poppedImage" value="${model.popIndent}" /><%--
 --%></c:if><%--
 
==== LOOP for Child rendering contains these steps 
==== 1) for branch rendering tell each child if it has a next sibling 
==== 2) set the model's current node to the child
==== 3) make call to this jsp for rendering that child

 --%><c:if test="${node.isExpanded}"><%--
 --%><c:forEach items="${node.children}" var="child" varStatus="status"><%--
  --%><c:set target="${child}" property="hasNextSibling" value="${not status.last}" /><%--
  --%><c:set target="${model}" property="node" value="${child}" /><%--
   --%><c:import url="${model.config.renderer}" /><%--
 --%></c:forEach><%--
 --%></c:if><%--
 
 
==== pop the image from the intentations list that was injected for node's children
==== unless this is the root node since root doesn't inject any indent image.
 
 --%><c:if test="${! isRenderingRoot}"><%--
  --%><c:set var="poppedImage" value="${model.popIndent}" /><%--
 --%></c:if><%--
 
==== END OF CHILD AND ASPECT RENDERING 
--%></c:if><%--
 
==== If viewing containment then after all children rendered need a row to close
==== any container node being rendered

--%><c:if test="${node.canContainChildren and model.config.viewContainment}"><%--
--%><tr><%--
==== START OF closing container row

==== fill action link columns with empty filler
 --%><td>&nbsp;</td><%--
 --%><td>&nbsp;</td><%--
 --%><td>&nbsp;</td><%--
 --%><td>&nbsp;</td><%--
 --%><td>&nbsp;</td><%--
 --%><td>&nbsp;</td><%--

==== start row with indentation images that precede the node's parent.

--%><td width="100%"><%--
 --%><c:forEach items="${model.indentImages}" var="image"><%--
  --%><img src="<c:out value='${model.config.images[image]}'/>"/><%--
 --%></c:forEach><%-- 

==== handle the node's immediately preceding branch or indentation image 
==== immediately following the parent's indentation level since this changes for 
==== each child node based on if they have a following sibling. Note that the root 
==== node has no preceding branch. 


--%><c:if test="${! isRenderingRoot}"><%--    
 --%><c:choose><%--   
  --%><c:when test="${model.config.showBranches}"><%--
   --%><c:choose><%--
    --%><c:when test="${node.hasNextSibling}"><%--
     --%><img align="bottom" src="<c:out value='${model.config.images.skippedChild}'/>" /><%--
    --%></c:when><%--
    --%><c:otherwise><%--
     --%><img align="bottom" src="<c:out value='${model.config.images.noBranch}'/>" /><%--
    --%></c:otherwise><%--
   --%></c:choose><%--
  --%></c:when><%--
  --%><c:otherwise><%--
   --%><img align="bottom" src="<c:out value='${model.config.images.noBranch}'/>" /><%--
 --%></c:otherwise><%--
 --%></c:choose><%--
--%></c:if><%-- 

==== and finally append container-closing image
 --%><img align="bottom" src="<c:out value='${model.config.images.endOfContainer}'/>"/><%--

==== 	END OF closing container row
--%></c:if><%--

==== Now see if this page rendered the root element. If so then we have 
==== finished rendering the tree and returned to the top node and must also 
==== close the outer most table tag.

--%><c:if test="${isRenderingRoot}"><%--
 --%><c:set target="${model}" property="isRendering" value="${false}" /><%--
--%></table><%--
--%></c:if>