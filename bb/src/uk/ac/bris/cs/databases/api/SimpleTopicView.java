package uk.ac.bris.cs.databases.api;

import java.util.List;
import uk.ac.bris.cs.databases.util.Params;

/**
 * Detailed view of a singe topic (i.e. the posts).
 * @author csxdb
 */
public class SimpleTopicView {
    
    /* The id of this topic. */
    private final long topicId;
    
    /* The title of this topic. */
    private final String title;
    
    /* The posts in this topic, in the order that they were created. */
    private final List<SimplePostView> posts;
    
    public SimpleTopicView(long topicId, String title,
            List<SimplePostView> posts) {
        
        Params.cannotBeEmpty(title);
        Params.cannotBeEmpty(posts);

        this.topicId = topicId;
        this.title = title;
        this.posts = posts;
    }

    public List<SimplePostView> getPosts() {
        return posts;
    }
    
    /**
     * @return the topicId
     */
    public long getTopicId() {
        return topicId;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }  
}
