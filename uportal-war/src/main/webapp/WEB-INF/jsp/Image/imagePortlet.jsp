<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<p align="center">${ caption }</p>
<p align="center">
  <a href="${ link }">
    <img src="${ uri }" alt="${ alt }" width="${ width }" height="${ height }" border="${ border }"/>
  </a>
</p>
<p align="center"><font size="2">${ subcaption }</font></p>