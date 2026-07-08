/*
  北工大食堂线上订餐系统数据库脚本 v1.0
  负责人：D 杜雨晗

  新要求：
  1. MySQL 版本建议使用 MySQL 8.4 LTS。
  2. 数据库名固定为 sec_database。
  3. 仅保留 account、dishes、orders 三张核心表，删除学长项目遗留 collect 表。
  4. 全局字符集使用 utf8mb4，排序规则使用 utf8mb4_general_ci。
  5. orders 表字段冻结，只包含 userId、dishId，不新增 orderId、status、orderTime 等字段。
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
(7, '1774971', '123456'),
(8, '就是我', '123456'),
(9, 'student01', '123456'),
(10, 'student02', '123456'),
(11, 'student03', '123456'),
(12, 'teacher01', '123456'),
(13, 'testuser', '123456');

INSERT INTO dishes (dishId, dishRoom, dishWindow, dishName, price) VALUES
(33, '强子烤冷面', '一号窗口', '经典烤冷面', 8),
(34, '强子烤冷面', '一号窗口', '加蛋烤冷面', 10),
(35, '强子烤冷面', '二号窗口', '火腿烤冷面', 11),
(36, '强子烤冷面', '二号窗口', '培根烤冷面', 12),
(37, '重庆小面', '一号窗口', '重庆小面', 10),
(38, '重庆小面', '一号窗口', '豌杂面', 12),
(39, '重庆小面', '二号窗口', '牛肉面', 14),
(40, '重庆小面', '二号窗口', '酸辣粉', 9),
(41, '桂林米粉', '一号窗口', '桂林米粉', 11),
(42, '桂林米粉', '一号窗口', '螺蛳粉', 13),
(43, '桂林米粉', '二号窗口', '牛肉米粉', 14),
(44, '桂林米粉', '二号窗口', '酸笋米粉', 12),
(45, '清真食堂', '一号窗口', '牛肉拉面', 12),
(46, '清真食堂', '一号窗口', '鸡肉盖饭', 13),
(47, '清真食堂', '二号窗口', '羊肉泡馍', 14),
(48, '清真食堂', '二号窗口', '孜然牛肉饭', 13),
(49, '美食园', '一号窗口', '番茄炒蛋饭', 9),
(50, '美食园', '一号窗口', '鱼香肉丝饭', 12),
(51, '美食园', '二号窗口', '宫保鸡丁饭', 12),
(52, '美食园', '二号窗口', '土豆牛肉饭', 13),
(53, '奥运二层', '一号窗口', '鸡排饭', 13),
(54, '奥运二层', '一号窗口', '黑椒牛柳饭', 14),
(55, '奥运二层', '二号窗口', '麻辣香锅', 14),
(56, '奥运二层', '二号窗口', '冒菜', 13),
(57, '学苑餐厅', '一号窗口', '小笼包', 6),
(58, '学苑餐厅', '一号窗口', '豆浆', 2),
(59, '学苑餐厅', '二号窗口', '鸡蛋灌饼', 7),
(60, '学苑餐厅', '二号窗口', '油条', 1.5),
(61, '第三食堂', '一号窗口', '米饭', 0.2),
(62, '第三食堂', '一号窗口', '红烧茄子', 6),
(63, '第三食堂', '二号窗口', '糖醋里脊', 12),
(64, '第三食堂', '二号窗口', '青椒肉丝', 10);

INSERT INTO orders (userId, dishId) VALUES
(7, 33),
(7, 41),
(8, 45),
(9, 50),
(10, 55),
(11, 57),
(12, 63);

SET FOREIGN_KEY_CHECKS = 1;
