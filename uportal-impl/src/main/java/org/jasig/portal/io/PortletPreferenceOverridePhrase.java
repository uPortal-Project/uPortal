package org.jasig.portal.io;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class PortletPreferenceOverridePhrase implements Phrase {

    // Instance Members.
    private Phrase inpt;

    /*
     * Public API.
     */

    public static final Reagent INPT = new SimpleReagent("INPT", "descendant-or-self::text()",
                    ReagentType.PHRASE, String.class, "Either Y or N.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {INPT};
        return new SimpleFormula(PortletPreferenceOverridePhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.inpt = (Phrase) config.getValue(INPT);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

        String s = (String) inpt.evaluate(req, res);
        return s.equals("Y") ? "N" : "Y";

    }

}
