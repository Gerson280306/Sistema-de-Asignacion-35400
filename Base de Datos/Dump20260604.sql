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

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '3c11b7d8-4964-11f1-a207-00155d09f000:1-357';

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
  `id_usuario_asigna` int DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_asignacion`),
  UNIQUE KEY `id_solicitud` (`id_solicitud`),
  KEY `fk_asig_tecnico` (`id_tecnico`),
  KEY `fk_asig_usuario` (`id_usuario_asigna`),
  CONSTRAINT `fk_asig_solicitud` FOREIGN KEY (`id_solicitud`) REFERENCES `tb_solicitud` (`id_solicitud`),
  CONSTRAINT `fk_asig_tecnico` FOREIGN KEY (`id_tecnico`) REFERENCES `tb_tecnico` (`id_tecnico`),
  CONSTRAINT `fk_asig_usuario` FOREIGN KEY (`id_usuario_asigna`) REFERENCES `tb_usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_asignacion`
--

LOCK TABLES `tb_asignacion` WRITE;
/*!40000 ALTER TABLE `tb_asignacion` DISABLE KEYS */;
INSERT INTO `tb_asignacion` VALUES (1,4,2,'AUTOMATICA','2026-05-12 01:59:46','2026-05-12 08:00:00','ASIGNADA',NULL,NULL),(2,2,3,'AUTOMATICA','2026-05-12 01:59:46','2026-05-12 08:00:00','ASIGNADA',NULL,NULL),(3,3,4,'AUTOMATICA','2026-05-12 01:59:46','2026-05-12 08:00:00','ASIGNADA',NULL,NULL),(4,5,3,'AUTOMATICA','2026-05-12 02:01:15','2026-05-12 12:00:00','ASIGNADA',NULL,NULL),(5,6,1,'AUTOMATICA','2026-05-12 02:01:46','2026-05-12 08:00:00','ASIGNADA',NULL,NULL),(6,7,3,'MANUAL','2026-05-12 02:02:51','2026-05-12 08:00:00','ASIGNADA',NULL,''),(7,8,1,'AUTOMATICA','2026-05-12 02:19:06','2026-05-12 17:00:00','ASIGNADA',NULL,NULL),(8,9,3,'AUTOMATICA','2026-05-13 11:14:41','2026-05-13 11:00:00','ASIGNADA',NULL,NULL),(9,11,1,'AUTOMATICA','2026-05-13 11:34:05','2026-05-13 17:00:00','ASIGNADA',NULL,''),(10,18,2,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL,NULL),(11,17,4,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL,NULL),(12,16,1,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL,NULL),(13,15,3,'AUTOMATICA','2026-06-02 02:13:09','2026-06-02 08:00:00','ASIGNADA',NULL,NULL),(14,26,4,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL,NULL),(15,25,1,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL,NULL),(16,24,2,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL,NULL),(17,23,3,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 17:00:00','ASIGNADA',NULL,NULL),(18,22,4,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL,NULL),(19,21,1,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL,NULL),(20,20,2,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL,NULL),(21,19,3,'AUTOMATICA','2026-06-02 02:28:14','2026-06-02 12:00:00','ASIGNADA',NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_cliente`
--

LOCK TABLES `tb_cliente` WRITE;
/*!40000 ALTER TABLE `tb_cliente` DISABLE KEYS */;
INSERT INTO `tb_cliente` VALUES (1,'11111111','Juan','Perez','911111111','juan@gmail.com','Av Calle','',NULL,4,NULL,1,'2026-05-11 01:24:21'),(2,'22222222','Pablo','Lopez','922222222','pablo@gmail.com','Av Calle','',NULL,3,NULL,1,'2026-05-11 01:29:15'),(3,'33333333','Carlos','Garcia','933333333','carlos@gmail.com','Av Calle','',NULL,2,NULL,1,'2026-05-11 01:30:20'),(4,'44444444','Ana','Palacios','944444444','ana@gmail.com','Av Calle','',NULL,1,NULL,1,'2026-05-11 01:31:07');
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
-- Table structure for table `tb_evaluacion`
--

DROP TABLE IF EXISTS `tb_evaluacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_evaluacion` (
  `id_evaluacion` int NOT NULL AUTO_INCREMENT,
  `id_asignacion` int NOT NULL,
  `calificacion` tinyint NOT NULL,
  `comentario` text COLLATE utf8mb4_unicode_ci,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_evaluacion`),
  UNIQUE KEY `id_asignacion` (`id_asignacion`),
  CONSTRAINT `fk_eval_asignacion` FOREIGN KEY (`id_asignacion`) REFERENCES `tb_asignacion` (`id_asignacion`),
  CONSTRAINT `tb_evaluacion_chk_1` CHECK ((`calificacion` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_evaluacion`
--

LOCK TABLES `tb_evaluacion` WRITE;
/*!40000 ALTER TABLE `tb_evaluacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_evaluacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_historial`
--

DROP TABLE IF EXISTS `tb_historial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_historial` (
  `id_historial` int NOT NULL AUTO_INCREMENT,
  `id_solicitud` int NOT NULL,
  `id_asignacion` int DEFAULT NULL,
  `estado_anterior` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado_nuevo` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `id_usuario` int DEFAULT NULL,
  `fecha_cambio` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_historial`),
  KEY `fk_hist_solicitud` (`id_solicitud`),
  KEY `fk_hist_asignacion` (`id_asignacion`),
  KEY `fk_hist_usuario` (`id_usuario`),
  CONSTRAINT `fk_hist_asignacion` FOREIGN KEY (`id_asignacion`) REFERENCES `tb_asignacion` (`id_asignacion`),
  CONSTRAINT `fk_hist_solicitud` FOREIGN KEY (`id_solicitud`) REFERENCES `tb_solicitud` (`id_solicitud`),
  CONSTRAINT `fk_hist_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `tb_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_historial`
--

LOCK TABLES `tb_historial` WRITE;
/*!40000 ALTER TABLE `tb_historial` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_historial` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_horario`
--

LOCK TABLES `tb_horario` WRITE;
/*!40000 ALTER TABLE `tb_horario` DISABLE KEYS */;
INSERT INTO `tb_horario` VALUES (31,2,1,'08:00:00','19:00:00',1),(32,2,2,'08:00:00','19:00:00',1),(33,2,3,'08:00:00','19:00:00',1),(34,2,4,'08:00:00','19:00:00',1),(35,2,5,'08:00:00','19:00:00',1),(36,3,1,'08:00:00','19:00:00',1),(37,3,2,'08:00:00','19:00:00',1),(38,3,3,'08:00:00','19:00:00',1),(39,3,4,'08:00:00','19:00:00',1),(40,3,5,'08:00:00','19:00:00',1),(41,4,1,'08:00:00','19:00:00',1),(42,4,2,'08:00:00','19:00:00',1),(43,4,3,'08:00:00','19:00:00',1),(44,4,4,'08:00:00','19:00:00',1),(45,4,5,'08:00:00','19:00:00',1),(51,1,1,'08:00:00','19:00:00',1),(52,1,2,'08:00:00','19:00:00',1),(53,1,3,'08:00:00','19:00:00',1),(54,1,4,'08:00:00','19:00:00',1),(55,1,5,'08:00:00','19:00:00',1);
/*!40000 ALTER TABLE `tb_horario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_reporte`
--

DROP TABLE IF EXISTS `tb_reporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_reporte` (
  `id_reporte` int NOT NULL AUTO_INCREMENT,
  `tipo` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parametros` text COLLATE utf8mb4_unicode_ci,
  `fecha_generado` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_usuario` int DEFAULT NULL,
  `ruta_archivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_reporte`),
  KEY `fk_reporte_usuario` (`id_usuario`),
  CONSTRAINT `fk_reporte_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `tb_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_reporte`
--

LOCK TABLES `tb_reporte` WRITE;
/*!40000 ALTER TABLE `tb_reporte` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_reporte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_solicitud`
--

DROP TABLE IF EXISTS `tb_solicitud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_solicitud` (
  `id_solicitud` int NOT NULL AUTO_INCREMENT,
  `codigo` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_cliente` int NOT NULL,
  `id_tipo_servicio` int NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `prioridad` enum('BAJA','MEDIA','ALTA','CRITICA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDIA',
  `direccion_atencion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `referencia_atencion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_registro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_solicitada` date DEFAULT NULL,
  `horario_preferido` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` enum('PENDIENTE','ASIGNADA','EN_PROCESO','COMPLETADA','CANCELADA','REPROGRAMADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDIENTE',
  `id_usuario_registro` int DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_solicitud`),
  UNIQUE KEY `codigo` (`codigo`),
  KEY `fk_solicitud_cliente` (`id_cliente`),
  KEY `fk_solicitud_tipo` (`id_tipo_servicio`),
  KEY `fk_solicitud_usuario` (`id_usuario_registro`),
  CONSTRAINT `fk_solicitud_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `tb_cliente` (`id_cliente`),
  CONSTRAINT `fk_solicitud_tipo` FOREIGN KEY (`id_tipo_servicio`) REFERENCES `tb_tipo_servicio` (`id_tipo_servicio`),
  CONSTRAINT `fk_solicitud_usuario` FOREIGN KEY (`id_usuario_registro`) REFERENCES `tb_usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_solicitud`
--

LOCK TABLES `tb_solicitud` WRITE;
/*!40000 ALTER TABLE `tb_solicitud` DISABLE KEYS */;
INSERT INTO `tb_solicitud` VALUES (1,'SOL-20260511-001',4,4,'','MEDIA',NULL,NULL,'2026-05-11 23:47:41','2026-05-11','08:00','CANCELADA',NULL,''),(2,'SOL-20260511-002',3,1,'','MEDIA',NULL,NULL,'2026-05-11 23:47:50','2026-05-12','08:00','COMPLETADA',NULL,''),(3,'SOL-20260512-001',1,2,'','BAJA',NULL,NULL,'2026-05-12 01:59:35','2026-05-12','08:00','COMPLETADA',NULL,''),(4,'SOL-20260512-002',2,3,'','MEDIA',NULL,NULL,'2026-05-12 01:59:42','2026-05-12','08:00','COMPLETADA',NULL,''),(5,'SOL-20260512-003',4,1,'','CRITICA',NULL,NULL,'2026-05-12 02:01:11','2026-05-12','12:00','COMPLETADA',NULL,''),(6,'SOL-20260512-004',3,3,'','MEDIA',NULL,NULL,'2026-05-12 02:01:44','2026-05-12','08:00','COMPLETADA',NULL,''),(7,'SOL-20260512-005',3,1,'','MEDIA',NULL,NULL,'2026-05-12 02:02:36','2026-05-12','08:00','COMPLETADA',NULL,''),(8,'SOL-20260512-006',3,1,'','MEDIA',NULL,NULL,'2026-05-12 02:10:33','2026-05-13','17:00','COMPLETADA',NULL,''),(9,'SOL-20260513-001',1,3,'','CRITICA',NULL,NULL,'2026-05-13 11:12:16','2026-05-13','11:00','COMPLETADA',NULL,''),(10,'SOL-20260513-002',1,5,'','MEDIA',NULL,NULL,'2026-05-13 11:15:19','2026-05-13','08:00','CANCELADA',NULL,''),(11,'SOL-20260513-003',1,1,'','CRITICA',NULL,NULL,'2026-05-13 11:34:00','2026-05-13','17:00','COMPLETADA',NULL,''),(12,'SOL-20260513-004',2,2,'','ALTA',NULL,NULL,'2026-05-13 15:24:35','2026-05-13','17:00','CANCELADA',NULL,''),(13,'SOL-20260513-005',2,2,'','MEDIA',NULL,NULL,'2026-05-13 15:27:37','2026-05-13','17:00','CANCELADA',NULL,''),(14,'SOL-20260602-001',3,1,'','MEDIA',NULL,NULL,'2026-06-02 02:12:29','2026-06-02','08:00','COMPLETADA',NULL,''),(15,'SOL-20260602-002',1,1,'','MEDIA',NULL,NULL,'2026-06-02 02:12:34','2026-06-02','08:00','COMPLETADA',NULL,''),(16,'SOL-20260602-003',2,4,'','MEDIA',NULL,NULL,'2026-06-02 02:12:38','2026-06-02','08:00','COMPLETADA',NULL,''),(17,'SOL-20260602-004',1,2,'','MEDIA',NULL,NULL,'2026-06-02 02:12:44','2026-06-02','08:00','COMPLETADA',NULL,''),(18,'SOL-20260602-005',2,5,'','MEDIA',NULL,NULL,'2026-06-02 02:13:06','2026-06-02','08:00','COMPLETADA',NULL,''),(19,'SOL-20260602-006',3,1,'','MEDIA',NULL,NULL,'2026-06-02 02:20:54','2026-06-02','12:00','COMPLETADA',NULL,''),(20,'SOL-20260602-007',1,5,'','MEDIA',NULL,NULL,'2026-06-02 02:20:57','2026-06-02','12:00','COMPLETADA',NULL,''),(21,'SOL-20260602-008',2,2,'','MEDIA',NULL,NULL,'2026-06-02 02:21:01','2026-06-02','12:00','COMPLETADA',NULL,''),(22,'SOL-20260602-009',2,2,'','MEDIA',NULL,NULL,'2026-06-02 02:21:07','2026-06-02','12:00','COMPLETADA',NULL,''),(23,'SOL-20260602-010',2,2,'','MEDIA',NULL,NULL,'2026-06-02 02:21:35','2026-06-02','17:00','COMPLETADA',NULL,''),(24,'SOL-20260602-011',2,1,'','MEDIA',NULL,NULL,'2026-06-02 02:21:39','2026-06-02','17:00','COMPLETADA',NULL,''),(25,'SOL-20260602-012',1,1,'','MEDIA',NULL,NULL,'2026-06-02 02:21:42','2026-06-02','17:00','COMPLETADA',NULL,''),(26,'SOL-20260602-013',2,2,'','MEDIA',NULL,NULL,'2026-06-02 02:22:38','2026-06-02','17:00','COMPLETADA',NULL,'');
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
  `nivel` enum('JUNIOR','SENIOR','EXPERTO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'JUNIOR',
  `disponibilidad` enum('DISPONIBLE','OCUPADO','DESCANSO','INACTIVO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DISPONIBLE',
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_tecnico`
--

LOCK TABLES `tb_tecnico` WRITE;
/*!40000 ALTER TABLE `tb_tecnico` DISABLE KEYS */;
INSERT INTO `tb_tecnico` VALUES (1,'11111111','Gerson','Contreras','911111111','gerson@gmail.com',4,4,'JUNIOR','DISPONIBLE',6,'',1,'2026-05-11 01:32:24'),(2,'22222222','Jams','Calla','922222222','jams@gmail.com',5,4,'JUNIOR','DISPONIBLE',6,'',1,'2026-05-11 01:33:47'),(3,'33333333','Abel','Giurfa','933333333','abel@gmail.com',3,2,'JUNIOR','DISPONIBLE',6,'',1,'2026-05-11 01:34:51'),(4,'44444444','Alonso','Rodriguez','944444444','alonso@gmail.com',2,1,'JUNIOR','DISPONIBLE',6,'',1,'2026-05-11 01:35:42');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_usuario`
--

LOCK TABLES `tb_usuario` WRITE;
/*!40000 ALTER TABLE `tb_usuario` DISABLE KEYS */;
INSERT INTO `tb_usuario` VALUES (1,'admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','Administrador del Sistema',NULL,'ADMIN',1,'2026-06-04 23:22:56','2026-05-06 11:08:56'),(2,'gestor','18f2b94d784d03c222cb7c47148cdb8457f1ef3eaf3e317711f25d55747f6a35','Gestor de Asignaciones','gestor@sbr.com','SUPERVISOR',1,'2026-06-04 23:19:20','2026-06-04 00:03:18');
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
 1 AS `codigo`,
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
/*!50001 VIEW `v_solicitud_resumen` AS select `s`.`id_solicitud` AS `id_solicitud`,`s`.`codigo` AS `codigo`,concat(`c`.`nombres`,' ',`c`.`apellidos`) AS `cliente`,`c`.`telefono` AS `telefono`,`c`.`direccion` AS `direccion`,`z`.`nombre` AS `zona_cliente`,`ts`.`nombre` AS `tipo_servicio`,`s`.`prioridad` AS `prioridad`,`s`.`estado` AS `estado`,`s`.`fecha_registro` AS `fecha_registro`,`s`.`fecha_solicitada` AS `fecha_solicitada`,concat(`t`.`nombres`,' ',`t`.`apellidos`) AS `tecnico_asignado`,`a`.`fecha_programada` AS `fecha_programada`,`a`.`estado_asignacion` AS `estado_asignacion` from (((((`tb_solicitud` `s` join `tb_cliente` `c` on((`c`.`id_cliente` = `s`.`id_cliente`))) left join `tb_zona` `z` on((`z`.`id_zona` = `c`.`id_zona`))) join `tb_tipo_servicio` `ts` on((`ts`.`id_tipo_servicio` = `s`.`id_tipo_servicio`))) left join `tb_asignacion` `a` on((`a`.`id_solicitud` = `s`.`id_solicitud`))) left join `tb_tecnico` `t` on((`t`.`id_tecnico` = `a`.`id_tecnico`))) */;
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

-- Dump completed on 2026-06-04 23:31:14
