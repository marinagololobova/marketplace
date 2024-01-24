package ru.skypro.marketplace.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdDto {
    private Integer pk;
    private Integer author;
    private String image;
    private Integer price;
    private String title;
}
