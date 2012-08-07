package org.jasig.portal.spring.tx;

import org.hibernate.dialect.Dialect;
import org.springframework.transaction.annotation.Transactional;

public @interface DialectAwareTransactional {
    /**
     * The dialects where a transaction is required.
     */
    Class<? extends Dialect>[] dialects() default {};
    
    /**
     * The transactional annotation to use
     */
    Transactional transactional();
}
