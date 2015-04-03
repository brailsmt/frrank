CREATE DATABASE IF NOT EXISTS frrank;
use frrank;

CREATE TABLE IF NOT EXISTS teams (
    teamNumber integer primary key,
    rookieYear integer,
    stateProv varchar(10),
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
