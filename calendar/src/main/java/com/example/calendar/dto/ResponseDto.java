package com.example.calendar.dto;

import com.example.calendar.entity.Calendar;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResponseDto {
    private Long id;
    private String name;
    private String todo;
    private String email;
    private LocalDateTime createAt; // 작성일(날짜 + 시간)
    private LocalDateTime updateAt;

    // calendar와 responseDto가 동일한 필드를 가질수 없으므로(password 반환x) findById를 사용할때 나오는 calendar 객체를 responseDto 객체로 변환
    public ResponseDto(Calendar calendar) {
        this.id = calendar.getId();
        this.name = calendar.getName();
        this.todo = calendar.getTodo();
        this.email = calendar.getEmail();
        this.createAt = calendar.getCreateAt();
        this.updateAt = calendar.getUpdateAt();
    }

}
