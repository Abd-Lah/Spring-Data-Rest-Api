package com.spring.medical_appointment.service.doctor;

import com.spring.medical_appointment.commands.ReportCommand;
import com.spring.medical_appointment.exceptions.InvalidRequestException;
import com.spring.medical_appointment.exceptions.ResourceNotFoundException;
import com.spring.medical_appointment.mapper.ReportMapper;
import com.spring.medical_appointment.models.AppointmentEntity;
import com.spring.medical_appointment.models.AppointmentStatus;
import com.spring.medical_appointment.models.ReportEntity;
import com.spring.medical_appointment.models.UserEntity;
import com.spring.medical_appointment.repository.AppointmentRepository;
import com.spring.medical_appointment.repository.ReportRepository;
import com.spring.medical_appointment.service.user.UserService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final UserService userService;
    private final AppointmentRepository appointmentRepository;
    private final ReportRepository reportRepository;

    @Override
    public Page<AppointmentEntity> getMyAppointment(Pageable pageable, String orderBy) {
        UserEntity loggedDoctor = userService.getCurrentUser();
        return appointmentRepository.getAppointments(loggedDoctor ,orderBy ,pageable);
    }

    @Override
    @Transactional
    public AppointmentEntity changeStatus(AppointmentStatus status, String appointmentId) {
        UserEntity loggedDoctor = userService.getCurrentUser();
        AppointmentEntity appointment = appointmentRepository.findByAppointmentIdAndByUserId(appointmentId, loggedDoctor);
        if (appointment == null) {
            throw new ResourceNotFoundException("No appointment found");
        }
        if(status == AppointmentStatus.APPROVED && appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
            appointment.setStatus(status);
        }else{
            throw new ValidationException("Cannot change status of appointment to approved");
        }
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public ReportEntity addReport(ReportCommand reportCommand, String appointmentId) {
        UserEntity loggedDoctor = userService.getCurrentUser();
        AppointmentEntity appointment = appointmentRepository.findByAppointmentIdAndByUserId(appointmentId, loggedDoctor);
        if (appointment == null) {
            throw new ResourceNotFoundException("No appointment found");
        }
        ReportEntity report = ReportMapper.INSTANCE.ToReportEntity(reportCommand);
        appointment.setReport(report);
        appointmentRepository.save(appointment);
        return report;
    }

    @Override
    public ReportEntity editReport(ReportCommand reportCommand, String reportId) {
        UserEntity loggedDoctor = userService.getCurrentUser();
        ReportEntity reportEntity = reportRepository.findById(reportId).orElse(null);
        if (reportEntity == null) {
            throw new ResourceNotFoundException("Report not found");
        }
        AppointmentEntity appointment = appointmentRepository.findByAppointmentIdAndByUserId(reportEntity.getAppointment().getId(), loggedDoctor);
        if (appointment == null) {
            throw new InvalidRequestException("Unauthorized to edit this report !");
        }
        reportEntity.update(reportCommand);
        reportRepository.save(reportEntity);
        return reportEntity;
    }
}
