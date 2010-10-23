package org.jasig.portal.portlets.search.gsa;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "gsaApplicationContext.xml")
public class GsaSearchTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() throws JAXBException, IOException {
        
        Resource resource = applicationContext.getResource("classpath:/org/jasig/portal/portlets/search/gsa/gsa1.xml");
        
        JAXBContext jc = JAXBContext.newInstance(GsaResults.class);
        Unmarshaller u = jc.createUnmarshaller();
        GsaResults result = (GsaResults)u.unmarshal(resource.getInputStream());
        
        assert result.getSpellingSuggestion().size() == 1;
    }

    @Test
    public void test2() throws JAXBException, IOException {
        
        Resource resource = applicationContext.getResource("classpath:/org/jasig/portal/portlets/search/gsa/gsa2.xml");
        
        JAXBContext jc = JAXBContext.newInstance(GsaResults.class);
        Unmarshaller u = jc.createUnmarshaller();
        GsaResults result = (GsaResults)u.unmarshal(resource.getInputStream());
        
        assert result.getDirectoryLinks().size() == 2;
    }

}
