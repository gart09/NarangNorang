-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: narangnorang
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chat`
--
CREATE DATABASE IF NOT EXISTS narangnorang
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE narangnorang;

DROP TABLE IF EXISTS `chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(300) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `sender_id` bigint DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `target_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invite_space`
--

DROP TABLE IF EXISTS `invite_space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invite_space` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `status` enum('ACCEPTED','PENDING','REJECTED') NOT NULL,
  `type` enum('MemberToSpace','SpaceToMember') NOT NULL,
  `member_profile_card_id` bigint NOT NULL,
  `space_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsbf13omp9hs3wc4hy2boa7jro` (`member_profile_card_id`),
  KEY `FKs801q7j1hgbdalt5ldcvbk15` (`space_id`),
  CONSTRAINT `FKs801q7j1hgbdalt5ldcvbk15` FOREIGN KEY (`space_id`) REFERENCES `space` (`id`),
  CONSTRAINT `FKsbf13omp9hs3wc4hy2boa7jro` FOREIGN KEY (`member_profile_card_id`) REFERENCES `member_profile_card` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member_profile_card`
--

DROP TABLE IF EXISTS `member_profile_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_profile_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `room_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd4xyvx3vy43rg64obv2rsuy6e` (`room_id`),
  KEY `FKaw02so8y6xn158h6vdnatc0uk` (`user_id`),
  CONSTRAINT `FKaw02so8y6xn158h6vdnatc0uk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKd4xyvx3vy43rg64obv2rsuy6e` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member_profile_custom_answer`
--

DROP TABLE IF EXISTS `member_profile_custom_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_profile_custom_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `value` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `field_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnwy9x7nxvjg9lxjtltqijj6gj` (`member_id`),
  KEY `FKgmpu43v0mnm3gcky2ynulypqg` (`field_id`),
  CONSTRAINT `FKgmpu43v0mnm3gcky2ynulypqg` FOREIGN KEY (`field_id`) REFERENCES `room_profile_custom_field` (`id`),
  CONSTRAINT `FKnwy9x7nxvjg9lxjtltqijj6gj` FOREIGN KEY (`member_id`) REFERENCES `member_profile_card` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_token`
--

DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expired_at` datetime(6) DEFAULT NULL,
  `token_key` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `max_member` int NOT NULL,
  `name` varchar(128) NOT NULL,
  `room_code` varchar(6) NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkr9j3iuvn7xxck7q4kj6hab5w` (`room_code`),
  KEY `FKbx9snvq7gghcs7i2hjasjln6f` (`owner_id`),
  CONSTRAINT `FKbx9snvq7gghcs7i2hjasjln6f` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `room_profile_custom_field`
--

DROP TABLE IF EXISTS `room_profile_custom_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_profile_custom_field` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `field_name` varchar(128) NOT NULL,
  `option_type` enum('DATE','MULTI_SELECT','NUMBER','SINGLE_SELECT','TEXT') NOT NULL,
  `is_required` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt9krndq9tc2j1bfytvokt75qf` (`room_id`),
  CONSTRAINT `FKt9krndq9tc2j1bfytvokt75qf` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `room_profile_custom_field_option`
--

DROP TABLE IF EXISTS `room_profile_custom_field_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_profile_custom_field_option` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `display_order` int NOT NULL,
  `option_value` varchar(128) NOT NULL,
  `field_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn3fsioye2pkulhq9errrp6k5h` (`field_id`),
  CONSTRAINT `FKn3fsioye2pkulhq9errrp6k5h` FOREIGN KEY (`field_id`) REFERENCES `room_profile_custom_field` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `space`
--

DROP TABLE IF EXISTS `space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `space` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `current_member_count` bigint DEFAULT NULL,
  `max_member_count` bigint DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `owner_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `space_member`
--

DROP TABLE IF EXISTS `space_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `space_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `space_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm3kufdwacuw4x2k4qymmqa1fr` (`member_id`),
  KEY `FK3d4g0h3ks6nt43881qftd4p87` (`space_id`),
  CONSTRAINT `FK3d4g0h3ks6nt43881qftd4p87` FOREIGN KEY (`space_id`) REFERENCES `space` (`id`),
  CONSTRAINT `FKm3kufdwacuw4x2k4qymmqa1fr` FOREIGN KEY (`member_id`) REFERENCES `member_profile_card` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `space_profile_card`
--

DROP TABLE IF EXISTS `space_profile_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `space_profile_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `custom_field` json DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `preferred_end_time` time DEFAULT NULL,
  `preferred_start_time` time DEFAULT NULL,
  `tech_stack` json DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `space_id` bigint NOT NULL,
  `owner` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKes4nh8jjiedvfi4jia7petq86` (`space_id`),
  CONSTRAINT `FKiitt6qos7bos9avux1mjvr741` FOREIGN KEY (`space_id`) REFERENCES `space` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `space_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmv3bm4evy05blju4y3rr29wor` (`space_id`),
  CONSTRAINT `FKmv3bm4evy05blju4y3rr29wor` FOREIGN KEY (`space_id`) REFERENCES `space` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_user_role`
--

DROP TABLE IF EXISTS `user_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_user_role` (
  `user_id` bigint NOT NULL,
  `user_role_id` bigint NOT NULL,
  KEY `FKak8topdb5d9ms6ml76er9vd3l` (`user_role_id`),
  KEY `FK2c25owjk4tax6fvm6yptbakvj` (`user_id`),
  CONSTRAINT `FK2c25owjk4tax6fvm6yptbakvj` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKak8topdb5d9ms6ml76er9vd3l` FOREIGN KEY (`user_role_id`) REFERENCES `user_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-27 15:16:18
