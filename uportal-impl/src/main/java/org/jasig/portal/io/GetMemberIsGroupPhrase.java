/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
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