package org.apereo.portal.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.portlet.container.services.PortletPreferencesFactory;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PortletPrefsRESTController {

    protected final Log logger = LogFactory.getLog(getClass());
    private ObjectMapper mapper;

    private IPersonManager personManager;

    private final boolean DEFINITION_MODE=true;
    private final boolean ENTITY_MODE=false;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private IPortletDefinitionDao portletDao;

    @Autowired
    public void setPortletDao(IPortletDefinitionDao portletDao) {
        this.portletDao = portletDao;
    }

    private IPortletEntityRegistry portletEntityRegistry;

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry){
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    private TransactionOperations transactionOperations;

    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    private PortletPreferencesFactory portletPreferencesFactory;

    @Autowired
    @Qualifier("portletPreferencesFactory")
    public void setPortletPreferencesFactoryAPI(PortletPreferencesFactory portletPreferencesFactory){
        this.portletPreferencesFactory=portletPreferencesFactory;
    }



    public PortletPrefsRESTController() {
        this.mapper=new ObjectMapper();
    }

    @RequestMapping(value = "/prefs/getentity/{fname}", method = RequestMethod.GET)
    public ResponseEntity getEntity(HttpServletRequest request, HttpServletResponse response,
                                    @ApiParam(value = "The portlet fname to get entity for")
                                    @PathVariable(value = "fname") String fname) {
        try{
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if(portletDefinition==null){
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity = portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinition.getPortletDefinitionId());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{\""+portletDefinition.getFName()+"\": {");
            stringBuilder.append(" \"layoutNodeID\": \""+entity.getLayoutNodeId()+"\",");
            stringBuilder.append(" \"userID\": \""+entity.getUserId()+"\",");
            stringBuilder.append(" \"entityID\": \""+entity.getPortletEntityId()+"\",");
            stringBuilder.append(" \"windowStates\": [");
            boolean comma=false;
            for(Map.Entry<Long, WindowState> entry : entity.getWindowStates().entrySet()){
                stringBuilder.append(" { \"stylesheetID\": \"");
                stringBuilder.append(entry.getKey());
                stringBuilder.append("\", \"windowState\": \"");
                stringBuilder.append(entry.getValue().toString());
                stringBuilder.append("\"},");
                comma=true;
            }
            if (comma) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            stringBuilder.append(" ] } }");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(stringBuilder.toString());
        }catch(IllegalArgumentException e){
            if(e.getMessage().contains("No portlet definition found for id '")){
                String error="ERROR: User not authorized to access portlet '"+fname+"'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //return ResponseEntity.badRequest().build();
    }


    //returns the entity composite preferences for the portlet in question
    @RequestMapping(value = "/prefs/getprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getCompositePrefs(HttpServletRequest request, HttpServletResponse response,
                                            @ApiParam(value = "The portlet fname to find preferences for")
                                            @PathVariable(value = "fname") String fname) {

        try {

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if(portletDefinition==null){
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity = portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinition.getPortletDefinitionId());
            //config mode of true is definition, false for entity (guest or user)
            boolean config=ENTITY_MODE;
            PortletPreferences prefs=portletPreferencesFactory.createAPIPortletPreferences(request,entity,false,config);

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(prefs.getMap()));

        } catch(IllegalArgumentException e){
            if(e.getMessage().contains("No portlet definition found for id '")){
                String error="ERROR: User not authorized to access portlet '"+fname+"'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("threw error trying to retrieve composite preferences",e);
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //maybe get rid of this one
    @RequestMapping(value = "/prefs/getentityonlyprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getEntityOnlyPrefs(HttpServletRequest request, HttpServletResponse response,
                                             @ApiParam(value = "The portlet fname to find preferences for")
                                             @PathVariable(value = "fname") String fname) {
        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if(portletDefinition==null){
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity = portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinition.getPortletDefinitionId());

            PortletPreferences defprefs=portletPreferencesFactory.createAPIPortletPreferences(request,entity,false,DEFINITION_MODE);
            PortletPreferences entprefs=portletPreferencesFactory.createAPIPortletPreferences(request,entity,false,ENTITY_MODE);
            Map<String, String[]> eprefs=getEntityOnly(entprefs.getMap(),defprefs.getMap());

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(eprefs));
        } catch(IllegalArgumentException e){
            if(e.getMessage().contains("No portlet definition found for id '")){
                String error="ERROR: User not authorized to access portlet '"+fname+"'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //get the entity preferences by comparing the entity with the defintion and returning the entity ones that are different
    private Map<String, String[]> getEntityOnly(Map<String, String[]> entity, Map<String, String[]> definition){
        Map<String, String[]> result=new HashMap<>();
        for(Map.Entry<String,String[]> entry : entity.entrySet()){
            String key=entry.getKey();
            String[] value=entry.getValue();
            if(!definition.containsKey(key)){
                //the entity has something the definition does not
                result.put(key,value);
            }else{
                //they both have it so compare the two
                String[] defvalues = definition.get(key);
                if(value==null){
                    if(defvalues != null){
                        result.put(key,value);
                    }//otherwise they're both null so don't add
                }else{
                    if(!Arrays.equals(value, defvalues)){
                        result.put(key,value);
                    }//otherwise they're the same so don't add
                }
            }
        }
        return result;
    }

    //technically I guess we could get the full discriptor set by
    /*  final PortletDefinition portletDescriptor =
                this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
        final Preferences descriptorPreferences = portletDescriptor.getPortletPreferences();
        for (final Preference preference : descriptorPreferences.getPortletPreferences()) {
            final IPortletPreference preferenceWrapper = new PortletPreferenceImpl(preference);
            basePortletPreferences.put(preferenceWrapper.getName(), preferenceWrapper);
        }
    */
    //without needing to do the compare

    //get the discriptor only preferences by comparing the entity with the defintion and returning the discriptor ones that are missing
    private Map<String, String[]> getDescriptorOnly(Map<String, String[]> descriptor, List<IPortletPreference> definition) {
        Map<String, String[]> defprefs=new HashMap<>();
        Map<String, String[]> result= new HashMap<>();
        for(IPortletPreference p:definition){
            defprefs.put(p.getName(),p.getValues());
        }
        for(Map.Entry<String, String[]> entry : descriptor.entrySet()){
            if(!defprefs.containsKey(entry.getKey())){
                result.put(entry.getKey(),entry.getValue());
            }
        }
        return result;
    }

    //must be authenticated and a portal administrator
    @RequestMapping(value = "/prefs/getdefinitionprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getDefinitionPrefs(HttpServletRequest request, HttpServletResponse response,
                                             @ApiParam(value = "The portlet fname to find preferences for")
                                             @PathVariable(value = "fname") String fname) {
        HttpSession session = request.getSession(false);
        if(session == null){
            return new ResponseEntity<>("",HttpStatus.NOT_FOUND);
        }
        final IPerson person = personManager.getPerson(request);
        if(person.isGuest()){
            logger.warn("Guest user tried to access getDefinitionPrefs. Possible hacking attempt.");
            return new ResponseEntity<>("ERROR: guest cannot use this action.",HttpStatus.UNAUTHORIZED);
        }
        else if(!person.getSecurityContext().isAuthenticated()){
            logger.warn("Person: {"+person.getUserName()+"} tried to access getDefinitionPrefs while not authenticated. Possible hacking attempt.");
            return new ResponseEntity<>("ERROR: must be logged in to use this action.",HttpStatus.UNAUTHORIZED);
        }

        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if(portletDefinition==null){
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }

            if(!canConfigure(person,portletDefinition.getPortletDefinitionId())){
                logger.warn("Person: {"+person.getUserName()+"} tried to access getDefinitionPrefs for portlet: {"+fname+"} without configuration permissions. Possible hacking attempt.");
                return new ResponseEntity<>("ERROR: user is not authorized to perform this action",HttpStatus.UNAUTHORIZED);
            }

            List<IPortletPreference> prefs = portletDefinition.getPortletPreferences();//this should probably return the descriptor prefs too...

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(prefMap(prefs)));
        }catch(Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
//        final org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        final String username = (String) authentication.getPrincipal();
//        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    }

    private boolean canConfigure(IPerson user, IPortletDefinitionId portletDefinitionId){
        final IAuthorizationPrincipal principal =
            AuthorizationPrincipalHelper.principalFromUser(user);
        return principal.canConfigure(portletDefinitionId.toString());//this might backfire, will have to figure out exactly how it's figuring out if it can be configured
    }

    private Map<String,String[]> prefMap(List<IPortletPreference> prefs){
        Map<String,String[]> map=new HashMap<>();
        for(IPortletPreference p : prefs){
            map.put(p.getName(),p.getValues());
        }
        return map;
    }


    @RequestMapping(value = "/prefs/putdefinitionprefs", method = RequestMethod.PUT)
    public ResponseEntity putDefinitionPrefs(HttpServletRequest request, HttpServletResponse response,
                                             @ApiParam(value = "The portlet fname to store preferences for")
                                             @RequestParam(value = "fname", required=true) String fname,
                                             @ApiParam(value = "the entity preferences to be stored in a single json object ex. " +
                                                 "{\"key1\":\"value1\",\"key2\":\"value2\", ...}. " +
                                                 "Values can be strings, booleans, numbers, null, or a jsonarray of values " +
                                                 "(objects can be stored as a value as serialized strings). " +
                                                 "This will overwrite any currently saved preferences for the portlet" +
                                                 " with the same key as the pairs put in.")
                                             @RequestBody String body) {

        HttpSession session = request.getSession(false);
        if(session == null){
            return new ResponseEntity<>("",HttpStatus.NOT_FOUND);
        }
        final IPerson person = personManager.getPerson(request);
        if(person.isGuest()){
            return new ResponseEntity<>("ERROR: guests may not use this action.",HttpStatus.FORBIDDEN);
        }
        else if(!person.getSecurityContext().isAuthenticated()){
            return new ResponseEntity<>("ERROR: must be logged in to use this action.",HttpStatus.UNAUTHORIZED);
        }

        try {
            JsonNode map = mapper.readTree(body);

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if(portletDefinition==null){
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity = portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinition.getPortletDefinitionId());

            if(!canConfigure(person,entity.getPortletDefinitionId())){
                return new ResponseEntity<>("ERROR: user is not authorized to perform this action",HttpStatus.FORBIDDEN);
            }
            //config mode of true is definition
            PortletPreferences prefs=portletPreferencesFactory.createAPIPortletPreferences(request,entity,false,DEFINITION_MODE);

            boolean stored = storePrefs(prefs,map);
            return ResponseEntity.ok().body(stored);

        } catch(IllegalArgumentException e){
            if(e.getMessage().contains("No portlet definition found for id '")){
                String error="ERROR: User not authorized to access portlet '"+fname+"'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("threw error trying to parse preferences",e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("threw error trying to store preferences",e);
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }



    @RequestMapping(value = "/prefs/putprefs", method = RequestMethod.PUT)
    public ResponseEntity putEntityPrefs(HttpServletRequest request, HttpServletResponse response,
                                         @ApiParam(value = "The portlet fname to store preferences for")
                                         @RequestParam(value="fname", required=true) String fname,
                                         @ApiParam(value = "the entity preferences to be stored in a single json object ex. " +
                                             "{\"key1\":\"value1\",\"key2\":\"value2\", ...}. " +
                                             "Values can be strings, booleans, numbers, null, or a jsonarray of values " +
                                             "(objects can be stored as a value as serialized strings). " +
                                             "This will overwrite any currently saved preferences for the portlet" +
                                             " with the same key as the key passed in.")
                                         @RequestBody String body) {
        try {
            JsonNode map = mapper.readTree(body);

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if(portletDefinition==null){
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity = portletEntityRegistry.getOrCreateDefaultPortletEntity(request, portletDefinition.getPortletDefinitionId());
            //configMode of false is Entity
            PortletPreferences prefs=portletPreferencesFactory.createAPIPortletPreferences(request,entity,false,ENTITY_MODE);

            boolean stored = storePrefs(prefs,map);
            return ResponseEntity.ok().body(stored);

        } catch(IllegalArgumentException e){
            if(e.getMessage().contains("No portlet definition found for id '")){
                String error="ERROR: User not authorized to access portlet '"+fname+"'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("threw error trying to parse preferences",e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch(PreferenceException e){
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(ReadOnlyException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
            logger.error("threw error trying to store preferences",e);
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //process the
    private boolean storePrefs(PortletPreferences prefs, JsonNode map)throws Exception{
        if(prefs==null){
            return false;
        }

        try{
            Iterator nodes = map.fields();
            Map<String,String[]> tempMap=new HashMap<>();
            while(nodes.hasNext()){
                Map.Entry<String,JsonNode> entry=(Map.Entry<String,JsonNode>)nodes.next();
                String key=entry.getKey();
                JsonNode node=entry.getValue();
                if(node.isValueNode()){
                    String[] temp={node.asText()};
                    tempMap.put(key,temp);
                }else if(node.isArray()){
                    List<String> array=new ArrayList<String>();
                    for(JsonNode innerNode:node){
                        if(innerNode.isValueNode()){
                            array.add(innerNode.asText());
                        }else{
                            throw new PreferenceException("ERROR: preference arrays must only contain strings, numbers, booleans, or null");
                        }
                    }
                    tempMap.put(key,array.toArray(new String[array.size()]));
                }else{
                    throw new PreferenceException("ERROR: preferences must be strings, numbers, booleans, null, or arrays of strings, numbers, booleans, or nulls");
                }
            }
            for(Map.Entry<String,String[]> entry : tempMap.entrySet()){
                prefs.setValues(entry.getKey(),entry.getValue());
            }
            prefs.store();
            return true;

        }catch(Exception e){
            throw e;
        }
    }

    private class PreferenceException extends Exception{

        public PreferenceException(){
            super();
        }

        public PreferenceException(String message){
            super(message);
        }

        public PreferenceException(String message, Exception cause){
            super(message,cause);
        }

    }

}


