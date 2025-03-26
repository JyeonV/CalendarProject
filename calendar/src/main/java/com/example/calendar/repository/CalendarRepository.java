package com.example.calendar.repository;

import com.example.calendar.dto.ResponseDto;
import com.example.calendar.entity.Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CalendarRepository {

    ResponseDto saveCalendar(Calendar calendar);
    List<ResponseDto> findAllCalendar();
    List<ResponseDto> findCalendar(String name, LocalDate updateAt);
    Calendar findCalendarById(Long id);
    int updateCalendar(Long id, String name, String todo, String password, LocalDateTime updateAt);
    int deleteCalendar(Long id);
}
