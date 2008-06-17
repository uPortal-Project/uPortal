package org.jasig.portal.io;

import org.dom4j.Element;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class GetMemberServicePhrase implements Phrase {

    // Instance Members.
    private Phrase element;

    /*
     * Public API.
     */

    public static final Reagent ELEMENT = new SimpleReagent("ELEMENT", "descendant-or-self::text()",
                    ReagentType.PHRASE, Element.class, "Element whose text is a member name.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ELEMENT};
        return new SimpleFormula(GetMemberServicePhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.element = (Phrase) config.getValue(ELEMENT);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

        String rslt = null;

        Element e = (Element) element.evaluate(req, res);

        // We can (and must) cut & run if the element is a <literal> or a <channel>...
        if (e.getName().equals("literal") || e.getName().equals("channel")) {
            return "local";
        }

        try {

            Class[] leafTypes = new Class[] {IPerson.class, ChannelDefinition.class};
            for (int i=0; i < leafTypes.length && rslt == null; i++) {
                EntityIdentifier[] eis = GroupService.searchForGroups(e.getText(), IGroupConstants.IS, leafTypes[i]);
                if (eis.length == 1) {
                    // Match!
                    if (eis[0].getType() == IEntityGroup.class) {
                        IEntityGroup g = GroupService.findGroup(eis[0].getKey());
                        rslt = g.getServiceName().toString();
                    } else {
                        String msg = "The specified enttity is not a group:  " + e.getText();
                        throw new RuntimeException(msg);
                    }
                    break;
                } else if (eis.length > 1) {
                    String msg = "Ambiguous member name:  " + e.getText();
                    throw new RuntimeException(msg);
                }
            }

        } catch (Throwable t) {
            String msg = "Error looking up the specified member:  " + e.getText();
            throw new RuntimeException(msg, t);
        }

        if (rslt == null) {
            String msg = "The specified member was not found:  " + e.getText();
            throw new RuntimeException(msg);
        }

        return rslt;

    }

}