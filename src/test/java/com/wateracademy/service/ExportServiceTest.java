package com.wateracademy.service;

import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.exception.ResourceNotFoundException;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ExportServiceTest {

    @Autowired
    private ExportService exportService;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private ScheduleEntryService scheduleEntryService;

    @Autowired
    private CourseAssignmentService courseAssignmentService;

    private UUID createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    @Test
    void exportToExcel_shouldCreateWorkbookWithAllSheets() throws Exception {
        var wsId = createWorkspace("Export Test");

        byte[] data = exportService.exportToExcel(wsId);

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Courses")).isNotNull();
            assertThat(wb.getSheet("Trainers")).isNotNull();
            assertThat(wb.getSheet("Venues")).isNotNull();
            assertThat(wb.getSheet("Calendar")).isNotNull();
            assertThat(wb.getSheet("assigned course")).isNotNull();
            assertThat(wb.getSheet("Schedule Entries")).isNotNull();
        }
    }

    @Test
    void exportToExcel_shouldIncludeExportedData() throws Exception {
        var wsId = createWorkspace("Data Export");

        courseService.create(wsId, new CourseRequest("Export Course", "Eng", 3, 5, 10,
                "Riyadh", "Benef", "HIGH", CourseType.IN_PERSON,
                null, null, null, null, null));
        trainerService.create(wsId, new TrainerRequest("Export Trainer", "Jeddah",
                "Leadership", "Internal", null, 10, 4, 300, null));
        venueService.create(wsId, new VenueRequest("Export Venue", "Dammam", 40,
                CourseType.IN_PERSON, null, null, null, "AC"));

        byte[] data = exportService.exportToExcel(wsId);

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Courses").getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(wb.getSheet("Courses").getRow(1).getCell(2).getStringCellValue())
                    .isEqualTo("Export Course");

            assertThat(wb.getSheet("Trainers").getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(wb.getSheet("Trainers").getRow(1).getCell(1).getStringCellValue())
                    .isEqualTo("Export Trainer");

            assertThat(wb.getSheet("Venues").getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(wb.getSheet("Venues").getRow(1).getCell(1).getStringCellValue())
                    .isEqualTo("Export Venue");
        }
    }

    @Test
    void exportToExcel_emptyWorkspace_returnsHeadersOnly() throws Exception {
        var wsId = createWorkspace("Empty WS");

        byte[] data = exportService.exportToExcel(wsId);

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Courses").getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(wb.getSheet("Trainers").getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(wb.getSheet("Venues").getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(wb.getSheet("Calendar").getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(wb.getSheet("assigned course").getPhysicalNumberOfRows()).isEqualTo(1);
            assertThat(wb.getSheet("Schedule Entries").getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    void exportToExcel_unknownWorkspace_throwsException() {
        assertThatThrownBy(() -> exportService.exportToExcel(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void exportToExcel_withSheetFilter_onlyIncludesRequestedSheets() throws Exception {
        var wsId = createWorkspace("Sheet Filter");
        courseService.create(wsId, new CourseRequest("C1", null, 3, null, null, null, null, null, CourseType.IN_PERSON, null, null, null, null, null));
        trainerService.create(wsId, new TrainerRequest("T1", null, null, null, null, null, null, null, null));

        byte[] data = exportService.exportToExcel(wsId, Set.of("courses"), null);

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Courses")).isNotNull();
            assertThat(wb.getSheet("Trainers")).isNull();
            assertThat(wb.getSheet("Venues")).isNull();
            assertThat(wb.getSheet("Calendar")).isNull();
            assertThat(wb.getSheet("assigned course")).isNull();
            assertThat(wb.getSheet("Schedule Entries")).isNull();
            assertThat(wb.getSheet("Courses").getPhysicalNumberOfRows()).isEqualTo(2);
        }
    }

    @Test
    void exportToExcel_withTypeSchedule_onlyIncludesConfirmedEntries() throws Exception {
        var wsId = createWorkspace("Schedule Type");
        var courseId = courseService.create(wsId, new CourseRequest("SC", null, 3, null, null, null, null, null, CourseType.IN_PERSON, null, null, null, null, null)).id();
        var trainerId = trainerService.create(wsId, new TrainerRequest("ST", null, null, null, null, null, null, null, null)).id();
        var venueId = venueService.create(wsId, new VenueRequest("SV", null, 20, CourseType.IN_PERSON, null, null, null, null)).id();

        var entry1 = scheduleEntryService.create(wsId, new ScheduleEntryRequest(courseId, trainerId, venueId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), null));
        scheduleEntryService.updateStatus(entry1.id(), ScheduleStatus.CONFIRMED);
        scheduleEntryService.create(wsId, new ScheduleEntryRequest(courseId, trainerId, venueId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), null));

        byte[] data = exportService.exportToExcel(wsId, null, "schedule");

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Courses")).isNull();
            assertThat(wb.getSheet("Schedule Entries")).isNotNull();
            assertThat(wb.getSheet("Schedule Entries").getPhysicalNumberOfRows()).isEqualTo(2);
        }
    }

    @Test
    void exportToExcel_withTypeConflicts_onlyIncludesEntriesWithConflicts() throws Exception {
        var wsId = createWorkspace("Conflicts Type");
        var courseId = courseService.create(wsId, new CourseRequest("CC", null, 3, null, null, null, null, null, CourseType.IN_PERSON, null, null, null, null, null)).id();
        var trainerId = trainerService.create(wsId, new TrainerRequest("CT", null, null, null, null, null, null, null, null)).id();
        var venueId = venueService.create(wsId, new VenueRequest("CV", null, 20, CourseType.IN_PERSON, null, null, null, null)).id();

        scheduleEntryService.create(wsId, new ScheduleEntryRequest(courseId, trainerId, venueId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5), null));
        scheduleEntryService.create(wsId, new ScheduleEntryRequest(courseId, trainerId, venueId, LocalDate.of(2026, 6, 3), LocalDate.of(2026, 6, 7), null));

        byte[] data = exportService.exportToExcel(wsId, null, "conflicts");

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Schedule Entries")).isNotNull();
            assertThat(wb.getSheet("Schedule Entries").getPhysicalNumberOfRows()).isEqualTo(2);
        }
    }

    @Test
    void exportToExcel_withTypeUnscheduled_onlyIncludesUnscheduledCourses() throws Exception {
        var wsId = createWorkspace("Unscheduled Type");
        var courseId = courseService.create(wsId, new CourseRequest("Scheduled", null, 3, null, null, null, null, null, CourseType.IN_PERSON, null, null, null, null, null)).id();
        courseService.create(wsId, new CourseRequest("Unscheduled", null, 3, null, null, null, null, null, CourseType.IN_PERSON, null, null, null, null, null));
        var trainerId = trainerService.create(wsId, new TrainerRequest("UT", null, null, null, null, null, null, null, null)).id();
        var venueId = venueService.create(wsId, new VenueRequest("UV", null, 20, CourseType.IN_PERSON, null, null, null, null)).id();

        scheduleEntryService.create(wsId, new ScheduleEntryRequest(courseId, trainerId, venueId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), null));

        byte[] data = exportService.exportToExcel(wsId, null, "unscheduled");

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            assertThat(wb.getSheet("Courses")).isNotNull();
            assertThat(wb.getSheet("Schedule Entries")).isNull();
            assertThat(wb.getSheet("Courses").getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(wb.getSheet("Courses").getRow(1).getCell(2).getStringCellValue()).isEqualTo("Unscheduled");
        }
    }
}