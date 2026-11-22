package org.springframework.samples.petclinic.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

/**
 * Guards the per-database configuration files: every database must define the same keys,
 * so switching with -Dpetclinic.db never leaves a placeholder unresolved, and the MySQL
 * file must not store credentials in the repository.
 */
public class DataAccessPropertiesTests {

    private static Properties load(String database) throws IOException {
        String path = "/spring/data-access-" + database + ".properties";
        InputStream in = DataAccessPropertiesTests.class.getResourceAsStream(path);
        assertTrue("missing " + path, in != null);
        try {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } finally {
            in.close();
        }
    }

    @Test
    public void allDatabasesDefineTheSameKeys() throws IOException {
        assertEquals(load("hsqldb").keySet(), load("mysql").keySet());
    }

    @Test
    public void mysqlPasswordComesFromTheEnvironment() throws IOException {
        String password = load("mysql").getProperty("jdbc.password");
        assertEquals("${PETCLINIC_DB_PASSWORD}", password);
    }

    @Test
    public void mysqlSchemaScriptHasNoAccountOrPasswordStatements() throws IOException {
        InputStream in = DataAccessPropertiesTests.class.getResourceAsStream("/db/mysql/initDB.sql");
        assertTrue("missing initDB.sql", in != null);
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            String sql = out.toString("UTF-8").toUpperCase();
            assertFalse(sql.contains("GRANT "));
            assertFalse(sql.contains("IDENTIFIED BY"));
            assertFalse(sql.contains("CREATE USER"));
            assertFalse(sql.contains("CREATE DATABASE"));
        } finally {
            in.close();
        }
    }

    @Test
    public void mysqlUsesItsOwnSchemaScripts() throws IOException {
        Properties mysql = load("mysql");
        assertTrue(mysql.getProperty("jdbc.initLocation").contains("db/mysql/"));
        assertFalse(mysql.getProperty("jdbc.initLocation").contains("hsqldb"));
    }
}
