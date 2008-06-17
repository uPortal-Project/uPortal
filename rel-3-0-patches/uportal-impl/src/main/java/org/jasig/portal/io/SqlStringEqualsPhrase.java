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

public class SqlStringEqualsPhrase implements Phrase {

    // Instance Members.
    private Phrase value;

    /*
     * Public API.
     */

    public static final Reagent VALUE = new SimpleReagent("ELEMENT", "descendant-or-self::text()", ReagentType.PHRASE,
                                            String.class, "Value to generate a SQL string equal string for, works with null correctly.");

    public Formula getFormula() {
        Reagent[] reagents = new Reagent[] {VALUE};
        return new SimpleFormula(SqlStringEqualsPhrase.class, reagents);
    }

    public void init(EntityConfig config) {

        // Instance Members.
        this.value = (Phrase) config.getValue(VALUE);

    }

    public Object evaluate(TaskRequest req, TaskResponse res) {
        final String value = (String) this.value.evaluate(req, res);
        if (value == null) {
            return "IS NULL";
        }
        
        return "= '" + value + "'";
    }
}