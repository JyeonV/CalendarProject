package com.example.calendar.service;

import com.example.calendar.dto.RequestDto;
import com.example.calendar.dto.ResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface CalendarService {

    ResponseDto saveCalendar(RequestDto dto);
    List<ResponseDto> findAllCalendar();
    List<ResponseDto> findCalendar(String name, LocalDate updateAt);
    ResponseDto findCalendarById(Long id);
    ResponseDto updateCalendar(Long id, String name, String todo, String password, LocalDateTime updateAt);
    void deleteCalendar(Long id);
}
