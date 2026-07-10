/*
  北工大食堂线上订餐系统数据库脚本 v1.0
  负责人：D 杜雨晗

  新要求：
  1. MySQL 版本建议使用 MySQL 8.4 LTS。
  2. 数据库名固定为 sec_database。
  3. 仅保留 account、dishes、orders 三张核心表，删除学长项目遗留 collect 表。
  4. 全局字符集使用 utf8mb4，排序规则使用 utf8mb4_general_ci。
  5. orders 表字段冻结，只包含 userId、dishId，不新增 orderId、status、orderTime 等字段。
  6. 测试数据沿用原项目 ID，便于前后端联调。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS sec_database;
CREATE DATABASE sec_database
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE sec_database;

DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS dishes;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
  userId INT NOT NULL AUTO_INCREMENT,
  userAccount TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  userPassword TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (userId)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE dishes (
  dishId INT NOT NULL AUTO_INCREMENT,
  dishName VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  dishWindow VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  dishRoom VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  price FLOAT NOT NULL,
  PRIMARY KEY (dishId)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE orders (
  userId INT NOT NULL,
  dishId INT NOT NULL,
  INDEX idx_orders_userId (userId),
  INDEX idx_orders_dishId (dishId),
  CONSTRAINT fk_orders_account
    FOREIGN KEY (userId) REFERENCES account (userId)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_orders_dishes
    FOREIGN KEY (dishId) REFERENCES dishes (dishId)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO account (userId, userAccount, userPassword) VALUES
(1, '1234', '1234'),
(2, '123', '123'),
(3, '12345', '12345'),
(4, 'ABC', 'abc'),
(5, '1774971', '123456'),
(6, 'abcd', 'abc'),
(7, '就是我', '123456');

INSERT INTO dishes (dishId, dishName, dishWindow, dishRoom, price) VALUES
(1, '招牌烤冷面', '强子烤冷面 石锅拌饭', '风味餐厅', 7.5),
(2, '经典培根石锅拌饭', '强子烤冷面 石锅拌饭', '风味餐厅', 12),
(3, '鸡汤小面', '重庆小面 酸辣粉', '风味餐厅', 7.5),
(4, '素粉', '桂林米粉 手工水饺', '风味餐厅', 7.5),
(5, '红烧日本豆腐', '浇汁饭', '风味餐厅', 9.5),
(6, '麻辣排骨饭', '浇汁饭', '风味餐厅', 14),
(7, '八宝粥', '早点', '清真食堂', 2),
(8, '石锅泡泡鸡', '石锅泡泡系列', '清真食堂', 14),
(9, '照烧鸡排', '石锅泡泡系列', '清真食堂', 12),
(10, '咖喱鸡肉', '石锅泡泡系列', '清真餐厅', 13),
(11, '老坛酸菜', '酸菜鱼米饭', '清真食堂', 14),
(12, '淤泥波波牛奶', '鲜果路', '美食园', 9),
(13, '现磨花生牛奶', '鲜果路', '美食园', 8),
(14, '西红柿鸡蛋盖饭', '湘里香外', '美食园', 9.5),
(15, '炒香干盖饭', '湘里香外', '美食园', 9.5),
(16, '烧鸭饭', '海南鸡饭', '美食园', 11),
(17, '沙拉鸭拌饭', '海南鸡饭', '美食园', 13),
(18, '木须肉盖饭', '川渝美食', '美食园', 9),
(19, '包菜回锅肉盖饭', '川渝美食', '美食园', 11),
(20, '煎包', '杭州小笼包', '美食园', 7.5),
(21, '煎饺', '杭州小笼包', '美食园', 7.5),
(22, '炒土豆丝', '精品大碗炖菜', '奥运二层', 1.5),
(23, '毛血旺', '精品大碗炖菜', '奥运二层', 5),
(24, '小馒头', '主食', '奥运二层', 0.2),
(25, '花卷', '主食', '奥运二层', 0.5),
(26, '窝头', '主食', '奥运二层', 0.8),
(27, '炝炒绿豆芽', '基本伙', '奥运二层', 1),
(28, '手撕包菜', '基本伙', '奥运二层', 1),
(29, '香菇油菜', '基本伙', '奥运二层', 5),
(30, '虎皮蛋糕', '甜点', '奥运二层', 4),
(31, '菠萝包', '甜点', '奥运二层', 4),
(32, '肉松卷', '甜点', '奥运二层', 5);

INSERT INTO orders (userId, dishId) VALUES
(1, 1),
(2, 3),
(2, 3),
(2, 10),
(4, 2),
(4, 5),
(4, 16),
(4, 17),
(7, 3);

SET FOREIGN_KEY_CHECKS = 1;
