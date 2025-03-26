package com.example.calendar.service;

import com.example.calendar.dto.RequestDto;
import com.example.calendar.dto.ResponseDto;
import com.example.calendar.entity.Calendar;
import com.example.calendar.repository.CalendarRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {
    // 생성자 주입(아직 뭔지 모름)
    private final CalendarRepository calendarRepository;

    public CalendarServiceImpl(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public ResponseDto saveCalendar(RequestDto dto) {
        Calendar calendar = new Calendar(dto.getName(), dto.getTodo(), dto.getPassword(), dto.getEmail());
        // repository layer 호출
        return calendarRepository.saveCalendar(calendar);
    }

    @Override
    public List<ResponseDto> findAllCalendar() {
        return calendarRepository.findAllCalendar();
    }

    @Override
    public List<ResponseDto> findCalendar(String name, LocalDate updateAt) {
        return calendarRepository.findCalendar(name, updateAt);
    }

    @Override
    public ResponseDto findCalendarById(Long id) {
        return new ResponseDto(calendarRepository.findCalendarById(id));
    }

    @Transactional
    @Override
    public ResponseDto updateCalendar(Long id, String name, String todo, String password, LocalDateTime updateAt) {
        if(name == null || todo == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The title and content are required values.");
        }
        int updatedRow = calendarRepository.updateCalendar(id, name, todo, password, updateAt);

        if(updatedRow == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Does not exist");
        }
        Calendar calendar = calendarRepository.findCalendarById(id);
        return new ResponseDto(calendar);
    }

    @Override
    public void deleteCalendar(Long id) {
        int deletedRow = calendarRepository.deleteCalendar(id);
        if(deletedRow == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Does not exist");
        }
    }
}
