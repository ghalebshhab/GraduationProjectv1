package com.start.demo.Services.Posts;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.PostRepository;
import com.start.demo.Entities.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImplemintation implements PostsServices{
    PostRepository postRepository;
    @Autowired
    public PostServiceImplemintation(PostRepository postRepository){
        this.postRepository=postRepository;
    }
    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Post findById(Long id) {
        Optional<Post> post=postRepository.findById(id);
        Post thePost=null;
        if(post.isPresent())
            thePost=post.get();
        else
            throw new RuntimeException("The Post is not found by id-"+id);
        return thePost;
    }

    @Override
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }
}
