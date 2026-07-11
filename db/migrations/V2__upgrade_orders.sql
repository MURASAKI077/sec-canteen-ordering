USE sec_database;

-- Run this migration once on databases created with the original schema.
-- Existing orders are preserved and receive generated IDs and default metadata.
ALTER TABLE orders
  ADD COLUMN orderId BIGINT NOT NULL AUTO_INCREMENT FIRST,
  ADD COLUMN quantity INT NOT NULL DEFAULT 1 AFTER dishId,
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PLACED' AFTER quantity,
  ADD COLUMN orderTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER status,
  ADD PRIMARY KEY (orderId),
  ADD INDEX idx_orders_user_time (userId, orderTime, orderId);
