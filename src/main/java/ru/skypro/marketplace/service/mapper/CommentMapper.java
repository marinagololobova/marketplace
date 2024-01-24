package ru.skypro.marketplace.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.marketplace.dto.CommentDto;
import ru.skypro.marketplace.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "id", target = "pk")
    @Mapping(target = "authorImage", source = "author.image")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(source = "author.id", target = "author")
    CommentDto commentToCommentDTO(Comment comment);

    @Mapping(source = "pk", target = "id")
    @Mapping(source = "author", target = "author.id")
    Comment commentDTOToComment(CommentDto commentDTO);

}
