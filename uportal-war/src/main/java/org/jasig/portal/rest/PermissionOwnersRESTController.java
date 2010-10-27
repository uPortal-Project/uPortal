package org.jasig.portal.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.permission.target.IPermissionTargetProvider;
import org.jasig.portal.permission.target.IPermissionTargetProviderRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * PermissionOwnersRESTController provides a REST endpoing for permission owners
 * and activities.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
public class PermissionOwnersRESTController {

    private IPersonManager personManager;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private IPermissionOwnerDao permissionOwnerDao;
    
    @Autowired(required = true)
    public void setPermissionOwnerDao(IPermissionOwnerDao permissionOwnerDao) {
        this.permissionOwnerDao = permissionOwnerDao;
    }

    private IPermissionTargetProviderRegistry targetProviderRegistry;
    
    @Autowired(required = true)
    public void setPermissionTargetProviderRegistry(IPermissionTargetProviderRegistry registry) {
        this.targetProviderRegistry = registry;
    }

    /**
     * Provide a JSON view of all known permission owners registered with uPortal.
     * 
     * @param req
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/permissions/owners.json", method = RequestMethod.GET)
    public ModelAndView getOwners(
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to see permission owners
        if (!this.isAuthorized(req)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        // get a list of all currently defined permission owners
        List<IPermissionOwner> owners = permissionOwnerDao.getAllPermissionOwners();
        
        ModelAndView mv = new ModelAndView();
        mv.addObject("owners", owners);
        mv.setViewName("json");
        
        return mv;
    }
    
    /**
     * Provide a detailed view of the specified IPermissionOwner.  This view
     * should contain a list of the owner's defined activities.
     * 
     * @param ownerParam
     * @param req
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/permissions/owners/{owner}.json", method = RequestMethod.GET)
    public ModelAndView getOwners(
            @PathVariable("owner") String ownerParam,
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to see permission owners
        if (!this.isAuthorized(req)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        IPermissionOwner owner = null;
        
        if (StringUtils.isNumeric(ownerParam)) {
            Long id = Long.valueOf(ownerParam);
            owner = permissionOwnerDao.getPermissionOwner(id);
        } else {
            owner = permissionOwnerDao.getPermissionOwner(ownerParam);
        }
        
        // if the IPermissionOwner was found, add it to the JSON model
        if (owner != null) {
            ModelAndView mv = new ModelAndView();
            mv.addObject("owner", owner);
            mv.setViewName("json");            
            return mv;
        } 
        
        // otherwise return a 404 not found error code
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
    }
    
    /**
     * Provide a list of all registered IPermissionActivities.  If an optional
     * search string is provided, the returned list will be restricted to 
     * activities matching the query.
     * 
     * @param query     optional search query
     * @param request    
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/permissions/activities.json", method = RequestMethod.GET)
    public ModelAndView getActivities(
            @RequestParam(value="q", required=false) String query,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to see permission owners
        if (!this.isAuthorized(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        List<IPermissionActivity> activities = new ArrayList<IPermissionActivity>();
        Collection<IPermissionOwner> owners = permissionOwnerDao.getAllPermissionOwners();
        for (IPermissionOwner owner : owners) {
            for (IPermissionActivity activity : owner.getActivities()) {
                if (StringUtils.isBlank(query) || activity.getName().toLowerCase().contains(query)) {
                    activities.add(activity);
                }
            }
        }
        
        ModelAndView mv = new ModelAndView();
        mv.addObject("activities", activities);
        mv.setViewName("json");
        
        return mv;
    }

    /**
     * Return a list of targets defined for a particular IPermissionActivity 
     * matching the specified search query. 
     * 
     * @param activityId
     * @param query
     * @param req
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/permissions/{activity}/targets.json", method = RequestMethod.GET)
    public ModelAndView getTargets(@PathVariable("activity") Long activityId,
            @RequestParam("q") String query,
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to see permission owners
        if (!this.isAuthorized(req)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(activityId);
        IPermissionTargetProvider provider = targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey());
        
        SortedSet<IPermissionTarget> matchingTargets = new TreeSet<IPermissionTarget>();
        // add matching results for this target provider to the set
        Collection<IPermissionTarget> targets = provider.searchTargets(query);
        for (IPermissionTarget target : targets) {
            if ((StringUtils.isNotBlank(target.getName()) && target
                    .getName().toLowerCase().contains(query))
                    || target.getKey().toLowerCase().contains(query)) {
                matchingTargets.addAll(targets);
            }
        }

        ModelAndView mv = new ModelAndView();
        mv.addObject("targets", targets);
        mv.setViewName("json");
        
        return mv;
    }
    
    
    protected boolean isAuthorized(HttpServletRequest request) {

        // TODO: figure out how we want to actually handle permissions
        
        final IPerson person = personManager.getPerson((HttpServletRequest) request);
        if (person != null) {
            IAuthorizationService authServ = AuthorizationImpl.singleton();
            IAuthorizationPrincipal principal = authServ.newPrincipal((String) person.getAttribute(IPerson.USERNAME), IPerson.class);
            return principal.hasPermission("UP_PERMISSION", "VIEW_PERMISSIONS", "REST");
        }
        
        return false;
    }
    

}
