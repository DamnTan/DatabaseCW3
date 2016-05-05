DROP TABLE IF EXISTS Person;
DROP TABLE IF EXISTS Forum;
DROP TABLE IF EXISTS Topic;
DROP TABLE IF EXISTS Post;
DROP TABLE IF EXISTS Likepost;
DROP TABLE IF EXISTS liketopic;
DROP TABLE IF EXISTS FavouriteTopic;

CREATE TABLE Person (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(10) NOT NULL UNIQUE,
    stuId VARCHAR(10) NULL
);

CREATE TABLE Forum (
    forum_id INTEGER PRIMARY KEY,
    forum_title VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Topic (
    topicID INTEGER PRIMARY KEY,
    topic_title VARCHAR(100) NOT NULL,
    parent_forum INTEGER NOT NULL,
    creator_un VARCHAR(10) NOT NULL,
    created INTEGER NOT NULL,
    CONSTRAINT creator_ID FOREIGN KEY (creator_un) REFERENCES Person(username),
    CONSTRAINT forum_ID FOREIGN KEY (parent_forum)REFERENCES Forum(forum_id)
);

CREATE TABLE Post (
    postNumber INTEGER NOT NULL,
    contents VARCHAR(200) NOT NULL,
    author VARCHAR(200),
    parent_topic INTEGER NOT NULL,
    postedAt INTEGER NOT NULL,
    CONSTRAINT author_id FOREIGN KEY (author) REFERENCES Person(username),
    CONSTRAINT topic_id FOREIGN KEY (parent_topic) REFERENCES Topic(topicID)
    PRIMARY KEY (postNumber, parent_topic)
);

CREATE TABLE Likepost (
    likepost_user VARCHAR(100),
    likepost_Post_ID INTEGER,
    likepost_topic_ID INTEGER,
    CONSTRAINT Liker_user FOREIGN KEY (likepost_user) REFERENCES Person(username),
    CONSTRAINT post_ref FOREIGN KEY (likepost_Post_ID) REFERENCES post(postnumber),
    CONSTRAINT Topic_ref FOREIGN KEY (likepost_Topic_ID) REFERENCES topic(topicID),
    PRIMARY KEY (likepost_user, likepost_post_ID, likepost_topic_ID)
);


CREATE TABLE Liketopic (
    liketopic_user VARCHAR(100),
    liketopic_topic_ID INTEGER,
    CONSTRAINT Liker_user FOREIGN KEY (liketopic_user) REFERENCES Person(username),
    CONSTRAINT Topic_ref FOREIGN KEY (liketopic_topic_ID) REFERENCES topic(topicID),
    PRIMARY KEY (liketopic_user, liketopic_topic_ID)
);

CREATE TABLE FavouriteTopic (
    favouriteTopic_user VARCHAR(100),
    FavouriteTopic_topic_id INTEGER,
    CONSTRAINT favorite_user FOREIGN KEY (favouriteTopic_user) REFERENCES Person(username),
    CONSTRAINT Topic_ref FOREIGN KEY (favouriteTopic_topic_id) REFERENCES topic(topicID),
    PRIMARY KEY (favouriteTopic_user, FavouriteTopic_topic_id)
);
