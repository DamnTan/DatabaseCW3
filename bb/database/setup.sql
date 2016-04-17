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
    title VARCHAR(100) NOT NULL
);

CREATE TABLE Topic (
    topicID INTEGER PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    Forum INTEGER NOT NULL,
    creatorID INTEGER,
    page INTEGER,
    CONSTRAINT Creator_ID FOREIGN KEY (creatorID) REFERENCES Person(id),
    CONSTRAINT Forum_ID FOREIGN KEY (Forum)REFERENCES Forum(ID)
);

CREATE TABLE Post (
    Postnumber INTEGER PRIMARY KEY,
    text VARCHAR(200) NOT NULL,
    posted INTEGER NOT NULL,
    author INTEGER,
    topic INTEGER NOT NULL,
    postedAt INTEGER NOT NULL,
    CONSTRAINT author_id FOREIGN KEY (author) REFERENCES Person(id),
    CONSTRAINT topic_id FOREIGN KEY (topic) REFERENCES Topic(topicID)
);

CREATE TABLE Likepost (
    userID INTEGER,
    Post_ID INTEGER,
    Topic_ID INTEGER,
    CONSTRAINT Liker_ID FOREIGN KEY (userID) REFERENCES Person(id),
    CONSTRAINT post_ref FOREIGN KEY (Post_ID) REFERENCES post(postnumber),
    CONSTRAINT Topic_ref FOREIGN KEY (Topic_ID) REFERENCES topic(topicID),
    PRIMARY KEY (userID, Post_ID, Topic_ID)
);


CREATE TABLE Liketopic (
    userID INTEGER,
    Topic_ID INTEGER,
    CONSTRAINT Liker_ID FOREIGN KEY (userID) REFERENCES Person(id),
    CONSTRAINT Topic_ref FOREIGN KEY (topic_id) REFERENCES topic(topicID),
    PRIMARY KEY (userID, Topic_ID)
);

CREATE TABLE favourite (
    userID INTEGER,
    Topic INTEGER,
    PRIMARY KEY (userID, Topic)
);
