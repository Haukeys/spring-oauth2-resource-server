package org.example.springoauth2resourceserver.repository;


import org.example.springoauth2resourceserver.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends MongoRepository<Post, String> {
}
