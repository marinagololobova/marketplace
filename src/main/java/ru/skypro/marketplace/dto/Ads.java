package ru.skypro.marketplace.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ads {
    private Integer count;
    private List<AdDto> results;
}
