/*
  评价功能增量脚本：
  已经导入过 sec_database.sql 的数据库，只需要执行本文件即可新增 reviews 表。
*/

USE sec_database;

CREATE TABLE IF NOT EXISTS reviews (
  reviewId BIGINT NOT NULL AUTO_INCREMENT,
  orderId BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  content VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  createTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (reviewId),
  UNIQUE INDEX uk_reviews_orderId (orderId),
  INDEX idx_reviews_rating (rating),
  CONSTRAINT fk_reviews_orders
    FOREIGN KEY (orderId) REFERENCES orders (orderId)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
