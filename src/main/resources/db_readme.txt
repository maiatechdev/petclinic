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

Before the first start, a MySQL administrator must create the database and the application
user with db/mysql/createDB.sql, replacing <SENHA> (password) and <HOST> (where the application
connects from: localhost for a local MySQL; the address the container sees for MySQL in Docker) (see db/mysql/petclinic_db_setup_mysql.txt). The application
only runs the schema (initDB.sql) and sample data (populateDB.sql), with a user that has no
administrative privileges.

The MySQL JDBC driver (Connector/J 5.1.49) is a runtime dependency in pom.xml, so no manual
edit is needed. Use MySQL 5.7: the 5.1.x driver cannot authenticate against MySQL 8.

Example with MySQL 5.7 in Docker:

  docker run --name petclinic-mysql -e MYSQL_ROOT_PASSWORD=<admin-password> -p 3306:3306 -d mysql:5.7
  (run createDB.sql as administrator, with <HOST> set as described above)
  set PETCLINIC_DB_PASSWORD=<application-password>
  set MAVEN_OPTS=-Dpetclinic.db=mysql
  mvn tomcat7:run
