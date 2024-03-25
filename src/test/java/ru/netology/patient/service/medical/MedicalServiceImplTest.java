package ru.netology.patient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class MedicalServiceImplTest {
    private PatientInfo patient;
    private SendAlertService alertService;
    private PatientInfoFileRepository patientInfoFileRepository;

    @BeforeEach
    public void patient() {
        patientInfoFileRepository = mock(PatientInfoFileRepository.class);
        patient = new PatientInfo(
                "Дмитрий",
                "Лакутин",
                LocalDate.of(1990, 07, 12),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80)));
        alertService = mock(SendAlertService.class);
    }

    @ParameterizedTest
    @MethodSource("forBloodPressureCheck")
    void checkBloodPressure(BloodPressure bloodPressure, int numberOfMethodCalls) {
        when(patientInfoFileRepository.getById(any())).thenReturn(patient);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, alertService);
        medicalService.checkBloodPressure("id", bloodPressure);
        verify(alertService, times(numberOfMethodCalls)).send(contains("Warning"));
    }

    @ParameterizedTest
    @MethodSource("forCheckTemperature")
    void checkTemperature(BigDecimal temperature, int numberOfMethodCalls) {
        when(patientInfoFileRepository.getById(any())).thenReturn(patient);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, alertService);
        medicalService.checkTemperature("id", temperature);
        verify(alertService,times(numberOfMethodCalls)).send(anyString());
    }

    @Test
    void checkNormalBloodPressure() {
        when(patientInfoFileRepository.getById(any())).thenReturn(patient);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, alertService);
        medicalService.checkBloodPressure("id", patient.getHealthInfo().getBloodPressure());
        verify(alertService, never()).send(anyString());
    }

    @Test
    void checkNormalTemperature() {
        when(patientInfoFileRepository.getById(any())).thenReturn(patient);
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, alertService);
        medicalService.checkTemperature("id", patient.getHealthInfo().getNormalTemperature());
        verify(alertService, never()).send(anyString());
    }

    public static Stream<Arguments> forBloodPressureCheck() {
        return Stream.of(
                Arguments.of(new BloodPressure(150, 90), 1),
                Arguments.of(new BloodPressure(110, 70), 1),
                Arguments.of(new BloodPressure(120, 80), 0),
                Arguments.of(new BloodPressure(130, 90), 1),
                Arguments.of(new BloodPressure(120, 70), 1)
        );
    }

    public static Stream<Arguments> forCheckTemperature() {
        return Stream.of(
                Arguments.of(new BigDecimal("38.1"),0),
                Arguments.of(new BigDecimal("39.1"),0),
                Arguments.of(new BigDecimal("37.1"),0),
                Arguments.of(new BigDecimal("36.6"),0),
                Arguments.of(new BigDecimal("35.0"),1)
        );
    }
}