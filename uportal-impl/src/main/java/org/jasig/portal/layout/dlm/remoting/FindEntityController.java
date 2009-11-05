package org.jasig.portal.layout.dlm.remoting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FindEntityController  extends AbstractController {

	private static final Log log = LogFactory.getLog(FindEntityController.class);
	private IGroupListHelper groupListHelper;
	private IPersonManager personManager;
	
	public FindEntityController() {
		// for security reasons, we only want to allow POST access to this
		// service
		this.setSupportedMethods(new String[]{"POST"});
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
		/* Make sure the user is an admin. */
		IPerson user = personManager.getPerson(request);
		if(!AdminEvaluator.isAdmin(user)) {
			throw new AuthorizationException("User " + user.getUserName() + 
					" not an administrator.");
		}
		
		String entityType = request.getParameter("entityType");
		String entityId = request.getParameter("entityId");

		if(StringUtils.isBlank(entityType)) {
			return new ModelAndView("jsonView", "error", "No entityType specified.");
		}
		
		if (StringUtils.isBlank(entityId)) {
			return new ModelAndView("jsonView", "error", "No entityId specified.");
		}

		JsonEntityBean result = groupListHelper.getEntity(entityType, entityId, true);

		return new ModelAndView("jsonView", "result", result);	
	}

	/**
	 * <p>For injection of the group list helper.</p>
	 * @param groupListHelper IGroupListHelper instance
	 */
	public void setGroupListHelper(IGroupListHelper groupListHelper) {
		this.groupListHelper = groupListHelper;
	}

	/**
	 * <p>For injection of the person manager.  Used for authorization.</p>
	 * @param personManager IPersonManager instance
	 */
	public void setPersonManager(IPersonManager personManager) {
		this.personManager = personManager;
	}
}
