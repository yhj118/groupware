package io.github.yhj118.groupware.Team;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamResponse {

    private Integer departmentId;
    private String departmentName;
    private String departmentType;

}
