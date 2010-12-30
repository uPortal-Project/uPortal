package org.jasig.portal.portlet.registry;

import java.util.Set;

import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;

public interface IPortletCategoryRegistry {

	/**
	 * Makes one category a child of another.
	 * @param child the source category
	 * @param parent the destination category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void addCategoryToCategory(PortletCategory child,
			PortletCategory parent);

	/**
	 * Deletes a portlet category.
	 * @param category the portlet category to delete
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void deletePortletCategory(PortletCategory category);

	/**
	 * Gets all child portlet categories for a parent category.
	 * @return portletCategories the children categories
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public Set<PortletCategory> getAllChildCategories(
			PortletCategory parent);

	/**
	 * Gets all child portlet definitions for a parent category.
	 * @return portletDefinitions the children portlet definitions
	 * @throws java.sql.SQLException
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public Set<IPortletDefinition> getAllChildPortlets(
			PortletCategory parent);

	/**
	 * Gets an existing portlet category.
	 * @param portletCategoryId the id of the category to get
	 * @return portletCategory the portlet category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public PortletCategory getPortletCategory(String portletCategoryId);

	/**
	 * Gets all child portlet categories for a parent category.
	 * @return portletCategories the children categories
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public Set<PortletCategory> getChildCategories(PortletCategory parent);

	/**
	 * Gets all child portlet definitions for a parent category.
	 * @return portletDefinitions the children portlet definitions
	 * @throws java.sql.SQLException
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public Set<IPortletDefinition> getChildPortlets(PortletCategory parent);

	/**
	 * Gets the immediate parent categories of this category.
	 * @return parents, the parent categories.
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public Set<PortletCategory> getParentCategories(PortletCategory child);

	/**
	 * Gets the immediate parent categories of this portlet definition.
	 * @return parents, the parent categories.
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public Set<PortletCategory> getParentCategories(IPortletDefinition child);

	/**
	 * Gets top level portlet category
	 * @return portletCategories the new portlet category
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public PortletCategory getTopLevelPortletCategory();

	/**
	 * Creates a new portlet category with the specified values.
	 * @param name the name of the category
	 * @param description the name of the description
	 * @param creatorId the id of the creator or system
	 * @return portletCategory the new portlet category
	 * @throws GroupsException
	 */
	public PortletCategory createPortletCategory(String name,
			String description, String creatorId);

	/**
	 * Makes one category a child of another.
	 * @param child the category to remove
	 * @param parent the category to remove from
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void removeCategoryFromCategory(PortletCategory child,
			PortletCategory parent) throws GroupsException;

	/**
	 * Persists a portlet category.
	 * @param category the portlet category to persist
	 * @throws org.jasig.portal.groups.GroupsException
	 */
	public void updatePortletCategory(PortletCategory category);

}