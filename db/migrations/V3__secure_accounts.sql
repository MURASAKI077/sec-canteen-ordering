USE sec_database;

-- Run once after confirming that userAccount has no duplicate values.
-- Password values are converted to BCrypt automatically when Tomcat starts.
ALTER TABLE account
  MODIFY COLUMN userAccount VARCHAR(64) NOT NULL,
  MODIFY COLUMN userPassword VARCHAR(100) NOT NULL,
  ADD UNIQUE INDEX uk_account_userAccount (userAccount);
