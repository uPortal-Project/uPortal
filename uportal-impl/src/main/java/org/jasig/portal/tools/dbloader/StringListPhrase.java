package org.jasig.portal.tools.dbloader;

import java.util.Arrays;

import org.danann.cernunnos.EntityConfig;
import org.danann.cernunnos.Formula;
import org.danann.cernunnos.Phrase;
import org.danann.cernunnos.Reagent;
import org.danann.cernunnos.ReagentType;
import org.danann.cernunnos.SimpleFormula;
import org.danann.cernunnos.SimpleReagent;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;

public class StringListPhrase implements Phrase {

    // Instance Members.
    private Phrase items;

    /*
     * Public API.
     */

    public static final Reagent ITEMS = new SimpleReagent("ITEMS", "descendant-or-self::text()",
            		ReagentType.PHRASE, String.class, "Comma-separated list of strings.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {ITEMS};
        return new SimpleFormula(StringListPhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.items = (Phrase) config.getValue(ITEMS);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

    	String s = (String) items.evaluate(req, res);
    	String[] tokens = s.split(",");
    	return Arrays.asList(tokens);

    }

}
