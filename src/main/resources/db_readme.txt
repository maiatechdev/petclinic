================================================================================
===        Spring PetClinic sample application - Database Configuration      ===
================================================================================

@author Costin Leau

--------------------------------------------------------------------------------

In its default configuration, Petclinic uses an in-memory database (HSQLDB) which
gets populated at startup with data. A similar setup is provided for Mysql in case
a persistent database configuration is needed.
The database is selected without editing any XML or properties file. The setting
"petclinic.db" picks the file spring/data-access-<petclinic.db>.properties:

  (default)                  -> data-access-hsqldb.properties  (in memory, no setup)
  -Dpetclinic.db=mysql       -> data-access-mysql.properties
  PETCLINIC_DB=mysql         -> same, through an environment variable

For MySQL the credentials are never stored in the repository. Set them before starting:

  PETCLINIC_DB_PASSWORD      required
  PETCLINIC_DB_USERNAME      optional (default: petclinic)
  PETCLINIC_DB_URL           optional (default: jdbc:mysql://localhost:3306/petclinic)

Example (Maven, Tomcat plugin):

  set PETCLINIC_DB_PASSWORD=your-password
  set MAVEN_OPTS=-Dpetclinic.db=mysql
  mvn tomcat7:run

The MySQL JDBC driver dependency is commented out in pom.xml and must be enabled to use MySQL.
