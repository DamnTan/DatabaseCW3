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

    private long timer = cal.getTimeInMillis();

    public API(Connection c) {
        this.c = c;
    }

    @Override
    public Result<Map<String, String>> getUsers() {
        Map<String, String> userMap = new HashMap<String, String>();
        try(PreparedStatement p = c.prepareStatement("SELECT username, name FROM Person")){
            ResultSet r = p.executeQuery();
            boolean exists = r.next();
            if(!exists){
               return Result.failure("Table is empty");
            }
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
      String s = "SELECT count(*) AS counter FROM Topic JOIN Post on topicID = parent_topic WHERE topicID = ?";
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
      String s = "SELECT * FROM Person JOIN likeTopic on id = liketopic_user WHERE liketopic_topic_ID = ?";
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
        String s = "SELECT *, (SELECT COUNT(*) FROM Topic LEFT JOIN Likepost ON topicid = likepost_topic_ID where topicid = ?) AS Likes FROM Topic LEFT JOIN Post ON topicid = parent_topic JOIN Forum ON forum_id = parent_forum JOIN Person ON person.username = post.author left join likepost on (likepost_user=Person.username) and (likepost_topic_ID=topicid) WHERE topicid = ? ORDER BY postedAt DESC LIMIT 1";
        PostView pv;
        try (PreparedStatement p = c.prepareStatement(s)) {
            p.setLong(1, topicId);
            p.setLong(2, topicId);
            ResultSet r = p.executeQuery();
            boolean exists;
            exists = r.next();
            Long fid = r.getLong("forum_id");
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
        String s = "SELECT * FROM Forum ORDER BY forum_title ASC";
        List<ForumSummaryView> list = new ArrayList<ForumSummaryView>();
        try(PreparedStatement p = c.prepareStatement(s)){
            ResultSet r = p.executeQuery();
            Long id;
            String title;
            while(r.next()){
                id = r.getLong("forum_id");
                title = r.getString("forum_title");
                ForumSummaryView f = new ForumSummaryView(id, title, null);
                list.add(f);
            }
        } catch (SQLException e){
            return Result.failure("Something bad happened: " + e);
        }
        return Result.success(list);
    }

    @Override
    public Result createForum(String title) {
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
      Result<PostView> previous = getLatestPost(topicId);
      PostView post;
      try  {
         post = previous.getValue();
      }
      catch (Exception e) {
         return Result.failure("Topic does not exist");
      }
      String s = "Insert into Post (postNumber, parent_topic, author, contents, postedAt) VALUES(?, ?, ?, ?, ?)";
      try(PreparedStatement p = c.prepareStatement(s)){
          int postNumber = post.getPostNumber() + 1;
          p.setInt(1, postNumber);
          p.setLong(2, topicId);
          p.setString(3, username);
          p.setString(4, text);
          p.setLong(5, timer);
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
      String s = "SELECT * FROM Forum LEFT JOIN Topic on parent_forum = forum_id LEFT JOIN Person on creator_un = username where forum_id =?";
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
      String s = "SELECT * FROM Topic JOIN Forum on parent_forum = forum_id WHERE topicid = ?";
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
      String s = "SELECT *, (SELECT COUNT(*) FROM Post JOIN Likepost ON postNumber = likepost_post_ID WHERE topic_id = ?) AS likes FROM Post JOIN Person ON author = username WHERE parent_topic = ? ORDER BY postNumber DESC";
      try(PreparedStatement p = c.prepareStatement(s)){
         p.setLong(1, topicId);
         p.setLong(2, topicId);
          ResultSet r = p.executeQuery();
          while(r.next()){
             int postNumber = r.getInt("postNumber");
             String authorName = r.getString("name");
             String authorUserName = r.getString("username");
             String text = r.getString("contents");
             int postedAt = r.getInt("postedAt");
             int likes = r.getInt("likes");
             PostView post = new PostView(forumId, topicId, postNumber, authorName, authorUserName, text, postedAt, likes);
             postList.add(post);
         }
      }
      catch(SQLException e){
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
          return Result.failure("bad username");
         }
        String s = "Insert into topic (topic_title, parent_forum, creator_un, created) VALUES(?, ?, ?, ?)";

        try(PreparedStatement p = c.prepareStatement(s)){
            p.setString(1, title);
            p.setLong(2, forumId);
            p.setString(3, username);
            p.setLong(4, timer);
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
         Result<Integer> topic_id = countTopic(forumId);
         int id;
         if (topic_id != null) {
            id = topic_id.getValue() + 1;
         }
         else id =0;
         createPost(id, username, text);
         return Result.success(null);
      }

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
      String s = "  SELECT *, (SELECT topicid FROM Forum LEFT JOIN Topic on parent_forum = forum_id where f.forum_id= forum_id ORDER BY created DESC limit 1) as topicid from forum  f LEFT JOIN Topic on parent_forum = forum_id group by forum_id";
      List<AdvancedForumSummaryView> forums;
      try(PreparedStatement p = c.prepareStatement(s)){
          ResultSet r = p.executeQuery();
          int date = 0;
          forums = new ArrayList<AdvancedForumSummaryView>();
          AdvancedForumSummaryView forum;
          while(r.next()){
             long forumID = r.getLong("forum_id");
             String forumtitle = r.getString("forum_title");
             long topicID = r.getLong("topicID");
             TopicSummaryView topic = getTopicSummaryView(forumID, topicID);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedForumView> getAdvancedForum(long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result likePost(String username, long topicId, int post, boolean like) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public boolean checkString(String table, String column, String value) {
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

    public boolean checkLong (String table, String column, Long value) {
      String s = "SELECT * FROM "+table+" WHERE ? = ?";
      try(PreparedStatement p = c.prepareStatement(s)){
      //    System.out.println(0);
        p.setString(1, column);
        p.setLong(2, value);
        ResultSet r = p.executeQuery();
      }
      catch (SQLException e) {
        return false;
      }
      return true;
    }


    private TopicSummaryView getTopicSummaryView(long topicID, long forumID){
    String s = "SELECT (SELECT COUNT(*) FROM Topic JOIN Liketopic on liketopic_topic_ID = topicId group by topicid where topicId = ?) AS likes, * FROM Topic JOIN Person on creator_un = username WHERE topicid = ?";
    TopicSummaryView topic;
    try(PreparedStatement p = c.prepareStatement(s)){
        p.setLong(1, topicID);
        p.setLong(2, topicID);
        ResultSet r = p.executeQuery();
        boolean exists;
        exists = r.next();
        if(!exists) {
           return null;
        }
         topicID = r.getLong("topicID");
         String topictitle = r.getString("topic_title");
         int likes = r.getInt("Likes");
         String creatorName = r.getString("name");
         String creatorUsername  = r.getString("username");
         Result<Integer> counter = countPostsInTopic(topicID);
         Result<PostView> resultlatestpost = getLatestPost(topicID);
         PostView post = resultlatestpost.getValue();
         int postCount = counter.getValue();
         String lastPostName = post.getAuthorName();
         int lastPostTime = post.getPostedAt();
         int created = r.getInt("created");
         topic = new TopicSummaryView(topicID, forumID, topictitle, postCount, created, lastPostTime, lastPostName, likes, creatorName, creatorUsername);
      }
      catch (SQLException e){
         return null;
     }
     return topic;
  }

  public Result<Integer> countTopic(long forumId) {
       if(!checkLong("Forum", "forumID", forumId)) {
          return Result.failure("Topic does not exist");
       }
      TopicView topic;
      String s = "SELECT count(*) as counter FROM Topic JOIN Forum on forum_id = parent_forum WHERE forum_id = ?";
      int count = 0;
      try (PreparedStatement p = c.prepareStatement(s)) {
          p.setLong(1, forumId);
          ResultSet r = p.executeQuery();
          boolean exists;
          exists = r.next();
          if (!exists) return Result.success(count);
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
