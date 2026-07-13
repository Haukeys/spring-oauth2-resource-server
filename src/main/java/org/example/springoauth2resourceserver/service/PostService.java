package org.example.springoauth2resourceserver.service;

import org.example.springoauth2resourceserver.entity.MediaContent;
import org.example.springoauth2resourceserver.entity.Post;

import java.util.List;

public interface PostService {


    public Post createPost(String title, List<MediaContent> contentBlocks, String sub);
    public Post addComment(String postId, List<MediaContent> contentBlocks, String sub);
    public Post updatePost(String postId, String newTitle, List<MediaContent> newContentBlocks, String sub);
    public Post updateComment(String postId, String commentId, List<MediaContent> newContentBlocks, String sub);
    public void deleteOwnPost(String postId, String sub);
    public Post deleteCommentByModerator(String postId, String commentId);
    public Post deleteOwnComment(String postId, String commentId, String sub);
    public void deletePostByModerator(String postId);

}

