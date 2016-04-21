DROP TABLE IF EXISTS Person;
DROP TABLE IF EXISTS Forum;
DROP TABLE IF EXISTS Topic;
DROP TABLE IF EXISTS Post;
DROP TABLE IF EXISTS Likepost;
DROP TABLE IF EXISTS liketopic;
DROP TABLE IF EXISTS Favourite;

CREATE TABLE Person (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(10) NOT NULL UNIQUE,
    stuId VARCHAR(10) NULL
);

CREATE TABLE Forum (
    id INTEGER PRIMARY KEY,
    title VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Topic (
    topicID INTEGER PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    forum INTEGER NOT NULL,
    creatorID INTEGER NOT NULL,
    page INTEGER,
    created INTEGER NOT NULL,
    CONSTRAINT creator_ID FOREIGN KEY (creatorID) REFERENCES Person(username),
    CONSTRAINT forum_ID FOREIGN KEY (Forum)REFERENCES Forum(ID)
);

CREATE TABLE Post (
    postNumber INTEGER NOT NULL,
    contents VARCHAR(200) NOT NULL,
    author  VARCHAR(200),
    topic INTEGER NOT NULL,
    postedAt INTEGER NOT NULL,
    CONSTRAINT author_id FOREIGN KEY (author) REFERENCES Person(username),
    CONSTRAINT topic_id FOREIGN KEY (topic) REFERENCES Topic(topicID)
    PRIMARY KEY (postNumber, topic)
);

CREATE TABLE Likepost (
    userID INTEGER,
    Post_ID INTEGER,
    Topic_ID INTEGER,
    CONSTRAINT Liker_ID FOREIGN KEY (userID) REFERENCES Person(username),
    CONSTRAINT post_ref FOREIGN KEY (Post_ID) REFERENCES post(postnumber),
    CONSTRAINT Topic_ref FOREIGN KEY (Topic_ID) REFERENCES topic(topicID),
    PRIMARY KEY (userID, Post_ID, Topic_ID)
);


CREATE TABLE Liketopic (
    userID INTEGER,
    Topic_ID INTEGER,
    CONSTRAINT Liker_ID FOREIGN KEY (userID) REFERENCES Person(username),
    CONSTRAINT Topic_ref FOREIGN KEY (topic_id) REFERENCES topic(topicID),
    PRIMARY KEY (userID, Topic_ID)
);

CREATE TABLE Favourite (
    userID INTEGER,
    Topic INTEGER,
    PRIMARY KEY (userID, Topic)
);
