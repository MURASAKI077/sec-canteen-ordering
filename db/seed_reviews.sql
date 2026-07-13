/*
  菜品评价演示数据：
  reviews 表通过 orderId 关联订单，所以这里先为每个菜品生成 3 条演示订单，
  再为这些订单写入评价。脚本可重复执行，不会重复插入相同演示评价。
*/

USE sec_database;
SET NAMES utf8mb4;

DELETE r
FROM reviews r
JOIN orders o ON r.orderId = o.orderId
JOIN (
    SELECT 1 AS sampleNo
    UNION ALL SELECT 2
    UNION ALL SELECT 3
) s
WHERE o.userId = 1
  AND o.orderTime = TIMESTAMPADD(SECOND, o.dishId * 10 + s.sampleNo, '2026-07-01 12:00:00');

DELETE o
FROM orders o
JOIN (
    SELECT 1 AS sampleNo
    UNION ALL SELECT 2
    UNION ALL SELECT 3
) s
WHERE o.userId = 1
  AND o.orderTime = TIMESTAMPADD(SECOND, o.dishId * 10 + s.sampleNo, '2026-07-01 12:00:00');

INSERT INTO orders (userId, dishId, quantity, status, orderTime)
SELECT 1, d.dishId, 1, 'PLACED',
       TIMESTAMPADD(SECOND, d.dishId * 10 + s.sampleNo, '2026-07-01 12:00:00')
FROM dishes d
JOIN (
    SELECT 1 AS sampleNo
    UNION ALL SELECT 2
    UNION ALL SELECT 3
) s;

INSERT INTO reviews (orderId, rating, content)
SELECT o.orderId,
       CASE s.sampleNo WHEN 1 THEN 5 WHEN 2 THEN 4 ELSE 5 END AS rating,
       CASE s.sampleNo
           WHEN 1 THEN CONCAT('菜品：', d.dishName, '。口味稳定，窗口「', d.dishWindow, '」出餐很顺。')
           WHEN 2 THEN CONCAT('这份', d.dishName, '价格合适，在', d.dishRoom, '吃起来比较方便。')
           ELSE CONCAT(d.dishName, '整体满意，分量和味道都在线，适合下次复点。')
       END AS content
FROM dishes d
JOIN (
    SELECT 1 AS sampleNo
    UNION ALL SELECT 2
    UNION ALL SELECT 3
) s
JOIN orders o
  ON o.userId = 1
 AND o.dishId = d.dishId
 AND o.orderTime = TIMESTAMPADD(SECOND, d.dishId * 10 + s.sampleNo, '2026-07-01 12:00:00')
;
