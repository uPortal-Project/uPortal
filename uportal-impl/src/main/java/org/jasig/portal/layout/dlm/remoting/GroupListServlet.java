package org.jasig.portal.layout.dlm.remoting;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;

public class GroupListServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(GroupListServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/xml; charset=UTF-8");
		response.getWriter()
				.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		response.getWriter().print("<groups>");

		
		Class clazz = null;
		String classParam = request.getParameter("groupType");
		if ("person".equals(classParam)) {
			clazz = IPerson.class;
		} else {
			clazz = IChannelDefinition.class;
		}
		
		String groupKey = request.getParameter("groupKey");
		if (groupKey == null) {
			IEntityGroup baseRootGroup = GroupService.getRootGroup(clazz);
			groupKey = baseRootGroup.getKey();
		}
		IEntityGroup rootGroup = GroupService.findGroup(groupKey);
		String search = request.getParameter("searchTerm");
		if (search != null){
			// method 4: "contains"
			EntityIdentifier[] identifiers = GroupService.searchForEntities(search, 4, clazz);
			for (int i = 0; i < identifiers.length; i++) {
				try {
					if (identifiers[i].getType().equals(IPerson.class)) {
						IEntityNameFinder finder = EntityNameFinderService.instance()
							.getNameFinder(IPerson.class);
						response.getWriter().print("<person key=\"" + identifiers[i].getKey() 
							+"\">" + finder.getName(identifiers[i].getKey()) + "</person>");
					} else {
						IEntityNameFinder finder = EntityNameFinderService.instance()
							.getNameFinder(IEntityGroup.class);
						response.getWriter().print("<group key=\"" + identifiers[i].getKey() 
								+"\">" + finder.getName(identifiers[i].getKey()) + "</group>");
					}
				} catch (Exception e) {
				}
			}
		} else {
			
			response.getWriter().print("<rootGroup key=\"" + rootGroup.getKey() + "\">");
			response.getWriter().print("<name>" + rootGroup.getName() + "</name>");
			response.getWriter().print("<children>");
			for (Iterator groupIter = rootGroup.getMembers(); groupIter.hasNext();) {
				IGroupMember group = (IGroupMember) groupIter.next();
				if (group instanceof IEntityGroup) {
					IEntityGroup subgroup = (IEntityGroup) group;
					response.getWriter().print(
							"<group key=\"" + subgroup.getKey() + "\">"
									+ subgroup.getName() + "</group>");
				} else if (group instanceof IEntity){
					IEntity entity = (IEntity) group;
					EntityIdentifier identifier = entity.getUnderlyingEntityIdentifier();
					if (identifier.getType().equals(IPerson.class)) {
						try {
							response.getWriter().print("<person key=\"" + identifier.getKey() + "\">"
									+ EntityNameFinderService.instance()
									.getNameFinder(IPerson.class).getName(identifier.getKey()) + "</person>");
						} catch (GroupsException e) {
							log.warn("Failed to find name for person " + identifier.getKey(), e);
						} catch (Exception e) {
							log.warn("Failed to find name for person " + identifier.getKey(), e);
						}
					}
				} else {
					log.warn("Non-group of type " + group.getClass());
				}
			}
			response.getWriter().print("</children>");
			response.getWriter().print("</rootGroup>");
		}

		response.getWriter().print("</groups>");
		return;

	}
	
}
