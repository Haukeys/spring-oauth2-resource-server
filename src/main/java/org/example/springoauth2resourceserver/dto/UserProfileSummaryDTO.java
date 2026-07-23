package org.example.springoauth2resourceserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileSummaryDTO {
    private String nickname;
    private String name;
    private String surname;
    private String avatar_url;
}