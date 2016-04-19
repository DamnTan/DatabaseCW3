package uk.ac.bris.cs.databases.cwk3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import uk.ac.bris.cs.databases.api.SimplePostView;
/**
 *
 * @author csxdb
 */
public class API implements APIProvider {

    private final Connection c;

    public API(Connection c) {
        this.c = c;
    }

    @Override
    public Result<Map<String, String>> getUsers() {
        Map<String, String> userMap = new HashMap<String, String>();
        try(PreparedStatement p = c.prepareStatement("SELECT username, name FROM Person")){
            ResultSet r = p.executeQuery();
            boolean k = r.next();
            if(!k){
               return Result.failure("Table is empty");
            }
            while(k){
               String username = new String();
               String name = new String();
               username = r.getString("username");
               name = r.getString("name");
               userMap.put(username, name);
               k = r.next();
            }
        }
        catch (SQLException e){
            return Result.fatal("Something bad happened: " + e);
        }
        return Result.success(userMap);
    }

    @Override
    public Result<PersonView> getPersonView(String username) {
      if(username == ""){
        return Result.failure("Username blank");
      }
      PersonView pv;
      try(PreparedStatement p = c.prepareStatement("SELECT * FROM Person WHERE username = ?")){
          p.setString(1, username);
          ResultSet r = p.executeQuery();
          boolean k = r.next();
          if(!k){
            return Result.failure("User doesn't exist");
          }
          String name = new String();
          String studentId = new String();
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
      String s = "SELECT * FROM forum ORDER BY title DESC";
      try (PreparedStatement p = c.prepareStatement(s)) {
        SimpleForumSummaryView forum;
        ResultSet r = p.executeQuery();
        String title = new String();
        Long id;
        while (r.next()) {
          id = r.getLong("id");
          title = r.getString("title");
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
      int count;
      String s = "SELECT count(*) AS counter FROM Topic JOIN Post on topicID = topic WHERE topicID = ?";
      try(PreparedStatement p = c.prepareStatement(s)){
         p.setLong(1, topicId);
         ResultSet r = p.executeQuery();
         boolean k;
         k = r.next();
         if( !k ) {
           return Result.failure("Table is empty");
         }
         else {
           count = r.getInt("counter");
         }
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
      List<PersonView> list_of_people = new ArrayList<PersonView>();
      String s = "SELECT * FROM Person JOIN likeTopic on id = userID WHERE topicID = ?";
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
      SimplePostView post;
      List <SimplePostView> posts = new ArrayList<SimplePostView>();
      SimpleTopicView topic;
      String s = "SELECT *FROM Topic LEFT JOIN Post  ON topic.topicid = post.topic  WHERE topicID = ?";
      try (PreparedStatement p = c.prepareStatement(s)) {
        p.setLong(1, topicId);
        ResultSet r = p.executeQuery();
        if (r == null) {
          Result.failure("No topic found");
        }
        topicId = r.getLong("topicid");
        String title = r.getString("title");
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
        String s = "SELECT *, (SELECT COUNT(*) FROM Topic LEFT JOIN Likepost ON topicid = topic_id AS Likes) FROM Topic LEFT JOIN Post ON topic.topicid = post.topic JOIN Forum ON forum.id = topic.forum JOIN Person ON person.id = post.author JOIN Likepost ON (Post.postnumber = Likepost.Post_ID AND topic.topicid = likepost.topic_id) WHERE topic.topicid = ? ORDER BY postedAt DESC LIMIT 1";
        PostView pv;
        try (PreparedStatement p = c.prepareStatement(s)) {
          p.setLong(1, topicId);
          ResultSet r = p.executeQuery();
          if (r == null) {
            Result.failure("No results found");
            System.out.println("Whatever!");
          }// THis might be wrong!
          System.out.println("Whatever!");
          Long fid = r.getLong("Forum.id");
          Long tid = r.getLong("topicID");
          int pnum = r.getInt("postNumber");
          String aname = r.getString("Person.name");
          String auname = r.getString("Person.username");
          String txt = r.getString("post.contents");
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
        ForumSummaryView f = new ForumSummaryView(100, "this is a test", null);
        List<ForumSummaryView> list = new ArrayList<>();
        list.add(f);
        return Result.success(list);
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createForum(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createPost(long topicId, String username, String text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result addNewPerson(String name, String username, String studentId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<ForumView> getForum(long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<TopicView> getTopic(long topicId, int page) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result likeTopic(String username, long topicId, boolean like) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result favouriteTopic(String username, long topicId, boolean fav) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createTopic(long forumId, String username, String title, String text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
        throw new UnsupportedOperationException("Not supported yet.");
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
   }
