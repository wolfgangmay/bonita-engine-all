ALTER TABLE job_param DROP CONSTRAINT fk_job_param_jobid;
DROP TABLE job_param cascade constraints purge;
DROP TABLE job_desc cascade constraints purge;
