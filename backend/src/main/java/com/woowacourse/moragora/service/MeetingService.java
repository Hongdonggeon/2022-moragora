package com.woowacourse.moragora.service;

import com.woowacourse.moragora.dto.MeetingRequest;
import com.woowacourse.moragora.dto.MeetingResponse;
import com.woowacourse.moragora.dto.UserAttendanceRequest;
import com.woowacourse.moragora.dto.UserAttendancesRequest;
import com.woowacourse.moragora.dto.UserResponse;
import com.woowacourse.moragora.entity.Attendance;
import com.woowacourse.moragora.entity.Meeting;
import com.woowacourse.moragora.exception.MeetingNotFoundException;
import com.woowacourse.moragora.repository.AttendanceRepository;
import com.woowacourse.moragora.repository.MeetingRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final AttendanceRepository attendanceRepository;

    public MeetingService(final MeetingRepository meetingRepository, final AttendanceRepository attendanceRepository) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public Long save(final MeetingRequest request) {
        final Meeting meeting = request.toEntity();
        final Meeting savedMeeting = meetingRepository.save(meeting);
        return savedMeeting.getId();
    }

    // TODO 1 + N 최적화 대상
    public MeetingResponse findById(final Long id) {
        final Meeting meeting = findMeeting(id);
        List<Attendance> attendances = attendanceRepository.findByMeetingId(meeting.getId());
        List<UserResponse> userResponses = attendances.stream()
                .map(UserResponse::from)
                .collect(Collectors.toUnmodifiableList());

        return MeetingResponse.from(meeting, userResponses);
    }

    // TODO update (1 + N) -> 최적하기
    @Transactional
    public void updateAttendance(final Long meetingId, final UserAttendancesRequest requests) {
        final Meeting meeting = findMeeting(meetingId);
        meeting.increaseMeetingCount();

        final List<Long> absentUserIds = requests.getUsers().stream()
                .filter(UserAttendanceRequest::getIsTardy)
                .map(UserAttendanceRequest::getId)
                .collect(Collectors.toList());

        final List<Attendance> attendances = attendanceRepository.findByMeetingId(meetingId);
        for (final Attendance attendance : attendances) {
            if (absentUserIds.contains(attendance.getUser().getId())) {
                attendance.increaseTardyCount();
            }
        }
    }

    private Meeting findMeeting(final Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(MeetingNotFoundException::new);
    }
}
