package ru.skypro.marketplace.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.marketplace.dto.AdDto;
import ru.skypro.marketplace.entity.Ad;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, UserMapper.class})
public interface AdMapper {


    @Mapping(source = "id", target = "pk")
    @Mapping(source = "user.id", target = "author")
    AdDto adToAdDTO(Ad ad);

    @Mapping(source = "pk", target = "id")
    @Mapping(source = "author", target = "user.id")
    Ad adDTOToAd(AdDto adDto);
}
