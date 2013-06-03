CREATE TABLE employee (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  age NUMERIC(19, 0) NOT NULL,
  laptopid NUMERIC(19, 0),
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE saemployee (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  employeeid NUMERIC(19, 0) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE laptop (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(50) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE address (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  address VARCHAR(50) NOT NULL,
  employeeid NUMERIC(19, 0) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE project (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE employeeprojectmapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  employeeid NUMERIC(19, 0) NOT NULL,
  projectid NUMERIC(19, 0) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

