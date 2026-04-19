package fr.esgi.bibliotheque.users.infrastructure.rest.mapper;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.infrastructure.rest.dto.UserDetailDto;
import fr.esgi.bibliotheque.users.infrastructure.rest.dto.UserSummaryDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserSummaryDto toSummaryDto(User user) {
        return UserSummaryDto.from(user);
    }

    public UserDetailDto toDetailDto(User user) {
        return UserDetailDto.from(user);
    }

    public List<UserSummaryDto> toSummaryDtoList(List<User> users) {
        return users.stream().map(this::toSummaryDto).toList();
    }
}
