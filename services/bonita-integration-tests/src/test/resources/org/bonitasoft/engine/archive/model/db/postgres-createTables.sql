CREATE TABLE employee (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  laptopid INT8,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE saemployee (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  employeeid INT8 NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE laptop (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(50) NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE address (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  address VARCHAR(50) NOT NULL,
  employeeid INT8 NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE project (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE employeeprojectmapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  employeeid INT8 NOT NULL,
  projectid INT8 NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

