-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: db_asignacion
-- ------------------------------------------------------
-- Server version	9.7.0

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '3c11b7d8-4964-11f1-a207-00155d09f000:1-504';

--
-- Table structure for table `tb_asignacion`
--

DROP TABLE IF EXISTS `tb_asignacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asignacion` (
  `id_asignacion` int NOT NULL AUTO_INCREMENT,
  `id_solicitud` int NOT NULL,
  `id_tecnico` int NOT NULL,
  `tipo_asignacion` enum('AUTOMATICA','MANUAL') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AUTOMATICA',
  `fecha_asignacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_programada` datetime DEFAULT NULL,
  `estado_asignacion` enum('ASIGNADA','EN_CAMINO','EN_PROCESO','COMPLETADA','CANCELADA','REPROGRAMADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ASIGNADA',
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_asignacion`),
  UNIQUE KEY `id_solicitud` (`id_solicitud`),
  KEY `fk_asig_tecnico` (`id_tecnico`),
  CONSTRAINT `fk_asig_solicitud` FOREIGN KEY (`id_solicitud`) REFERENCES `tb_solicitud` (`id_solicitud`),
  CONSTRAINT `fk_asig_tecnico` FOREIGN KEY (`id_tecnico`) REFERENCES `tb_tecnico` (`id_tecnico`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_asignacion`
--

LOCK TABLES `tb_asignacion` WRITE;
/*!40000 ALTER TABLE `tb_asignacion` DISABLE KEYS */;
INSERT INTO `tb_asignacion` VALUES (1,4,2,'AUTOMATICA','2026-05-12 01:59:46','2026-05-12 08:00:00','ASIGNADA',NULL),(2,2,3,'AUTOMATICA','2026-05-12 01:59:46','2026-05-12 08:00:00','ASIGNADA',NULL),(3,3,4,'AUTOMATICA','2026-05-12 01:59:46','2026-05-12 08:00:00','ASIGNADA',NULL),(4,5,3,'AUTOMATICA','2026-05-12 02:01:15','2026-05-12 12:00:00','ASIGNADA',NULL),(5,6,1,'AUTOMATICA','2026-05-12 02:01:46','2026-05-12 08:00:00','ASIGNADA',NULL),(6,7,3,'MANUAL','2026-05-12 02:02:51','2026-05-12 08:00:00','ASIGNADA',''),(7,8,1,'AUTOMATICA','2026-05-12 02:19:06','2026-05-12 17:00:00','ASIGNADA',NULL),(8,9,3,'AUTOMATICA','2026-05-13 11:14:41','2026-05-13 11:00:00','ASIGNADA',NULL),(9,11,1,'AUTOMATICA','2026-05-13 11:34:05','2026-05-13 17:00:00','ASIGNADA',''),(10,18,2,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL),(11,17,4,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL),(12,16,1,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL),(13,15,3,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL),(14,26,4,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL),(15,25,1,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL),(16,24,2,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL),(17,23,3,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL),(18,22,4,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL),(19,21,1,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL),(20,20,2,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL),(21,19,3,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL),(22,28,4,'AUTOMATICA','2026-06-08 05:08:08','2026-06-08 08:00:00','ASIGNADA',NULL),(23,27,3,'AUTOMATICA','2026-06-08 05:08:08','2026-06-08 08:00:00','ASIGNADA',NULL),(24,30,1,'AUTOMATICA','2026-06-08 05:13:04','2026-06-08 08:00:00','ASIGNADA',NULL),(25,29,2,'AUTOMATICA','2026-06-08 05:13:04','2026-06-08 08:00:00','ASIGNADA',NULL),(26,31,4,'AUTOMATICA','2026-06-08 13:44:12','2026-06-08 17:00:00','COMPLETADA','\nAuto-completada por el sistema'),(29,34,1,'AUTOMATICA','2026-06-08 14:43:01','2026-06-08 17:00:00','COMPLETADA','\nAuto-completada por el sistema'),(30,33,2,'AUTOMATICA','2026-06-08 14:43:01','2026-06-08 17:00:00','COMPLETADA','\nAuto-completada por el sistema'),(31,32,3,'AUTOMATICA','2026-06-08 14:43:01','2026-06-08 17:00:00','COMPLETADA','\nAuto-completada por el sistema'),(37,42,1,'AUTOMATICA','2026-06-08 19:19:13','2026-06-18 08:00:00','ASIGNADA',NULL),(38,43,2,'AUTOMATICA','2026-06-09 14:28:14','2026-06-09 17:00:00','ASIGNADA',NULL),(39,44,5,'AUTOMATICA','2026-06-09 14:28:34','2026-06-09 17:00:00','ASIGNADA',NULL),(40,46,5,'AUTOMATICA','2026-06-09 14:55:01','2026-06-19 08:00:00','ASIGNADA',NULL),(41,45,3,'AUTOMATICA','2026-06-09 14:55:01','2026-06-09 17:00:00','ASIGNADA',NULL);
/*!40000 ALTER TABLE `tb_asignacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_cliente`
--

DROP TABLE IF EXISTS `tb_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_cliente` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `dni` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombres` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellidos` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `referencia` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `distrito` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_zona` int DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_registro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `dni` (`dni`),
  KEY `fk_cliente_zona` (`id_zona`),
  CONSTRAINT `fk_cliente_zona` FOREIGN KEY (`id_zona`) REFERENCES `tb_zona` (`id_zona`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_cliente`
--

LOCK TABLES `tb_cliente` WRITE;
/*!40000 ALTER TABLE `tb_cliente` DISABLE KEYS */;
INSERT INTO `tb_cliente` VALUES (1,'11111111','Juan','Perez','911111111','juan@gmail.com','Av Calle','',NULL,4,NULL,1,'2026-05-11 01:24:21'),(2,'22222222','Pablo','Lopez','922222222','pablo@gmail.com','Av Calle','',NULL,3,NULL,1,'2026-05-11 01:29:15'),(3,'33333333','Carlos','Garcia','933333333','carlos@gmail.com','Av Calle','',NULL,2,NULL,1,'2026-05-11 01:30:20'),(4,'44444444','Ana','Palacios','944444444','ana@gmail.com','Av Calle','',NULL,1,NULL,1,'2026-05-11 01:31:07'),(5,'55555555','Pepe','Guaman','955555555','pepe@gmail.com','Av. Calle','',NULL,3,NULL,1,'2026-06-08 04:59:48');
/*!40000 ALTER TABLE `tb_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_especialidad`
--

DROP TABLE IF EXISTS `tb_especialidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_especialidad` (
  `id_especialidad` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_especialidad`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_especialidad`
--

LOCK TABLES `tb_especialidad` WRITE;
/*!40000 ALTER TABLE `tb_especialidad` DISABLE KEYS */;
INSERT INTO `tb_especialidad` VALUES (1,'Fibra Óptica','Instalación y reparación de fibra óptica',1),(2,'Redes LAN/WAN','Configuración de redes locales y de área amplia',1),(3,'Soporte Técnico','Soporte general y mantenimiento de equipos',1),(4,'Antenas y RF','Instalación de antenas y señal de radiofrecuencia',1),(5,'Cableado Estructurado','Tendido y certificación de cableado',1);
/*!40000 ALTER TABLE `tb_especialidad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_horario`
--

DROP TABLE IF EXISTS `tb_horario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_horario` (
  `id_horario` int NOT NULL AUTO_INCREMENT,
  `id_tecnico` int NOT NULL,
  `dia_semana` tinyint NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fin` time NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_horario`),
  KEY `fk_horario_tecnico` (`id_tecnico`),
  CONSTRAINT `fk_horario_tecnico` FOREIGN KEY (`id_tecnico`) REFERENCES `tb_tecnico` (`id_tecnico`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_horario`
--

LOCK TABLES `tb_horario` WRITE;
/*!40000 ALTER TABLE `tb_horario` DISABLE KEYS */;
INSERT INTO `tb_horario` VALUES (31,2,1,'08:00:00','19:00:00',1),(32,2,2,'08:00:00','19:00:00',1),(33,2,3,'08:00:00','19:00:00',1),(34,2,4,'08:00:00','19:00:00',1),(35,2,5,'08:00:00','19:00:00',1),(36,3,1,'08:00:00','19:00:00',1),(37,3,2,'08:00:00','19:00:00',1),(38,3,3,'08:00:00','19:00:00',1),(39,3,4,'08:00:00','19:00:00',1),(40,3,5,'08:00:00','19:00:00',1),(41,4,1,'08:00:00','19:00:00',1),(42,4,2,'08:00:00','19:00:00',1),(43,4,3,'08:00:00','19:00:00',1),(44,4,4,'08:00:00','19:00:00',1),(45,4,5,'08:00:00','19:00:00',1),(51,1,1,'08:00:00','19:00:00',1),(52,1,2,'08:00:00','19:00:00',1),(53,1,3,'08:00:00','19:00:00',1),(54,1,4,'08:00:00','19:00:00',1),(55,1,5,'08:00:00','19:00:00',1),(66,5,1,'08:00:00','19:00:00',1),(67,5,2,'08:00:00','19:00:00',1),(68,5,3,'08:00:00','19:00:00',1),(69,5,4,'08:00:00','19:00:00',1),(70,5,5,'08:00:00','19:00:00',1);
/*!40000 ALTER TABLE `tb_horario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_solicitud`
--

DROP TABLE IF EXISTS `tb_solicitud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_solicitud` (
  `id_solicitud` int NOT NULL AUTO_INCREMENT,
  `id_cliente` int NOT NULL,
  `id_tipo_servicio` int NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `prioridad` enum('BAJA','MEDIA','ALTA','CRITICA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDIA',
  `fecha_registro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_solicitada` date DEFAULT NULL,
  `horario_preferido` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` enum('PENDIENTE','ASIGNADA','EN_PROCESO','COMPLETADA','CANCELADA','REPROGRAMADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDIENTE',
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_solicitud`),
  KEY `fk_solicitud_cliente` (`id_cliente`),
  KEY `fk_solicitud_tipo` (`id_tipo_servicio`),
  CONSTRAINT `fk_solicitud_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `tb_cliente` (`id_cliente`),
  CONSTRAINT `fk_solicitud_tipo` FOREIGN KEY (`id_tipo_servicio`) REFERENCES `tb_tipo_servicio` (`id_tipo_servicio`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_solicitud`
--

LOCK TABLES `tb_solicitud` WRITE;
/*!40000 ALTER TABLE `tb_solicitud` DISABLE KEYS */;
INSERT INTO `tb_solicitud` VALUES (1,4,4,'','MEDIA','2026-05-11 23:47:41','2026-05-11','08:00','CANCELADA',''),(2,3,1,'','MEDIA','2026-05-11 23:47:50','2026-05-12','08:00','COMPLETADA',''),(3,1,2,'','BAJA','2026-05-12 01:59:35','2026-05-12','08:00','COMPLETADA',''),(4,2,3,'','MEDIA','2026-05-12 01:59:42','2026-05-12','08:00','COMPLETADA',''),(5,4,1,'','CRITICA','2026-05-12 02:01:11','2026-05-12','12:00','COMPLETADA',''),(6,3,3,'','MEDIA','2026-05-12 02:01:44','2026-05-12','08:00','COMPLETADA',''),(7,3,1,'','MEDIA','2026-05-12 02:02:36','2026-05-12','08:00','COMPLETADA',''),(8,3,1,'','MEDIA','2026-05-12 02:10:33','2026-05-13','17:00','COMPLETADA',''),(9,1,3,'','CRITICA','2026-05-13 11:12:16','2026-05-13','11:00','COMPLETADA',''),(10,1,5,'','MEDIA','2026-05-13 11:15:19','2026-05-13','08:00','CANCELADA',''),(11,1,1,'','CRITICA','2026-05-13 11:34:00','2026-05-13','17:00','COMPLETADA',''),(12,2,2,'','ALTA','2026-05-13 15:24:35','2026-05-13','17:00','CANCELADA',''),(13,2,2,'','MEDIA','2026-05-13 15:27:37','2026-05-13','17:00','CANCELADA',''),(14,3,1,'','MEDIA','2026-06-02 02:12:29','2026-06-02','08:00','COMPLETADA',''),(15,1,1,'','MEDIA','2026-06-02 02:12:34','2026-06-02','08:00','COMPLETADA',''),(16,2,4,'','MEDIA','2026-06-02 02:12:38','2026-06-02','08:00','COMPLETADA',''),(17,1,2,'','MEDIA','2026-06-02 02:12:44','2026-06-02','08:00','COMPLETADA',''),(18,2,5,'','MEDIA','2026-06-02 02:13:06','2026-06-02','08:00','COMPLETADA',''),(19,3,1,'','MEDIA','2026-06-02 02:20:54','2026-06-02','12:00','COMPLETADA',''),(20,1,5,'','MEDIA','2026-06-02 02:20:57','2026-06-02','12:00','COMPLETADA',''),(21,2,2,'','MEDIA','2026-06-02 02:21:01','2026-06-02','12:00','COMPLETADA',''),(22,2,2,'','MEDIA','2026-06-02 02:21:07','2026-06-02','12:00','COMPLETADA',''),(23,2,2,'','MEDIA','2026-06-02 02:21:35','2026-06-02','17:00','COMPLETADA',''),(24,2,1,'','MEDIA','2026-06-02 02:21:39','2026-06-02','17:00','COMPLETADA',''),(25,1,1,'','MEDIA','2026-06-02 02:21:42','2026-06-02','17:00','COMPLETADA',''),(26,2,2,'','MEDIA','2026-06-02 02:22:38','2026-06-02','17:00','COMPLETADA',''),(27,1,3,'','MEDIA','2026-06-08 03:01:06','2026-06-08','08:00','COMPLETADA',''),(28,5,2,'','MEDIA','2026-06-08 05:00:02','2026-06-08','08:00','COMPLETADA',''),(29,2,2,'','MEDIA','2026-06-08 05:08:31','2026-06-08','08:00','COMPLETADA',''),(30,5,2,'','MEDIA','2026-06-08 05:12:58','2026-06-08','08:00','COMPLETADA',''),(31,2,2,'','MEDIA','2026-06-08 13:43:52','2026-06-08','17:00','COMPLETADA',''),(32,3,2,'','MEDIA','2026-06-08 13:48:14','2026-06-08','17:00','COMPLETADA',''),(33,2,2,'','MEDIA','2026-06-08 13:48:18','2026-06-08','17:00','COMPLETADA',''),(34,2,2,'','MEDIA','2026-06-08 13:48:21','2026-06-08','17:00','COMPLETADA',''),(42,1,1,'','MEDIA','2026-06-08 19:19:10','2026-06-18','08:00','ASIGNADA',''),(43,1,5,'','MEDIA','2026-06-09 14:28:10','2026-06-09','17:00','ASIGNADA',''),(44,4,5,'','MEDIA','2026-06-09 14:28:28','2026-06-09','17:00','ASIGNADA',''),(45,3,3,'','MEDIA','2026-06-09 14:46:08','2026-06-09','17:00','ASIGNADA',''),(46,1,1,'','MEDIA','2026-06-09 14:54:53','2026-06-19','08:00','ASIGNADA','');
/*!40000 ALTER TABLE `tb_solicitud` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_tecnico`
--

DROP TABLE IF EXISTS `tb_tecnico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_tecnico` (
  `id_tecnico` int NOT NULL AUTO_INCREMENT,
  `dni` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombres` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellidos` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_especialidad` int NOT NULL,
  `id_zona` int DEFAULT NULL,
  `max_solicitudes_dia` int NOT NULL DEFAULT '6',
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_registro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_tecnico`),
  UNIQUE KEY `dni` (`dni`),
  KEY `fk_tecnico_especialidad` (`id_especialidad`),
  KEY `fk_tecnico_zona` (`id_zona`),
  CONSTRAINT `fk_tecnico_especialidad` FOREIGN KEY (`id_especialidad`) REFERENCES `tb_especialidad` (`id_especialidad`),
  CONSTRAINT `fk_tecnico_zona` FOREIGN KEY (`id_zona`) REFERENCES `tb_zona` (`id_zona`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_tecnico`
--

LOCK TABLES `tb_tecnico` WRITE;
/*!40000 ALTER TABLE `tb_tecnico` DISABLE KEYS */;
INSERT INTO `tb_tecnico` VALUES (1,'11111111','Gerson','Contreras','911111111','gerson@gmail.com',4,4,6,'',1,'2026-05-11 01:32:24'),(2,'22222222','Jams','Calla','922222222','jams@gmail.com',5,4,6,'',1,'2026-05-11 01:33:47'),(3,'33333333','Abel','Giurfa','933333333','abel@gmail.com',3,2,6,'',1,'2026-05-11 01:34:51'),(4,'44444444','Alonso','Rodriguez','944444444','alonso@gmail.com',2,1,6,'',1,'2026-05-11 01:35:42'),(5,'55555555','Hilmer','Jauregui','955555555','hilmer@gmail.com',1,1,6,'',1,'2026-06-08 05:07:58');
/*!40000 ALTER TABLE `tb_tecnico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_tipo_servicio`
--

DROP TABLE IF EXISTS `tb_tipo_servicio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_tipo_servicio` (
  `id_tipo_servicio` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_especialidad` int DEFAULT NULL,
  `duracion_estimada_min` int DEFAULT '60',
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_tipo_servicio`),
  KEY `fk_ts_especialidad` (`id_especialidad`),
  CONSTRAINT `fk_ts_especialidad` FOREIGN KEY (`id_especialidad`) REFERENCES `tb_especialidad` (`id_especialidad`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_tipo_servicio`
--

LOCK TABLES `tb_tipo_servicio` WRITE;
/*!40000 ALTER TABLE `tb_tipo_servicio` DISABLE KEYS */;
INSERT INTO `tb_tipo_servicio` VALUES (1,'Fibra Óptica','Servicio especializado en Fibra Óptica',1,120,1),(2,'Redes LAN/WAN','Servicio especializado en Redes LAN/WAN',2,120,1),(3,'Soporte Técnico','Servicio especializado en Soporte Técnico',3,120,1),(4,'Antenas y RF','Servicio especializado en Antenas y RF',4,120,1),(5,'Cableado Estructurado','Servicio especializado en Cableado Estructurado',5,120,1);
/*!40000 ALTER TABLE `tb_tipo_servicio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_usuario`
--

DROP TABLE IF EXISTS `tb_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `username` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre_completo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rol` enum('ADMIN','SUPERVISOR','OPERADOR') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPERADOR',
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  `ultimo_acceso` datetime DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_usuario`
--

LOCK TABLES `tb_usuario` WRITE;
/*!40000 ALTER TABLE `tb_usuario` DISABLE KEYS */;
INSERT INTO `tb_usuario` VALUES (1,'admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','Administrador del Sistema',NULL,'ADMIN',1,'2026-06-08 13:50:13','2026-05-06 11:08:56'),(2,'gestor','18f2b94d784d03c222cb7c47148cdb8457f1ef3eaf3e317711f25d55747f6a35','Gestor de Asignaciones','gestor@sbr.com','SUPERVISOR',1,'2026-06-09 15:05:45','2026-06-04 00:03:18'),(3,'Gerson','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Gerson Contreras','gerson@gmail.com','SUPERVISOR',1,'2026-06-08 18:41:42','2026-06-08 03:00:12'),(4,'admin123','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','Pepe Lopez','pepe@gmail.com','SUPERVISOR',1,'2026-06-08 18:28:56','2026-06-08 13:46:02');
/*!40000 ALTER TABLE `tb_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_zona`
--

DROP TABLE IF EXISTS `tb_zona`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_zona` (
  `id_zona` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_zona`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_zona`
--

LOCK TABLES `tb_zona` WRITE;
/*!40000 ALTER TABLE `tb_zona` DISABLE KEYS */;
INSERT INTO `tb_zona` VALUES (1,'San Bartolo','Distrito de San Bartolo',1),(2,'Punta Negra','Distrito de Punta Negra',1),(3,'Punta Hermosa','Distrito de Punta Hermosa',1),(4,'Pucusana','Distrito de Pucusana',1);
/*!40000 ALTER TABLE `tb_zona` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_solicitud_resumen`
--

DROP TABLE IF EXISTS `v_solicitud_resumen`;
/*!50001 DROP VIEW IF EXISTS `v_solicitud_resumen`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_solicitud_resumen` AS SELECT 
 1 AS `id_solicitud`,
 1 AS `cliente`,
 1 AS `telefono`,
 1 AS `direccion`,
 1 AS `zona_cliente`,
 1 AS `tipo_servicio`,
 1 AS `prioridad`,
 1 AS `estado`,
 1 AS `fecha_registro`,
 1 AS `fecha_solicitada`,
 1 AS `tecnico_asignado`,
 1 AS `fecha_programada`,
 1 AS `estado_asignacion`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_solicitud_resumen`
--

/*!50001 DROP VIEW IF EXISTS `v_solicitud_resumen`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_solicitud_resumen` AS select `s`.`id_solicitud` AS `id_solicitud`,concat(`c`.`nombres`,' ',`c`.`apellidos`) AS `cliente`,`c`.`telefono` AS `telefono`,`c`.`direccion` AS `direccion`,`z`.`nombre` AS `zona_cliente`,`ts`.`nombre` AS `tipo_servicio`,`s`.`prioridad` AS `prioridad`,`s`.`estado` AS `estado`,`s`.`fecha_registro` AS `fecha_registro`,`s`.`fecha_solicitada` AS `fecha_solicitada`,concat(`t`.`nombres`,' ',`t`.`apellidos`) AS `tecnico_asignado`,`a`.`fecha_programada` AS `fecha_programada`,`a`.`estado_asignacion` AS `estado_asignacion` from (((((`tb_solicitud` `s` join `tb_cliente` `c` on((`c`.`id_cliente` = `s`.`id_cliente`))) left join `tb_zona` `z` on((`z`.`id_zona` = `c`.`id_zona`))) join `tb_tipo_servicio` `ts` on((`ts`.`id_tipo_servicio` = `s`.`id_tipo_servicio`))) left join `tb_asignacion` `a` on((`a`.`id_solicitud` = `s`.`id_solicitud`))) left join `tb_tecnico` `t` on((`t`.`id_tecnico` = `a`.`id_tecnico`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-09 15:06:52
