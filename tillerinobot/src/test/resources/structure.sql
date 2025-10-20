--
-- Table structure for table `actualbeatmaps`
--

CREATE TABLE `actualbeatmaps` (
  `beatmapid` int(11) NOT NULL,
  `content` longblob,
  `gzipContent` longblob COMMENT 'added later as a more space-efficient alternative to `content`',
  `downloaded` bigint(20) NOT NULL,
  `hash` varchar(32) COLLATE utf8_unicode_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED;

-- --------------------------------------------------------

--
-- Table structure for table `apibeatmaps`
--

CREATE TABLE `apibeatmaps` (
  `beatmapId` int(11) NOT NULL,
  `mods` int(11) NOT NULL DEFAULT '0' COMMENT 'Added later. Before we only ever pulled for nomod hence default 0',
  `setId` int(11) NOT NULL,
  `artist` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `title` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `version` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `creator` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `source` tinytext COLLATE utf8_unicode_ci NOT NULL,
  `tags` text COLLATE utf8_unicode_ci,
  `creatorId` int(11) NOT NULL DEFAULT '-1',
  `genreId` int(11) NOT NULL DEFAULT '-1',
  `languageId` int(11) NOT NULL DEFAULT '-1',
  `approved` mediumint(9) NOT NULL,
  `approvedDate` bigint(20) DEFAULT NULL,
  `lastUpdate` bigint(20) NOT NULL,
  `bpm` double NOT NULL,
  `starDifficulty` double NOT NULL,
  `aimDifficulty` double NOT NULL DEFAULT '-1',
  `speedDifficulty` double NOT NULL DEFAULT '-1',
  `overallDifficulty` double NOT NULL,
  `circleSize` double NOT NULL,
  `approachRate` double NOT NULL,
  `healthDrain` double NOT NULL,
  `hitLength` mediumint(9) NOT NULL COMMENT 'Had to be increase because there was a 111 hour beatmap',
  `totalLength` mediumint(9) NOT NULL COMMENT 'Had to be increased because there was a 111 hour beatmap',
  `mode` tinyint(4) NOT NULL,
  `fileMd5` char(32) COLLATE utf8_unicode_ci NOT NULL,
  `favouriteCount` int(11) NOT NULL DEFAULT '0',
  `playCount` int(11) NOT NULL DEFAULT '0',
  `passCount` int(11) NOT NULL DEFAULT '0',
  `maxCombo` int(11) NOT NULL DEFAULT '0',
  `downloaded` bigint(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `apiscores`
--

CREATE TABLE `apiscores` (
  `userId` int NOT NULL,
  `beatmapId` int NOT NULL,
  `pp` double NOT NULL,
  `score` bigint NOT NULL,
  `mods` bigint NOT NULL,
  `maxCombo` int NOT NULL,
  `count50` mediumint NOT NULL,
  `count100` mediumint NOT NULL,
  `count300` mediumint NOT NULL,
  `countMiss` mediumint NOT NULL,
  `countKatu` mediumint NOT NULL,
  `countGeki` mediumint NOT NULL,
  `perfect` tinyint NOT NULL,
  `date` bigint NOT NULL,
  `rank` char(2) COLLATE utf8mb3_unicode_ci NOT NULL,
  `mode` int NOT NULL,
  `downloaded` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `apiusers`
--

CREATE TABLE `apiusers` (
  `userId` int NOT NULL,
  `userName` tinytext COLLATE utf8mb3_unicode_ci NOT NULL,
  `mode` tinyint NOT NULL DEFAULT '0',
  `count300` int NOT NULL,
  `count100` int NOT NULL,
  `count50` int NOT NULL,
  `playCount` int NOT NULL,
  `rankedScore` bigint NOT NULL,
  `totalScore` bigint NOT NULL,
  `rank` int NOT NULL,
  `level` double NOT NULL,
  `pp` double NOT NULL,
  `accuracy` double NOT NULL,
  `countSS` int NOT NULL,
  `countS` int NOT NULL,
  `countA` int NOT NULL,
  `country` tinytext COLLATE utf8mb3_unicode_ci NOT NULL,
  `downloaded` bigint NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `botconfig`
--

CREATE TABLE `botconfig` (
  `path` varchar(128) NOT NULL,
  `value` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `diffestimates`
--

CREATE TABLE `diffestimates` (
  `beatmapid` int NOT NULL,
  `mods` bigint NOT NULL,
  `success` tinyint(1) NOT NULL,
  `failure` mediumtext COLLATE utf8mb3_unicode_ci,
  `calculated` bigint NOT NULL,
  `dataVersion` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `md5` char(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'notset',
  `aim` double NOT NULL,
  `speed` double NOT NULL,
  `starDiff` float NOT NULL DEFAULT '-1',
  `flashlight` float NOT NULL DEFAULT '-1',
  `sliderFactor` float NOT NULL DEFAULT '-1',
  `speedNoteCount` double NOT NULL DEFAULT '-1' COMMENT 'Added with https://github.com/Tillerino/Tillerinobot/pull/218',
  `approachRate` float NOT NULL DEFAULT '-1',
  `overallDifficulty` float NOT NULL DEFAULT '-1',
  `maxMaxCombo` int DEFAULT '0',
  `circleCount` int NOT NULL,
  `sliderCount` int NOT NULL DEFAULT '-1',
  `spinnerCount` int NOT NULL DEFAULT '-1' COMMENT 'added later',
  `aimDifficultStrainCount` double NOT NULL DEFAULT '-1',
  `aimDifficultSliderCount` double NOT NULL DEFAULT '-1',
  `speedDifficultStrainCount` double NOT NULL DEFAULT '-1'
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `givenrecommendations`
--

CREATE TABLE `givenrecommendations` (
  `id` int(11) NOT NULL,
  `beatmapid` int(11) NOT NULL,
  `date` bigint(20) NOT NULL,
  `userid` int(11) NOT NULL,
  `mods` bigint(20) NOT NULL,
  `forgotten` tinyint(1) NOT NULL DEFAULT '0',
  `hidden` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `players`
--

CREATE TABLE `players` (
  `userid` int NOT NULL,
  `lastseen` bigint NOT NULL DEFAULT '0',
  `lastupdatetop50` bigint NOT NULL DEFAULT '0',
  `agetop50` bigint NOT NULL DEFAULT '0' COMMENT 'lastseen - lastupdatetop50'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ppaddictlinkkeys`
--

CREATE TABLE `ppaddictlinkkeys` (
  `identifier` tinytext NOT NULL,
  `displayName` tinytext NOT NULL,
  `linkKey` char(32) NOT NULL,
  `expires` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `ppaddictusers`
--

CREATE TABLE `ppaddictusers` (
  `identifier` tinytext NOT NULL,
  `data` longtext,
  `forward` tinytext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `userdata`
--

CREATE TABLE `userdata` (
  `userid` int(11) NOT NULL,
  `userdata` text NOT NULL,
  `versionVisited` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `usernames`
--

CREATE TABLE `usernames` (
  `username` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `userid` int(11) NOT NULL,
  `resolved` bigint(20) NOT NULL,
  `firstresolveattempt` bigint(20) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usertop50`
--

CREATE TABLE `usertop50` (
  `userid` int NOT NULL,
  `place` int NOT NULL,
  `beatmapid` int NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `actualbeatmaps`
--
ALTER TABLE `actualbeatmaps`
  ADD PRIMARY KEY (`beatmapid`);

--
-- Indexes for table `apibeatmaps`
--
ALTER TABLE `apibeatmaps`
  ADD PRIMARY KEY (`beatmapId`,`mods`),
  ADD KEY `mode` (`mode`),
  ADD KEY `beatmapId` (`beatmapId`) USING BTREE;

--
-- Indexes for table `apiscores`
--
ALTER TABLE `apiscores`
  ADD PRIMARY KEY (`userId`,`beatmapId`),
  ADD KEY `userid` (`userId`),
  ADD KEY `beatmapid` (`beatmapId`),
  ADD KEY `idandmods` (`beatmapId`,`mods`);

--
-- Indexes for table `apiusers`
--
ALTER TABLE `apiusers`
  ADD PRIMARY KEY (`userId`,`mode`);

--
-- Indexes for table `botconfig`
--
ALTER TABLE `botconfig`
  ADD PRIMARY KEY (`path`);

--
-- Indexes for table `diffestimates`
--
ALTER TABLE `diffestimates`
  ADD PRIMARY KEY (`beatmapid`,`mods`),
  ADD KEY `dataVersion` (`dataVersion`);

--
-- Indexes for table `givenrecommendations`
--
ALTER TABLE `givenrecommendations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `userid` (`userid`),
  ADD KEY `useridanddate` (`userid`,`date`);

--
-- Indexes for table `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`userid`),
  ADD KEY `agetop50` (`agetop50`);

--
-- Indexes for table `ppaddictlinkkeys`
--
ALTER TABLE `ppaddictlinkkeys`
  ADD PRIMARY KEY (`linkKey`);

--
-- Indexes for table `ppaddictusers`
--
ALTER TABLE `ppaddictusers`
  ADD PRIMARY KEY (`identifier`(85)) USING BTREE;

--
-- Indexes for table `userdata`
--
ALTER TABLE `userdata`
  ADD PRIMARY KEY (`userid`);

--
-- Indexes for table `usernames`
--
ALTER TABLE `usernames`
  ADD PRIMARY KEY (`username`);

--
-- Indexes for table `usertop50`
--
ALTER TABLE `usertop50`
  ADD PRIMARY KEY (`userid`,`place`),
  ADD UNIQUE KEY `userid_2` (`userid`,`beatmapid`),
  ADD KEY `userid` (`userid`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `givenrecommendations`
--
ALTER TABLE `givenrecommendations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;
