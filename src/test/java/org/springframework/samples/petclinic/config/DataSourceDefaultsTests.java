package org.springframework.samples.petclinic.config;

import static org.junit.Assert.assertTrue;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Without any setting, the application must start on the in-memory HSQLDB database.
 */
@ContextConfiguration(locations = {"classpath:spring/datasource-config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class DataSourceDefaultsTests {

    @Autowired
    private DataSource dataSource;

    @Test
    public void usesHsqldbByDefault() {
        assertTrue(dataSource.getUrl().startsWith("jdbc:hsqldb:mem:"));
    }
}
