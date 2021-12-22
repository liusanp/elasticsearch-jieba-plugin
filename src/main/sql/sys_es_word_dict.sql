
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_es_word_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_es_word_dict`;
CREATE TABLE `sys_es_word_dict`  (
  `id` bigint(0) NOT NULL COMMENT 'id',
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '自定义分词',
  `freq` int(0) NOT NULL DEFAULT 3 COMMENT '分词权重，默认3',
  `is_enable` tinyint(0) NOT NULL DEFAULT 1 COMMENT '是否启用：0不启用，1启用，默认1',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `create_by` bigint(0) NULL DEFAULT 0 COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ES自定义分词字典' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
