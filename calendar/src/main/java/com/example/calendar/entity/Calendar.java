package com.example.calendar.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Calendar {
    //할 일, 작성자명, 비밀번호, 작성/수정일(날짜 + 시간)
    private Long id;
    private String name;
    private String email;
    private String todo;
    private String password;
    private LocalDateTime createAt; // 작성일(날짜 + 시간)
    private LocalDateTime updateAt; // 수정일(날짜 + 시간)

    public Calendar(String name, String todo, String password, String email) {
        this.name = name;
        this.todo = todo;
        this.email = email;
        this.password = password;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    public Calendar(Long id, String name, String email, String todo, LocalDateTime createAt, LocalDateTime updateAt) {
        this.id = id;
        this.name = name;
        this.todo = todo;
        this.email = email;
        this.password = password;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }
}
