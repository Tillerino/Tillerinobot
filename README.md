# Tillerinobot

[![Build Status](https://github.com/Tillerino/Tillerinobot/actions/workflows/build.yml/badge.svg)](https://github.com/Tillerino/Tillerinobot/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/Tillerino/Tillerinobot/branch/master/graph/badge.svg)](https://codecov.io/gh/Tillerino/Tillerinobot)

This project contains the IRC frontend and a growing part of the backend for ppaddict recommendations and similar services.
The web frontend can be found in the [ppaddict](https://github.com/Tillerino/ppaddict) project.

**Just want to use the bot? Message Tillerino in-game!**

Please visit the [wiki](https://github.com/Tillerino/Tillerinobot/wiki) for documentation.

Join the discussion on [discord](https://discord.gg/0ww19XGd9XsiJ4LI)!


<p align="center">
  <a href="https://discordapp.com/invite/0ww19XGd9XsiJ4LI">
    <img alt="Logo" src="https://discordapp.com/api/guilds/170177781257207808/widget.png?style=banner2">
  </a>
</p>

## Technology

![Pepe Silvia](https://web.archive.org/web/20230329080529if_/https://i.kym-cdn.com/photos/images/original/002/546/187/fb1.jpg)

Since I use the bot as a playground to try out technologies and concepts, it's... _interesting_ in some places and inconsistent at times.
The bot is written mostly in Java with increasing amounts of Rust sprinkled about.

frequently asked:
- [The IRC frontend](tillerinobot-irc) is built using the Rust [irc](https://github.com/aatxe/irc) crate.
- For the [osu! API](https://github.com/ppy/osu-api/wiki), I rolled my own [Java library](https://github.com/Tillerino/osuApiConnector).
  It's available in [maven central](https://mvnrepository.com/artifact/com.github.tillerino/osu-api-connector). It also does some of the AR/OD calculations.
  The bot still accesses the v1 api, I'd like to move to v2 when we can also use that for messaging.
- To calculate pp, we first use [SanDoku](https://github.com/omkelderman/SanDoku) to calculate difficulty attributes
and then our own [translation](src/main/java/tillerino/tillerinobot/diff/OsuScore.java) of the original pp code into Java.

less frequently asked:
- In a nutshell, the application runs in three containers:
  - [The IRC frontend](tillerinobot-irc)
  - [The backend](tillerinobot-live) for [this GUI](https://tillerino.github.io/Tillerinobot/)
  - The core which is mostly in the [tillerinobot](tillerinobot) module, the [ppaddict backend](https://github.com/Tillerino/ppaddict) and some closed source stuff.
- These three containers communicate via RabbitMQ with the contracts in [tillerinobot-model](tillerinobot-model) and [tillerinobot-rabbit](tillerinobot-rabbit).
  Some of the communication is RPC (where it needs to be synchronous, e.g. block until a pong is received from IRC to prevent bursting), some is standard pub/sub.
- There are a bunch of auxiliary containers, e.g. SanDoku (see above), some authentication stuff, RabbitMQ of course, etc.
- I initially rolled my own ORM for database access. It was living in the closed-source part of the backend.
  Since I wanted to open-source more and more of the backend and was hesitant about open-sourcing the ORM (it was messy),
  I migrated all the database code that went into this repository to Spring Data JPA.
  However, the use case is way too thin to justify the insane overhead of JPA,
  so I polished my own ORM (dubbed mORMon) a bit and it now lives [in this repository](tillerinobot/src/main/java/org/tillerino/mormon).
  Spring Data JPA was removed.

## Building/Running Tillerinobot (for developing purposes)

Check out [the wiki](https://github.com/Tillerino/Tillerinobot/wiki/Working-on-Tillerinobot) to find out how to build and run Tillerinobot locally for developing purposes.

---

If you want to support the project, consider becoming a patron:

[![Become a patron](https://i.imgur.com/IvMFq4Q.png)](https://www.patreon.com/tillerinobot)

For more info check out the [Wiki](https://github.com/Tillerino/Tillerinobot/wiki/Donate)!