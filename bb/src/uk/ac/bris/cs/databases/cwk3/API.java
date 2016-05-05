package uk.ac.bris.cs.databases.cwk3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.HashMap;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.AdvancedForumSummaryView;
import uk.ac.bris.cs.databases.api.AdvancedForumView;
import uk.ac.bris.cs.databases.api.ForumSummaryView;
import uk.ac.bris.cs.databases.api.ForumView;
import uk.ac.bris.cs.databases.api.AdvancedPersonView;
import uk.ac.bris.cs.databases.api.PostView;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.PersonView;
import uk.ac.bris.cs.databases.api.SimpleForumSummaryView;
import uk.ac.bris.cs.databases.api.SimpleTopicView;
import uk.ac.bris.cs.databases.api.TopicView;
import uk.ac.bris.cs.databases.api.TopicSummaryView;
import uk.ac.bris.cs.databases.api.SimpleTopicSummaryView;
import uk.ac.bris.cs.databases.api.SimplePostView;
/**
 *
 * @author csxdb
 */
public class API implements APIProvider {

    private final Connection c;
    private Calendar cal = Calendar.getInstance();


    private long getTimer()
    {
       return System.currentTimeMillis()/1000;
   }

    public API(Connection c) {
        this.c = c;
    }

    @Override
    public Result<Map<String, String>> getUsers() {
        Map<String, String> userMap = new HashMap<String, String>();
        try(PreparedStatement p = c.prepareStatement("SELECT username, name FROM Person")){
            ResultSet r = p.executeQuery();
            boolean exists = r.next();
            while(exists){
               String username = new String();
               String name = new String();
               username = r.getString("username");
               name = r.getString("name");
               userMap.put(username, name);
               exists = r.next();
            }
        }
        catch (SQLException e){
            return Result.fatal("Something bad happened: " + e);
        }
        return Result.success(userMap);
    }

    @Override
    public Result<PersonView> getPersonView(String username) {
      if(username == null || username.equals("")){
        return Result.failure("Username blank");
      }
      if (!checkString("Person", "username", username)) {
         return Result.failure("User does not exist");
      }
      PersonView pv;
      try(PreparedStatement p = c.prepareStatement("SELECT * FROM Person WHERE username = ?")){
          p.setString(1, username);
          ResultSet r = p.executeQuery();
          String name = new String();
          String studentId = new String();
          r.next();
          name = r.getString("name");
          username = r.getString("username");
          studentId = r.getString("stuId");
          if (studentId == null) {
             studentId= "";
          }
          pv = new PersonView(name, username, studentId);
          }
      catch (SQLException e){
          return Result.fatal("Something bad happened: " + e);
      }
      return Result.success(pv);
    }

    @Override
    public Result<List<SimpleForumSummaryView>> getSimpleForums() {
      List<SimpleForumSummaryView> sfsvlist = new ArrayList<SimpleForumSummaryView>();
      String s = "SELECT * FROM forum ORDER BY forum_title DESC";
      try (PreparedStatement p = c.prepareStatement(s)) {
        SimpleForumSummaryView forum;
        ResultSet r = p.executeQuery();
        String title = new String();
        Long id;
        while (r.next()) {
          id = r.getLong("forum_id");
          title = r.getString("forum_title");
          forum = new SimpleForumSummaryView(id, title);
          sfsvlist.add(forum);
        }
      }
      catch (SQLException e) {
        return Result.fatal("Something bad happened" + e);
      }
      return Result.success(sfsvlist);
    }

    @Override
    public Result<Integer> countPostsInTopic(long topicId) {
      if(!checkLong("Topic", "topicID", topicId)) {
         return Result.failure("Topic does not exist");
      }
      int count;
      String s = "SELECT count(*) AS counter FROM Topic JOIN Post ON topicID = parent_topic WHERE topicID = ?";
      try(PreparedStatement p = c.prepareStatement(s)){
         p.setLong(1, topicId);
         ResultSet r = p.executeQuery();
         boolean k;
         r.next();
         count = r.getInt("counter");
         if (r.next()) {
            throw new RuntimeException("There shouldn't be another row!");
         }
      }
      catch (SQLException e){
          return Result.fatal("Something bad happened: " + e);
      }
      return Result.success(count);
    }

    @Override
    public Result<List<PersonView>> getLikers(long topicId) {
      if (!checkLong("topic", "topicId", topicId)) {
         return Result.failure("Topic does not exist");
      }
      List<PersonView> list_of_people = new ArrayList<PersonView>();
      String s = "SELECT * FROM Person JOIN likeTopic ON id = liketopic_user WHERE liketopic_topic_ID = ?";
      try(PreparedStatement p = c.prepareStatement(s)) {
         PersonView person;
         p.setLong(1, topicId);
         ResultSet r = p.executeQuery();
         boolean k;
         String username = new String();
         String name = new String();
         String studentId = new String();
         while(r.next()){
            username = r.getString("username");
            name = r.getString("name");
            studentId = r.getString("stuId");
            person = new PersonView(name, username, studentId);
            list_of_people.add(person);
         }
      }
      catch (SQLException e){
          return Result.fatal("Something bad happened: " + e);
      }
      return Result.success(list_of_people);
    }

    @Override
    public Result<SimpleTopicView> getSimpleTopic(long topicId) {
      if(!checkLong("Topic", "topicID", topicId)) {
         return Result.failure("Topic does not exist");
      }SimplePostView post;
      List <SimplePostView> posts = new ArrayList<SimplePostView>();
      SimpleTopicView topic;
      String s = "SELECT * FROM Topic LEFT JOIN Post ON topicid = parent_topic WHERE topicID = ?";
      try (PreparedStatement p = c.prepareStatement(s)) {
        p.setLong(1, topicId);
        ResultSet r = p.executeQuery();
        topicId = r.getLong("topicid");
        String title = r.getString("topic_title");
        while(r.next()){
          int postNumber = r.getInt("postNumber");
          String author = r.getString("author");
          String text = r.getString("contents");
          int postedAt = r.getInt("postedAt");
          post = new SimplePostView(postNumber, author, text, postedAt);
          posts.add(post);
        }
        topic = new SimpleTopicView(topicId, title, posts);
      }
      catch (SQLException e) {
        return Result.failure("Something bad happened");
      }
      return Result.success(topic);
    }

    @Override
    public Result<PostView> getLatestPost(long topicId) {
         if(!checkLong("Topic", "topicID", topicId)) {
            return Result.failure("Topic does not exist");
         }
        String s = "SELECT *,"+
            " (SELECT COUNT(*) FROM Topic JOIN Likepost ON topicid = likepost_topic_ID"
            +" WHERE topicid = ?) AS Likes  FROM Topic "
            +"JOIN Forum ON forum.forum_id = topic.parent_forum "
            +"JOIN person ON username=creator_un LEFT JOIN post ON topicid = parent_topic "
            +"LEFT JOIN likepost ON (likepost_user=username) and (likepost_topic_ID=topicid) WHERE topicid = ? limit 1";
        PostView pv;
        try (PreparedStatement p = c.prepareStatement(s)) {
            p.setLong(1, topicId);
            p.setLong(2, topicId);
            ResultSet r = p.executeQuery();
            r.next();
            Long fid = r.getLong("parent_forum");
            Long tid = r.getLong("topicID");
            int pnum = r.getInt("postNumber");
            String aname = r.getString("name");
            String auname = r.getString("username");
            String txt = r.getString("contents");
            int date = r.getInt("postedAt");
            int likes = r.getInt("Likes");
            pv = new PostView(fid, tid, pnum, aname, auname, txt, date, likes);
        }
        catch (SQLException e) {
            return Result.failure("Something bad happened: " + e);
        }
        return Result.success(pv);
    }

    @Override
    public Result<List<ForumSummaryView>> getForums() {

        String s = "SELECT *, (SELECT topicid FROM Forum LEFT JOIN Topic ON parent_forum = forum_id WHERE f.forum_id= forum_id ORDER BY created DESC limit 1) AS topicid FROM forum  f LEFT JOIN Topic ON parent_forum = forum_id GROUP BY forum_id ORDER BY forum_title ASC";
        List<ForumSummaryView> list = new ArrayList<ForumSummaryView>();
        try(PreparedStatement p = c.prepareStatement(s)){
            ResultSet r = p.executeQuery();
            Long id;
            String title;
            SimpleTopicSummaryView topic;
            while(r.next()){
                id = r.getLong("forum_id");
                title = r.getString("forum_title");
                long topicID = r.getLong("topicID");
                String topictitle = r.getString("topic_title");
                if (topictitle != null) {
                   topic = new SimpleTopicSummaryView(topicID, id, topictitle);
                }
                else topic = null;
                ForumSummaryView f = new ForumSummaryView(id, title, topic);
                list.add(f);
            }
        } catch (SQLException e){
            return Result.failure("Something bad happened: " + e);
        }
        return Result.success(list);
    }

    @Override
    public Result createForum(String title) {
      if (title == null || title.equals("")) {
       return Result.failure("Please enter a title for the forum.");
      }
      String s = "Insert into Forum (forum_title) VALUES(?)";
      try(PreparedStatement p = c.prepareStatement(s)){
          p.setString(1, title);
          p.execute();
          c.commit();
       }
       catch (SQLException e){
          try{
            c.rollback();
          }
          catch (SQLException f) {
            return Result.fatal("Something really bad happened");
          }
           return Result.failure("Something bad happened: " + e);
       }
      return Result.success(null);
   }

    @Override
    public Result createPost(long topicId, String username, String text) {
      if (username == null || username.equals("") || text == null || text.equals("")) {
        return Result.failure("Not correct input");
      }
      if(!checkString("person", "username", username)) {
        return Result.failure("User does not exist");
      }
      if(!checkLong("Topic", "topicid", topicId)) {
        return Result.failure("User does not exist");
      }
      int postNumber;
      postNumber = countPostsInTopic(topicId).getValue();
      if (postNumber!=0) {
         postNumber++;
      }
      else {
         postNumber = 1;
      }
      String s = "Insert into Post (postNumber, parent_topic, author, contents, postedAt) VALUES(?, ?, ?, ?, ?)";
      try(PreparedStatement p = c.prepareStatement(s)){
          p.setInt(1, postNumber);
          p.setLong(2, topicId);
          p.setString(3, username);
          p.setString(4, text);
          p.setLong(5, getTimer());
          p.execute();
          c.commit();
       }
       catch (SQLException e){
          try{
            c.rollback();
          }
          catch (SQLException f) {
            return Result.fatal("Something really bad happened" +f);
          }
           return Result.failure("Something bad happened: " + e);
       }
      return Result.success(null);
   }

    @Override
    public Result addNewPerson(String name, String username, String studentId) {
      if (username == null || username.equals("") || name == null || name.equals("")) {
        return Result.failure("bad username");
      }
      String s = "Insert into Person (name, username, stuId) VALUES(?, ?, ?)";
      try(PreparedStatement p = c.prepareStatement(s)){
          p.setString(1, name);
          p.setString(2, username);
          p.setString(3, studentId);
          p.execute();
          c.commit();
       }
       catch (SQLException e){
          try{
            c.rollback();
          }
          catch (SQLException f) {
            return Result.fatal("Something really bad happened");
          }
           return Result.failure("Something bad happened: " + e);
       }
      return Result.success(null);
   }

    @Override
    public Result<ForumView> getForum(long id) {
      if (!checkLong("forum", "forum_id", id)) {
        return Result.failure("Forum does not exist");
      }
      String s = "SELECT * FROM Forum LEFT JOIN Topic ON parent_forum = forum_id LEFT JOIN Person ON creator_un = username WHERE forum_id =?";
      ForumView forum;
      try(PreparedStatement p = c.prepareStatement(s)){
          p.setLong(1, id);
          ResultSet r = p.executeQuery();
          String forumTitle;
          boolean exists;
          List<SimpleTopicSummaryView> topics = new ArrayList<SimpleTopicSummaryView>();
          exists = r.next();
          id = r.getLong("forum_id");
          String forumtitle = r.getString("forum_title");
          while(exists){
             Long topicID = r.getLong("topicID");
             String topictitle = r.getString("topic_title");
             if (topictitle == null) break;
             SimpleTopicSummaryView topic = new SimpleTopicSummaryView(topicID, id, topictitle);
             topics.add(topic);
             exists = r.next();
          }
          forum = new ForumView(id, forumtitle, topics);
      }
      catch (SQLException e){
          return Result.failure("Something bad happened: " + e);
      }
      return Result.success(forum);
    }

    @Override
    public Result<TopicView> getTopic(long topicId, int page) {
      if(!checkLong("topic", "topicid", topicId)) {
         return Result.failure("Topic id does not exist");
      }
      TopicView tv;
      String s = "SELECT * FROM Topic JOIN Forum ON parent_forum = forum_id WHERE topicid = ?";
      try(PreparedStatement p = c.prepareStatement(s)){
          p.setLong(1, topicId);
          ResultSet r = p.executeQuery();
          Long forumId = r.getLong("parent_forum");
          String forumName = r.getString("forum_title");
          String topicTitle = r.getString("topic_title");
          List<PostView> allPosts = getAllPostsFromTopic(forumId, topicId);
          List<PostView> posts = allPosts;
          if(page != 0){
             List<PostView> filteredPosts = new ArrayList<PostView>();
             int currentPostIndex = 10*(page-1);
             int counter = 0;
             PostView current;
             while((counter < 10) && ((current = allPosts.get(currentPostIndex)) != null)){
                filteredPosts.add(current);
                currentPostIndex++;
                counter++;
             }
             posts = filteredPosts;
          }
          tv = new TopicView(forumId, topicId, forumName, topicTitle, posts, page);
       }
       catch (SQLException e){
            return Result.failure("Something bad happened: " + e);
       }
      return Result.success(tv);
    }

    private List<PostView> getAllPostsFromTopic(long forumId, long topicId)
    {
      List<PostView> postList = new ArrayList<PostView>();
      String s = "SELECT *, (SELECT COUNT(*) FROM Post JOIN Likepost ON postNumber = likepost_post_ID WHERE likepost_topic_id = ?) AS likes FROM Post JOIN Person ON author = username WHERE parent_topic = ? ORDER BY postNumber ASC";
      try(PreparedStatement p = c.prepareStatement(s)){
         p.setLong(1, topicId);
         p.setLong(2, topicId);
          ResultSet r = p.executeQuery();
          boolean exists = r.next();
          if (!exists) {
             return null;
          }
          while(exists){
             int postNumber = r.getInt("postNumber");
             String authorName = r.getString("name");
             String authorUserName = r.getString("username");
             String text = r.getString("contents");
             int postedAt = r.getInt("postedAt");
             int likes = r.getInt("likes");
             PostView post = new PostView(forumId, topicId, postNumber, authorName, authorUserName, text, postedAt, likes);
             postList.add(post);
             exists = r.next();
         }
      }
      catch(SQLException e){
         return null;
      }
      return postList;
   }

    @Override
    public Result likeTopic(String username, long topicId, boolean like) {
      if (username == null || username.equals("")) {
        return Result.failure("bad username");
      }
      if(!checkString("person", "name", username)) {//table col val
        return Result.failure("bad username");
      }
      if(!checkLong("Topic", "topicId", topicId)) {//table col val
       return Result.failure("topic does not exist");
      }
      String s = "INSERT INTO Liketopic (liketopic_user, liketopic_topic_ID) VALUES (?, ?)";
      String t = "DELETE FROM Liketopic WHERE liketopic_user = ? AND liketopic_topic_ID = ?";
      String k = t;
      if (like) {
        k = s;
      }
      try(PreparedStatement p = c.prepareStatement(k)){
        p.setString(1, username);
        p.setLong(2, topicId);
        p.execute();
        c.commit();
      }
      catch (SQLException e) {
        try{
          c.rollback();
        }
        catch (SQLException f) {
          return Result.fatal("Something really bad happened");
        }
        return Result.fatal("Something really bad happened: " + e);
      }
      return Result.success(null);
    }

    @Override
    public Result favouriteTopic(String username, long topicId, boolean fav) {
      if (username == null || username.equals("")) {
       return Result.failure("bad username");
      }
      if(!checkString("person", "name", username)) {//table col val
       return Result.failure("bad username");
      }
      if(!checkLong("Topic", "topicId", topicId)) {//table col val
       return Result.failure("topicdoes not exist");
      }
      String s = "INSERT INTO FavouriteTopic (favouriteTopic_user, FavouriteTopic_topic_id) VALUES (?, ?)";
      String t = "DELETE FROM FavouriteTopic WHERE favouriteTopic_user = ? AND FavouriteTopic_topic_id = ?";
      String k = t;
      if (fav) {
       k = s;
      }
      try(PreparedStatement p = c.prepareStatement(k)){
       p.setString(1, username);
       p.setLong(2, topicId);
       p.execute();
       c.commit();
      }
      catch (SQLException e) {
       try{
          c.rollback();
       }
       catch (SQLException f) {
          return Result.fatal("Something really bad happened");
       }
       return Result.fatal("Something really bad happened: " + e);
      }
      return Result.success(null);    }

      @Override
      public Result createTopic(long forumId, String username, String title, String text) {
         if(!checkLong("Forum", "forumid", forumId)) {//table col val
          return Result.failure("forum not exist");
         }
         if(!checkString("Person", "username", username)) {//table col val
          return Result.failure("forum not exist");
         }
         if (username == null || username.equals("") || title == null || title.equals("") || text == null || text.equals("")) {
          return Result.failure("bad input");
         }
        String s = "Insert into topic (topic_title, parent_forum, creator_un, created) VALUES(?, ?, ?, ?)";
        try(PreparedStatement p = c.prepareStatement(s)){
            p.setString(1, title);
            p.setLong(2, forumId);
            p.setString(3, username);
            p.setLong(4, getTimer());
            p.execute();
            c.commit();
         }
         catch (SQLException e){
            try{
              c.rollback();
            }
            catch (SQLException f) {
              return Result.fatal("Something really bad happened");
            }
             return Result.failure("Something bad happened: " + e);
         }
         Result<Integer> topic_id = countTopic();
         int id;
         if (topic_id != null) {
            id = topic_id.getValue();
            createPost(id, username, text);
         }
         else {
            Result.failure("Something bad happened: ");
         };
         return Result.success(null);
      }

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
      String s = "SELECT *, a.name AS authorname, c.username AS creatorun, c.name AS creatorname,"
                  +"(SELECT postNumber FROM Forum LEFT JOIN Topic ON parent_forum = forum_id "
                  +"LEFT JOIN Post on parent_topic=topicid WHERE f.forum_id= forum_id "
                  +"ORDER BY postedAt DESC limit 1) AS postid2  ,"
                  +"(SELECT topicid FROM Forum LEFT JOIN Topic ON parent_forum = forum_id "
                  +"WHERE f.forum_id= forum_id ORDER BY created DESC limit 1) AS topicid2 ,"
                  +"(SELECT count(1) FROM post WHERE parent_topic = topicid) AS numposts,"
                  +"(SELECT count(1) FROM liketopic WHERE liketopic_topic_id = topicid) AS likes "
                  +"FROM forum  f LEFT JOIN Topic ON parent_forum = forum_id "
                  +"LEFT JOIN Post ON parent_topic=topicid left JOIN person c ON c.username=creator_un "
                  +"LEFT JOIN Person a on a.username=author GROUP BY forum_id";
      List<AdvancedForumSummaryView> forums;
      try(PreparedStatement p = c.prepareStatement(s)){
          ResultSet r = p.executeQuery();
          forums = new ArrayList<AdvancedForumSummaryView>();
          AdvancedForumSummaryView forum;
          TopicSummaryView topic;
          while(r.next()){
             long forumID = r.getLong("forum_id");
             String forumtitle = r.getString("forum_title");
             String topictitle = r.getString("topic_title");
             long topicID = r.getLong("topicID");
             int likes = r.getInt("likes");
             String creatorName = r.getString("creatorname");
             String creatorUsername = r.getString("creatorun");
             int postCount = r.getInt("numposts");
             String lastPostName = r.getString("authorname");
             int lastPostTime =r.getInt("postedAt");
             int created = r.getInt("created");
             if (topictitle!=null) {
                topic = new TopicSummaryView(topicID, forumID, topictitle, postCount, created, lastPostTime, lastPostName, likes, creatorName, creatorUsername);
             }
             else {
                topic = null;
             }
             forum = new AdvancedForumSummaryView(forumID, forumtitle, topic);
             forums.add(forum);
          }
      }
      catch (SQLException e){
          return Result.failure("Something bad happened: " + e);
      }
      return Result.success(forums);
    }

    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {
        if (username == null || username.equals("")) {
            return Result.failure("bad username");
        }
        if (!checkString("person", "name", username)) {//table col val
            return Result.failure("bad username");
        }
        AdvancedPersonView apv = null;
        String s =  "SELECT *, a.name AS authorname, c.username AS creatorun, c.name AS creatorname, " +
        "(SELECT count(1) from liketopic join topic ON topicid = liketopic_topic_id WHERE liketopic_user = ?) as topiclikes, " +
        "(select count(1) from likepost join post on (likepost.likepost_post_id = post.postnumber and likepost.likepost_topic_id = post.parent_topic) " +
        "JOIN person on post.author = person.username where person.username = ?) as postlikes, (SELECT topicid FROM Forum LEFT JOIN Topic ON parent_forum = forum_id " +
        "LEFT JOIN Post on parent_topic=topicid WHERE topicid=FavouriteTopic_topic_id ORDER BY postedAt DESC limit 1) AS postid2, " +
        "(SELECT count(1) FROM post WHERE parent_topic = FavouriteTopic_topic_id) AS numposts, " +
        "(SELECT count(1) FROM liketopic WHERE liketopic_topic_id = FavouriteTopic_topic_id) AS likes " +
        "FROM Person u LEFT JOIN favouritetopic on FavouriteTopic_user = u.username LEFT JOIN topic on FavouriteTopic_topic_id = topicid " +
        "LEFT JOIN Person c on creator_un= c.username LEFT JOIN Post ON (parent_topic=postid2 and postNumber=postid2) " +
        "LEFT JOIN Person a on a.username=author where u.username = ?";
        try(PreparedStatement p = c.prepareStatement(s)){
            p.setString(1, username);
            p.setString(2, username);
            p.setString(3, username);
            p.execute();
            c.commit();
            ResultSet r = p.executeQuery();
            boolean exists = r.next();
            List<TopicSummaryView> favourites = new ArrayList<TopicSummaryView>();
            String name = r.getString("name");
            username = r.getString("username");
            String studentId = r.getString("stuId");
            if(studentId == null){
                studentId = "";
            }
            int topicLikes = r.getInt("topiclikes");
            int postLikes = r.getInt("postlikes");
            TopicSummaryView topic;
            while(exists) {
                long forumID = r.getLong("parent_forum");
                String topictitle = r.getString("topic_title");
                if(topictitle == null) break;
                long topicID = r.getLong("topicID");
                int likes = r.getInt("likes");
                String creatorName = r.getString("creatorname");
                String creatorUsername = r.getString("creatorun");
                int postCount = r.getInt("numposts");
                String lastPostName = r.getString("authorname");
                int lastPostTime = r.getInt("postedAt");
                int created = r.getInt("created");
                topic = new TopicSummaryView(topicID, forumID, topictitle, postCount, created, lastPostTime, lastPostName, likes, creatorName, creatorUsername);
                favourites.add(topic);
                exists = r.next();
            }
            apv = new AdvancedPersonView(name, username, studentId, topicLikes, postLikes, favourites);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Result.success(apv);
    }

    @Override
    public Result<AdvancedForumView> getAdvancedForum(long id) {
        if (!checkLong("forum", "forum_id", id)) {
            return Result.failure("Forum does not exist");
        }
        String s = "SELECT *, a.name AS authorname, c.username AS creatorun, c.name AS creatorname, "
       +"(SELECT count(1) FROM liketopic WHERE liketopic_topic_id = topicid) AS likes, "
       +"(SELECT count(1) FROM post WHERE parent_topic = topicid) AS numposts, "
       +"(SELECT topicid FROM Forum LEFT JOIN Topic ON parent_forum = forum_id "
       +"WHERE f.forum_id= forum_id ORDER BY created DESC limit 1) AS topicid2, "
       +"(SELECT postNumber FROM Forum LEFT JOIN Topic ON parent_forum = forum_id "
       +"LEFT JOIN Post on parent_topic=topicid "
       +" WHERE f.forum_id= forum_id ORDER BY postedAt DESC limit 1) AS postid2 "
       +"FROM forum  f LEFT JOIN Topic ON parent_forum = forum_id "
       +"LEFT JOIN Post ON (parent_topic=topicid2 AND postNumber = postid2) "
       +"LEFT JOIN person c ON c.username=creator_un LEFT JOIN Person a on a.username=author "
       +"WHERE forum_id = ? GROUP BY forum_id";
        AdvancedForumView forum;
        try(PreparedStatement p = c.prepareStatement(s)){
            p.setLong(1, id);
            ResultSet r = p.executeQuery();
            boolean exists;
            List<TopicSummaryView> topics = new ArrayList<TopicSummaryView>();
            exists = r.next();
            id = r.getLong("forum_id");
            String forumTitle = r.getString("forum_title");
            while(exists){
                Long topicID = r.getLong("topicID");
                String topicTitle = r.getString("topic_title");
                int likes = r.getInt("likes");
                String creatorName = r.getString("creatorname");
                String creatorUsername = r.getString("creatorun");
                int postCount = r.getInt("numposts");
                String lastPostName = r.getString("authorname");
                int lastPostTime =r.getInt("postedAt");
                int created = r.getInt("created");
                if (postCount == 0) {
                   break;
                }
                TopicSummaryView topic = new TopicSummaryView(topicID, id, topicTitle, postCount, created, lastPostTime, lastPostName, likes, creatorName, creatorUsername);
                topics.add(topic);
                exists = r.next();
            }
            forum = new AdvancedForumView(id, forumTitle, topics);
        }
        catch (SQLException e){
            return Result.failure("Something bad happened: " + e);
        }
        return Result.success(forum);
    }

    @Override
    public Result likePost(String username, long topicId, int post, boolean like) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean checkString(String table, String column, String value) {
      String s = "SELECT * FROM "+table+" WHERE ? = ?";
      try(PreparedStatement p = c.prepareStatement(s)){
        p.setString(1, column);
        p.setString(2, value);
        ResultSet r = p.executeQuery();
        return true;
      }
      catch (SQLException e) {
        return false;
      }
    }

    private boolean checkLong (String table, String column, Long value) {
      String s = "SELECT * FROM "+table+" WHERE ? = ?";
      try(PreparedStatement p = c.prepareStatement(s)){
        p.setString(1, column);
        p.setLong(2, value);
        ResultSet r = p.executeQuery();
      }
      catch (SQLException e) {
        return false;
      }
      return true;
    }

  private Result<Integer> countTopic() {
      TopicView topic;
      String s = "SELECT count(*) AS counter FROM Topic";
      int count = 0;
      try (PreparedStatement p = c.prepareStatement(s)) {
          ResultSet r = p.executeQuery();
          boolean exists;
          exists = r.next();
          count = r.getInt("counter");
          if (!exists){
             return Result.success(count);
          }
          if (r.next()) {
             throw new RuntimeException("There shouldn't be another row!");
          }
      }
      catch (SQLException e) {
          return Result.failure("Something bad happened: " + e);
      }
      return Result.success(count);
  }

   }
