package org.example.springoauth2resourceserver.dto;

import lombok.Data;
import org.example.springoauth2resourceserver.entity.MediaContent;

import java.util.List;

@Data
public class PostRequest {
    private String title;
    private List<MediaContent> contentBlocks;
}
