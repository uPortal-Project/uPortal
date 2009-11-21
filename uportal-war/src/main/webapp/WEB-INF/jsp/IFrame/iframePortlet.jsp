<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>
<iframe src="${url}" height="${height}" frameborder="0" width="100%">
    This browser does not support inline frames.<br/> 
    <a href="${url}" target="_blank">Click here to view content</a> in a separate window.
</iframe>