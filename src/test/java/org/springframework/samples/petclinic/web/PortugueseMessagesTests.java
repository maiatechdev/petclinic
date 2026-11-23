package org.springframework.samples.petclinic.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("WebControllerTests-config.xml")
public class PortugueseMessagesTests {

    private static final Locale PT = new Locale("pt", "BR");

    @Autowired
    private MessageSource messageSource;

    @Test
    public void resolvesPortugueseMessagesWithAccents() {
        assertEquals("Bem-vindo", this.messageSource.getMessage("welcome", null, PT));
        assertEquals("já está em uso", this.messageSource.getMessage("duplicate", null, PT));
        assertEquals("data inválida", this.messageSource.getMessage("typeMismatch.date", null, PT));
    }

    @Test
    public void portugueseFileHasEveryDefaultKey() throws IOException {
        Set<String> missing = new HashSet<String>(keysOf("messages.properties"));
        missing.removeAll(keysOf("messages_pt.properties"));
        assertTrue("messages_pt.properties sem as chaves " + missing, missing.isEmpty());
    }

    private Set<String> keysOf(String file) throws IOException {
        Properties properties = new Properties();
        InputStream input = new ClassPathResource("messages/" + file).getInputStream();
        try {
            properties.load(input);
        } finally {
            input.close();
        }
        return properties.stringPropertyNames();
    }
}