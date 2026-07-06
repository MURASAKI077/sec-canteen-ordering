/*
 Navicat MySQL Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80023
 Source Host           : localhost:3306
 Source Schema         : sec_database

 Target Server Type    : MySQL
 Target Server Version : 80023
 File Encoding         : 65001

 Date: 04/07/2021 22:48:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`  (
  `userId` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，系统生成',
  `userAccount` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `userPassword` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES (1, '1234', '1234');
INSERT INTO `account` VALUES (2, '123', '123');
INSERT INTO `account` VALUES (3, '12345', '12345');
INSERT INTO `account` VALUES (4, 'ABC', 'abc');
INSERT INTO `account` VALUES (5, '1774971', '123456');
INSERT INTO `account` VALUES (6, 'abcd', 'abc');
INSERT INTO `account` VALUES (7, '就是我', '123456');

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `userId` int NOT NULL,
  `dishId` int NOT NULL,
  INDEX `dishId`(`dishId`) USING BTREE,
  INDEX `userId`(`userId`) USING BTREE,
  CONSTRAINT `collect_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `account` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `collect_ibfk_2` FOREIGN KEY (`dishId`) REFERENCES `dishes` (`dishId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collect
-- ----------------------------

-- ----------------------------
-- Table structure for dishes
-- ----------------------------
DROP TABLE IF EXISTS `dishes`;
CREATE TABLE `dishes`  (
  `dishId` int NOT NULL AUTO_INCREMENT,
  `dishName` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `dishWindow` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `dishRoom` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `price` float NOT NULL,
  PRIMARY KEY (`dishId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dishes
-- ----------------------------
INSERT INTO `dishes` VALUES (1, '招牌烤冷面', '强子烤冷面 石锅拌饭', '风味餐厅', 7.5);
INSERT INTO `dishes` VALUES (2, '经典培根石锅拌饭', '强子烤冷面 石锅拌饭', '风味餐厅', 12);
INSERT INTO `dishes` VALUES (3, '鸡汤小面', '重庆小面 酸辣粉', '风味餐厅', 7.5);
INSERT INTO `dishes` VALUES (4, '素粉', '桂林米粉 手工水饺', '风味餐厅', 7.5);
INSERT INTO `dishes` VALUES (5, '红烧日本豆腐', '浇汁饭', '风味餐厅', 9.5);
INSERT INTO `dishes` VALUES (6, '麻辣排骨饭', '浇汁饭', '风味餐厅', 14);
INSERT INTO `dishes` VALUES (7, '八宝粥', '早点', '清真食堂', 2);
INSERT INTO `dishes` VALUES (8, '石锅泡泡鸡', '石锅泡泡系列', '清真食堂', 14);
INSERT INTO `dishes` VALUES (9, '照烧鸡排', '石锅泡泡系列', '清真食堂', 12);
INSERT INTO `dishes` VALUES (10, '咖喱鸡肉', '石锅泡泡系列', '清真餐厅', 13);
INSERT INTO `dishes` VALUES (11, '老坛酸菜', '酸菜鱼米饭', '清真食堂', 14);
INSERT INTO `dishes` VALUES (12, '淤泥波波牛奶', '鲜果路', '美食园', 9);
INSERT INTO `dishes` VALUES (13, '现磨花生牛奶', '鲜果路', '美食园', 8);
INSERT INTO `dishes` VALUES (14, '西红柿鸡蛋盖饭', '湘里香外', '美食园', 9.5);
INSERT INTO `dishes` VALUES (15, '炒香干盖饭', '湘里香外', '美食园', 9.5);
INSERT INTO `dishes` VALUES (16, '烧鸭饭', '海南鸡饭', '美食园', 11);
INSERT INTO `dishes` VALUES (17, '沙拉鸭拌饭', '海南鸡饭', '美食园', 13);
INSERT INTO `dishes` VALUES (18, '木须肉盖饭', '川渝美食', '美食园', 9);
INSERT INTO `dishes` VALUES (19, '包菜回锅肉盖饭', '川渝美食', '美食园', 11);
INSERT INTO `dishes` VALUES (20, '煎包', '杭州小笼包', '美食园', 7.5);
INSERT INTO `dishes` VALUES (21, '煎饺', '杭州小笼包', '美食园', 7.5);
INSERT INTO `dishes` VALUES (22, '炒土豆丝', '精品大碗炖菜', '奥运二层', 1.5);
INSERT INTO `dishes` VALUES (23, '毛血旺', '精品大碗炖菜', '奥运二层', 5);
INSERT INTO `dishes` VALUES (24, '小馒头', '主食', '奥运二层', 0.2);
INSERT INTO `dishes` VALUES (25, '花卷', '主食', '奥运二层', 0.5);
INSERT INTO `dishes` VALUES (26, '窝头', '主食', '奥运二层', 0.8);
INSERT INTO `dishes` VALUES (27, '炝炒绿豆芽', '基本伙', '奥运二层', 1);
INSERT INTO `dishes` VALUES (28, '手撕包菜', '基本伙', '奥运二层', 1);
INSERT INTO `dishes` VALUES (29, '香菇油菜', '基本伙', '奥运二层', 5);
INSERT INTO `dishes` VALUES (30, '虎皮蛋糕', '甜点', '奥运二层', 4);
INSERT INTO `dishes` VALUES (31, '菠萝包', '甜点', '奥运二层', 4);
INSERT INTO `dishes` VALUES (32, '肉松卷', '甜点', '奥运二层', 5);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `userId` int NOT NULL,
  `dishId` int NOT NULL,
  INDEX `userId`(`userId`) USING BTREE,
  INDEX `dishId`(`dishId`) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `account` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`dishId`) REFERENCES `dishes` (`dishId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 1);
INSERT INTO `orders` VALUES (2, 3);
INSERT INTO `orders` VALUES (2, 3);
INSERT INTO `orders` VALUES (2, 10);
INSERT INTO `orders` VALUES (4, 2);
INSERT INTO `orders` VALUES (4, 5);
INSERT INTO `orders` VALUES (4, 16);
INSERT INTO `orders` VALUES (4, 17);
INSERT INTO `orders` VALUES (7, 3);

SET FOREIGN_KEY_CHECKS = 1;
