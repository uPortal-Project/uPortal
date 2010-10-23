package org.jasig.portal.i18n.translation;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jasig.portal.i18n.translate.AutoTranslationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "translationApplicationContext.xml")
public class AutoTranslationServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() throws IOException {
        Resource resource = applicationContext.getResource("classpath:/org/jasig/portal/i18n/translation/Messages.properties");
        Resource resource2 = applicationContext.getResource("classpath:/org/jasig/portal/i18n/translation/Messages_es.properties");
        AutoTranslationService service = Mockito.spy(new AutoTranslationService());
        
        Mockito.stub(service.translateMessage("String One")).toReturn("String One");
        Mockito.stub(service.translateMessage("String Two")).toReturn("String Two");
        Mockito.stub(service.translateMessage("String Three equals =")).toReturn("String Three equals =");
        Mockito.stub(service.translateMessage("String Four")).toReturn("String Four");
        
        String result = service.getTranslationMessages(resource, resource2);
        
        String expected = "string1=String One\nstring2=String Two\nstring3=String Three equals =\nstring4=String Four\n";
        System.out.println(result);
        System.out.println(expected);
        assertTrue(expected.equals(result));
    }

}
