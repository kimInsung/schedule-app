package com.example.schedule.service;

import com.example.schedule.common.exception.InvalidPasswordException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleDeleteRequestDto;
import com.example.schedule.dto.ScheduleDetailResponseDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.dto.ScheduleUpdateRequestDto;
import com.example.schedule.entity.Comment;
import com.example.schedule.entity.Schedule;
import com.example.schedule.repository.CommentRepository;
import com.example.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 일정 관련 비즈니스 로직을 처리하는 Service 계층.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final CommentRepository commentRepository;

    /**
     * 새로운 일정을 생성하여 저장한 뒤 응답 DTO를 반환한다.
     *
     * @param requestDto 일정 생성 요청 정보
     * @return 비밀번호를 제외한 생성된 일정 정보
     */
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateRequestDto requestDto) {
        // 요청 DTO를 기반으로 일정 엔티티 생성
        Schedule schedule = new Schedule(
                requestDto.getTitle(),
                requestDto.getContents(),
                requestDto.getAuthor(),
                requestDto.getPassword()
        );

        // DB 저장 후 생성된 ID 및 Auditing 시간 정보가 포함된 엔티티 획득
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 비밀번호가 제외된 응답 DTO로 변환하여 반환
        return ScheduleResponseDto.from(savedSchedule);
    }

    /**
     * 전체 일정 또는 특정 작성자의 일정을 수정일 기준 내림차순으로 조회한다.
     *
     * @param author 선택적 작성자명 필터 (null 또는 공백이면 전체 조회)
     * @return 비밀번호를 제외한 일정 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedules(String author) {
        // 작성자명이 없거나 공백이면 전체 일정을, 값이 있으면 해당 작성자의 일정만 조회
        List<Schedule> schedules = (author == null || author.isBlank())
                ? scheduleRepository.findAllByOrderByModifiedAtDesc()
                : scheduleRepository.findAllByAuthorOrderByModifiedAtDesc(author);

        return schedules.stream()
                .map(ScheduleResponseDto::from)
                .toList();
    }

    /**
     * 지정한 ID의 단건 일정을 연관된 댓글 목록과 함께 조회한다.
     * 댓글은 작성일 오름차순으로 정렬되며, 댓글이 없을 경우 빈 배열([])을 반환한다.
     *
     * @param id 조회할 일정 ID
     * @return 비밀번호를 제외한 단건 일정 및 댓글 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public ScheduleDetailResponseDto getSchedule(Long id) {
        Schedule schedule = findScheduleById(id);

        // 단건 조회 전용 응답 DTO로 분리 조립하며, 댓글이 없어도 null 대신 빈 리스트([])를 보장
        List<Comment> foundComments = commentRepository.findAllByScheduleIdOrderByCreatedAtAsc(id);
        List<CommentResponseDto> comments = (foundComments == null)
                ? List.of()
                : foundComments.stream()
                        .map(CommentResponseDto::from)
                        .toList();

        return ScheduleDetailResponseDto.from(schedule, comments);
    }

    /**
     * 지정한 ID의 일정을 수정한다.
     * 비밀번호 일치 시에만 제목(title)과 작성자명(author)을 변경하며, flush로 갱신된 수정일시를 응답에 반영한다.
     *
     * @param id 수정할 일정 ID
     * @param requestDto 수정 요청 정보 (title, author, password)
     * @return 비밀번호를 제외한 수정 완료된 일정 응답 DTO
     */
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, ScheduleUpdateRequestDto requestDto) {
        Schedule schedule = findScheduleById(id);
        validatePassword(schedule, requestDto.getPassword());

        // 허용된 필드(title, author)만 제한적으로 수정
        schedule.update(requestDto.getTitle(), requestDto.getAuthor());

        // 트랜잭션 커밋 전 Dirty Checking을 즉시 DB에 반영하여 JPA Auditing의 modifiedAt 갱신값을 DTO에 포함
        scheduleRepository.flush();

        return ScheduleResponseDto.from(schedule);
    }

    /**
     * 지정한 ID의 일정을 삭제한다.
     * 일정 존재 여부를 확인하고 비밀번호가 일치하는 경우에만 연관 댓글을 일괄 삭제한 뒤 일정을 삭제한다.
     *
     * @param id 삭제할 일정 ID
     * @param requestDto 삭제 요청 정보 (password)
     */
    @Transactional
    public void deleteSchedule(Long id, ScheduleDeleteRequestDto requestDto) {
        // 존재하지 않는 일정이면 404 예외 발생
        Schedule schedule = findScheduleById(id);

        // 본인 확인용 비밀번호 일치 여부를 검증한 뒤에만 삭제 진행
        validatePassword(schedule, requestDto.getPassword());

        // 비밀번호 검증 통과 후 연관된 댓글을 먼저 일괄 삭제하여 데이터 정합성 보장
        commentRepository.deleteAllByScheduleId(id);
        scheduleRepository.delete(schedule);
    }

    // 요청 비밀번호가 null이거나 저장된 비밀번호와 다르면 401 예외 발생
    private void validatePassword(Schedule schedule, String requestPassword) {
        if (requestPassword == null || !requestPassword.equals(schedule.getPassword())) {
            throw new InvalidPasswordException();
        }
    }

    // 존재하지 않는 ID 조회 시 커스텀 예외를 발생시켜 전역 예외 처리기에서 404 응답 처리
    private Schedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }
}
