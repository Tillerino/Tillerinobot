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
-- Table structure for table `botconfig`
--

CREATE TABLE `botconfig` (
  `path` varchar(128) NOT NULL,
  `value` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

--
-- Indexes for dumped tables
--

--
-- Indexes for table `actualbeatmaps`
--
ALTER TABLE `actualbeatmaps`
  ADD PRIMARY KEY (`beatmapid`);

--
-- Indexes for table `botconfig`
--
ALTER TABLE `botconfig`
  ADD PRIMARY KEY (`path`);

--
-- Indexes for table `givenrecommendations`
--
ALTER TABLE `givenrecommendations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `userid` (`userid`),
  ADD KEY `useridanddate` (`userid`,`date`);

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
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `givenrecommendations`
--
ALTER TABLE `givenrecommendations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;
