package com.example.calendar.controller;

import com.example.calendar.dto.RequestDto;
import com.example.calendar.dto.ResponseDto;
import com.example.calendar.service.CalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/calendars")
@RestController
public class CalendarController {

    // calendar service를 controller 클래스에서 사용하기 위해선 항상 이 구조(생성자 주입) 필수
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createCalendar(@RequestBody RequestDto dto) {
        // service layer 호출 및 응답
        return new ResponseEntity<>(calendarService.saveCalendar(dto),HttpStatus.CREATED);
    }

    @GetMapping
    public List<ResponseDto> findAllCalendar() {
        // service layer 호출 및 응답
        return calendarService.findAllCalendar();
    }
    @GetMapping("/search")
    public List<ResponseDto> findCalendar(
        // required false
        @RequestParam(required = false) String name,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate updateAt
    ) {
        // service layer 호출 및 응답
        return calendarService.findCalendar(name, updateAt);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> findCalendarById(@PathVariable Long id) {
        // service layer 호출 및 응답
        return new ResponseEntity<>(calendarService.findCalendarById(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto> updateCalendar(
            @PathVariable Long id,
            @RequestBody RequestDto dto
    ) {
        LocalDateTime updateAt = LocalDateTime.now();
        ResponseDto response = calendarService.updateCalendar(id, dto.getName(), dto.getTodo(), dto.getPassword(), updateAt);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return new ResponseEntity(HttpStatus.OK);
    }

}
