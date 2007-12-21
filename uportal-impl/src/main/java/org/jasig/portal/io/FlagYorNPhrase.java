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

public class FlagYorNPhrase implements Phrase {

    // Static Members.
    public static final String DEFAULT_VALUE = "N";

    // Instance Members.
    private Phrase dbvalue;

    /*
     * Public API.
     */

    public static final Reagent DB_VALUE = new SimpleReagent("DB_VALUE", "descendant-or-self::text()",
                    ReagentType.PHRASE, String.class, "Value of the field from the DB.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {DB_VALUE};
        return new SimpleFormula(FlagYorNPhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.dbvalue = (Phrase) config.getValue(DB_VALUE);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {

        String value = (String) dbvalue.evaluate(req, res);
        return value != null ? value : DEFAULT_VALUE;

    }

}
