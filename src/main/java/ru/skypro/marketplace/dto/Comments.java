package ru.skypro.marketplace.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Comments {
    private Integer count;
    private List<CommentDto> results;
}
