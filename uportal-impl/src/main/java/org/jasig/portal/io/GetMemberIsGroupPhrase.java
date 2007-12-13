package org.jasig.portal.io;

import org.dom4j.Element;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class GetMemberIsGroupPhrase implements Phrase {

    // Instance Members.
    private Phrase element;

    /*
     * Public API.
     */

    public static final Reagent ELEMENT = new SimpleReagent("ELEMENT", "descendant-or-self::text()",
                    ReagentType.PHRASE, Element.class, "Element whose text is a member name.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ELEMENT};
        return new SimpleFormula(GetMemberIsGroupPhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.element = (Phrase) config.getValue(ELEMENT);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

        Element e = (Element) element.evaluate(req, res);
        return e.getName().equals("group") ? "T" : "F";

    }

}