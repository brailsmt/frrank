CREATE DATABASE IF NOT EXISTS frrank;
use frrank;

CREATE TABLE IF NOT EXISTS team (
    teamNumber integer primary key,
    rookieYear integer,
    stateProv varchar(128),
    robotName varchar(256),
    city varchar(50),
    nameFull varchar(1024),
    nameShort varchar(256),
    districtCode varchar(16),
    country varchar(32)
);

CREATE TABLE IF NOT EXISTS season (
    year integer primary key,
    frcChampionship datetime,
    eventCount integer,
    gameName varchar(128),
    kickoff datetime,
    rookieStart integer,
    teamCount INTEGER
);

CREATE TABLE IF NOT EXISTS event (
    code VARCHAR(128) PRIMARY KEY, -- alphanumeric event code unique to the event, and used to make additional api calls regarding the specific event
    divisionCode VARCHAR(128),     -- alphanumeric event code of the parent event, if the event is the child of another
    name VARCHAR(256),             -- official name of the event
    type VARCHAR(32),              -- (regional/districtEvent/districtChampionship/championship) type of event
    districtCode VARCHAR(32),      -- if district event or district championship, the code of the associated district
    venue VARCHAR(256),            -- name of the venue
    city VARCHAR(128),             -- city of event
    stateProv VARCHAR(128),        -- state of event
    country VARCHAR(128),          -- country code of event
    dateStart DATETIME,            -- scheduled start date of the event
    dateEnd DATETIME               -- scheduled end date of the event
);

CREATE TABLE IF NOT EXISTS event_rank (
    event_code VARCHAR(32),     -- This is not provided as part of the api response, because it is part of the request
    teamNumber INTEGER,         -- team number of the team in the record
    autoPoints INTEGER,         -- total of all autonomous points the team has accumulated in the qualification rounds of the event
    containerPoints INTEGER,    -- total number of container points the team has accumulated in the qualification rounds of the event
    coopertitionPoints INTEGER, -- total number of Coopertition points the team has accumulated in the qualification rounds of the event
    dq INTEGER,                 -- total number of times the team has been disqualified from a match in the qualification rounds of the event
    litterPoints INTEGER,       -- total number of litter points the team has accumulated in the qualification rounds of the event (sum of all three types of litter points)
    matchesPlayed INTEGER,      -- total number of match the team has played in the qualification rounds of the event
    qualAverage FLOAT,          -- total qualification average (qa) of the team in the qualification rounds of the event (total points scored in all matches, divided by number of matches). Value is truncated in accordance with the Game Manual
    rank INTEGER,               -- current rank (1-n) of the team in the qualification rounds of the event, where n is the number of teams at the event
    totePoints INTEGER,         -- total number of tote points the team has accumulated in the qualification rounds of the event
    wins INTEGER,               -- not active in the 2015 season. total number of matches the team has won in the qualification rounds of the event
    losses INTEGER,             -- not active in the 2015 season. total number of matches the team has lost in the qualification rounds of the event
    ties INTEGER,               -- not active in the 2015 season. total number of matches the team has tied in the qualification rounds of the event
    PRIMARY KEY (event_code, teamNumber)
);
