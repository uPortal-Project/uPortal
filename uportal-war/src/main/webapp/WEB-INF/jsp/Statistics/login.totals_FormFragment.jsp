<%@ include file="/WEB-INF/jsp/include.jsp"%>
            <p>
                <form:label path="totalLogins"><spring:message code="total.logins"/></form:label>
                <form:checkbox path="totalLogins" />
            </p>
            <p>
                <form:label path="uniqueLogins"><spring:message code="unique.logins"/></form:label>
                <form:checkbox path="uniqueLogins" />
            </p>