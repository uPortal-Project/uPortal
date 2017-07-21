package org.apereo.portal.api;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrincipalImplTest {

   PrincipalImpl principal;

   @Before
    public void setup() {
       principal = new PrincipalImpl("key","name");
   }

    @Test
    public void testGetKey(){
        principal = new PrincipalImpl("key","name");
        Assert.assertEquals("key",principal.getKey());
    }

    @Test
    public void testGetName(){
        principal = new PrincipalImpl("key","name");
        Assert.assertEquals("name",principal.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetKeyNull(){
        principal = new PrincipalImpl(null,null);

    }

}
