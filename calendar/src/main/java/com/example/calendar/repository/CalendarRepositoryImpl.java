package com.example.calendar.repository;

import com.example.calendar.dto.ResponseDto;
import com.example.calendar.entity.Calendar;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CalendarRepositoryImpl implements CalendarRepository{
    // JDBC 사용을 위한 생성자 주입
    private final JdbcTemplate jdbcTemplate;

    public CalendarRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public ResponseDto saveCalendar(Calendar calendar) {
        // INSERT QUERY 문자열로 직접 작성하지 않아도 되게끔 쓰는 기능
        SimpleJdbcInsert userInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("user").usingGeneratedKeyColumns("user_id");
        Map<String, Object> userParameters = new HashMap<>();
        userParameters.put("name", calendar.getName());
        userParameters.put("email", calendar.getEmail());
        userParameters.put("createAt", calendar.getCreateAt());
        userParameters.put("updateAt", calendar.getUpdateAt());
        Number userKey = userInsert.executeAndReturnKey(new MapSqlParameterSource(userParameters));
        // 'calendar' 이름 insert , 이 테이블의 키값은 id
        SimpleJdbcInsert calendarInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("calendar").usingGeneratedKeyColumns("id");
        Map<String, Object> calenderParameters = new HashMap<>();
        calenderParameters.put("todo", calendar.getTodo());
        calenderParameters.put("password", calendar.getPassword());
        calenderParameters.put("name_id", userKey.longValue());
        Number calendarKey = calendarInsert.executeAndReturnKey(new MapSqlParameterSource(calenderParameters));
        // insert를 하게되면 식별자(id)가 auto increment 하게 되서 생성
        // 저장 후 생성된 id 값을 number 타입으로 반환하는 메서드
        //password는 반환 안되게 responseDto에 필드로 안넣음
        return new ResponseDto(calendarKey.longValue(), calendar.getName(), calendar.getTodo(), calendar.getEmail(), calendar.getCreateAt(), calendar.getUpdateAt());
    }

    @Override
    public List<ResponseDto> findAllCalendar() {
        // jdbcTemplate.query 메서드는 내부적으로 resultSet에 대해 while 반복문을 돌면서 RowMapper 내부를 호출한다
        return jdbcTemplate.query("SELECT c.id, u.name, c.todo, u.email, u.createAt, u.updateAt FROM calendar c JOIN user u ON c.name_id = u.user_id", calendarRowMapper());
    }

    @Override
    public List<ResponseDto> findCalendar(String name, LocalDate updateAt) {
        List<ResponseDto> result;
        // 1. 둘다 있을경우 2. 둘중 한쪽만 있을 경우
        //1. jdbcTemplate.query("SELECT id, name, todo, createAt, updateAt FROM calendar WHERE name = ? And Date(updateAt) = ?", calendarRowMapper(), name, updateAt);
        //2. jdbcTemplate.query("SELECT id, name, todo, createAt, updateAt FROM calendar WHERE name = ?", calendarRowMapper(), name);
        //3. jdbcTemplate.query("SELECT id, name, todo, createAt, updateAt FROM calendar WHERE Date(updateAt) = ?", calendarRowMapper(), updateAt);
        //4. 둘다 없을 경우 findAllCalendar
        //5. 동적 쿼리 사용(나중에 업데이트) : 중복되는 SQL 일부를 변수로 추출
        String commonSQL = "SELECT c.id, u.name, u.email, c.todo, u.createAt, u.updateAt " +
                   "FROM calendar c JOIN user u ON c.name_id = u.user_id ";
        if(name != null) {
            // 1. 둘 다 있을경우
            if(updateAt != null) { result = jdbcTemplate.query(commonSQL + "WHERE u.name = ? " + "And Date(u.updateAt) = ?", calendarRowMapper(), name, updateAt); }
            // 2. name만 있을 경우
            else { result = jdbcTemplate.query(commonSQL + "WHERE u.name = ?", calendarRowMapper(), name); }
        }
        else {
            // 3. createAt만 있을 경우
            if(updateAt != null) { result = jdbcTemplate.query(commonSQL + "WHERE Date(u.updateAt) = ?", calendarRowMapper(), updateAt); }
            // 4. 다 없을 경우 (findall)
            else { result = jdbcTemplate.query(commonSQL , calendarRowMapper()); }
        }
        return result;
    }

    @Override
    public Calendar findCalendarById(Long id) {
        // rowmapper랑 query문에서 받는 데이터랑 일치해야 오류가 안난다, password는 조회에선 안필요해서 뺐다가 이거 때문에 오류나서 한참 고생
        List<Calendar> result = jdbcTemplate.query(
                "SELECT c.id, u.name, u.email, c.todo, u.createAt, u.updateAt " +
                    "FROM calendar c JOIN user u ON c.name_id = u.user_id " +
                    "WHERE c.id = ?", calendarRowMapperV2(), id);
        return result.stream().findAny().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "does not exist"));
    }

    @Override
    public int updateCalendar(Long id, String name, String todo, String password, LocalDateTime updateAt) {
        // 1. DB에서 password값을 꺼내와서 변수에 넣고 비교
        // query는 리스트 형태로 반환
        Calendar test = jdbcTemplate.queryForObject("SELECT * FROM calendar c JOIN user u ON c.name_id = u.user_id WHERE c.id = ?", calendarRowMapperV3(), id);
        if(!test.getPassword().equals(password)) {
            //비밀번호가 안맞으면 예외를 발생시킨다
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Password error");
        }
        jdbcTemplate.update("UPDATE calendar SET todo = ? WHERE id = ?", todo, id);
        int a = jdbcTemplate.queryForObject("SELECT name_id FROM calendar WHERE id = ?",Integer.class, id);
        return jdbcTemplate.update("UPDATE user SET name = ?, updateAt = ? WHERE user_id = ?", name, updateAt, a);
    }

    @Override
    public int deleteCalendar(Long id) {
        return jdbcTemplate.update("DELETE FROM calendar where id = ?", id);
    }

    // RowMapper : DB에서 가져온 Row 를 객체로 바꿔줌
    private RowMapper<ResponseDto> calendarRowMapper() {
        return new RowMapper<ResponseDto>() {
            @Override
            public ResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new ResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("todo"),
                        rs.getString("email"),
                        // ResultSet에 getLocalDateTime() 메서드가 없다,
                        rs.getTimestamp("createAt").toLocalDateTime(),
                        rs.getTimestamp("updateAt").toLocalDateTime()
                );
            }
        };
    }

    private RowMapper<Calendar> calendarRowMapperV2() {
        return new RowMapper<Calendar>() {
            @Override
            public Calendar mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Calendar(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("todo"),
                        // ResultSet에 getLocalDateTime() 메서드가 없다,
                        rs.getTimestamp("createAt").toLocalDateTime(),
                        rs.getTimestamp("updateAt").toLocalDateTime()
                );
            }
        };
    }

    private RowMapper<Calendar> calendarRowMapperV3() {
        return new RowMapper<Calendar>() {
            @Override
            public Calendar mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Calendar(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("todo"),
                        rs.getString("password"),
                        // ResultSet에 getLocalDateTime() 메서드가 없다,
                        rs.getTimestamp("createAt").toLocalDateTime(),
                        rs.getTimestamp("updateAt").toLocalDateTime()
                );
            }
        };
    }

}
