FROM Forum LEFT JOIN Topic on forum = Forum.id LEFT JOIN Person on creatorID = username JOIN Post on topic = topicId WHERE Forum.id = ?
