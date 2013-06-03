CREATE TABLE document_content (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  documentId VARCHAR(50) NOT NULL,
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);