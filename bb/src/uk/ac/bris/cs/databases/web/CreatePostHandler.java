package uk.ac.bris.cs.databases.web;

import java.util.Map;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.Result;

/**
 *
 * @author csxdb
 */
public class CreatePostHandler extends AbstractPostHandler {
    
    @Override
    public RenderPair handlePost(Map<String,String> params) {
        
        String name = params.get("user");
        if (name == null || name.equals("")) {
            return new RenderPair(null, Result.failure("Missing 'name'"));
        }
        
        long topicId = Long.parseLong(params.get("topic"));
        if (topicId == 0) {
            return new RenderPair(null, Result.failure("Got zero topic id."));
        }
        
        String text = params.get("text");
        if (text == null || text.equals("")) {
            return new RenderPair(null, Result.failure("Missing 'text'"));
        }
        
        APIProvider api = ApplicationContext.getInstance().getApi();
        Result r = api.createPost(topicId, name, text);
        
        if (!r.isSuccess()) {
            return new RenderPair(null, Result.failure(
                "Failed to create post - " + r.getMessage()));
        }

        return new RenderPair("Success.ftl",
            Result.success(new ValueHolder("Created a new post.")));
    }
}
