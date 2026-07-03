package com.wateracademy.service;

import com.wateracademy.dto.response.ImportResult;
import com.wateracademy.entity.CalendarDay;
import com.wateracademy.entity.Course;
import com.wateracademy.entity.CourseAssignment;
import com.wateracademy.entity.Trainer;
import com.wateracademy.entity.Venue;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.exception.ExcelParseException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CalendarDayRepository;
import com.wateracademy.repository.CourseAssignmentRepository;
import com.wateracademy.repository.CourseRepository;
import com.wateracademy.repository.TrainerRepository;
import com.wateracademy.repository.VenueRepository;
import com.wateracademy.repository.WorkspaceRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ImportServiceTest {

    @Autowired
    private ImportService importService;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CalendarDayRepository calendarDayRepository;

    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

    @Test
    void importFullExcel_shouldImportAllSheets() throws IOException {
        Workspace workspace = persistWorkspace("Test WS", 2026);

        InputStream excel = new ClassPathResource("training_input_updated.xlsx").getInputStream();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.hasError()).isFalse();
        assertThat(result.coursesParsed()).isEqualTo(5);
        assertThat(result.coursesInserted()).isEqualTo(5);
        assertThat(result.trainersParsed()).isEqualTo(4);
        assertThat(result.trainersInserted()).isEqualTo(4);
        assertThat(result.venuesParsed()).isEqualTo(5);
        assertThat(result.venuesInserted()).isEqualTo(5);
        assertThat(result.calendarDaysParsed()).isEqualTo(120);
        assertThat(result.calendarDaysInserted()).isEqualTo(120);
        assertThat(result.assignmentsParsed()).isEqualTo(5);
        assertThat(result.assignmentsInserted()).isEqualTo(5);

        assertThat(courseRepository.findByWorkspaceId(workspace.getId())).hasSize(5);
        assertThat(trainerRepository.findByWorkspaceId(workspace.getId())).hasSize(4);
        assertThat(venueRepository.findByWorkspaceId(workspace.getId())).hasSize(5);
        assertThat(calendarDayRepository.findByWorkspaceId(workspace.getId())).hasSize(120);
        assertThat(courseAssignmentRepository.findByWorkspaceId(workspace.getId())).hasSize(5);
    }

    @Test
    void importExcel_shouldMapAllCourseFields() throws IOException {
        Workspace workspace = persistWorkspace("Courses Test", 2026);

        InputStream excel = createWorkbookWithCourses();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.coursesInserted()).isEqualTo(2);

        List<Course> courses = courseRepository.findByWorkspaceId(workspace.getId());
        Course c1 = courses.stream().filter(c -> "C-001".equals(c.getExternalId())).findFirst().orElseThrow();
        assertThat(c1.getName()).isEqualTo("Time Management");
        assertThat(c1.getSpecialization()).isEqualTo("Management");
        assertThat(c1.getDurationDays()).isEqualTo(1);
        assertThat(c1.getHoursPerDay()).isEqualTo(5);
        assertThat(c1.getExpectedTrainees()).isEqualTo(15);
        assertThat(c1.getCity()).isEqualTo("Riyadh");
        assertThat(c1.getBeneficiary()).isEqualTo("HR");
        assertThat(c1.getType()).isEqualTo(CourseType.IN_PERSON);
        assertThat(c1.getPriority()).isEqualTo("Medium");

        Course c2 = courses.stream().filter(c -> "C-002".equals(c.getExternalId())).findFirst().orElseThrow();
        assertThat(c2.getType()).isEqualTo(CourseType.ONLINE);
    }

    @Test
    void importExcel_shouldMapAllTrainerFields() throws IOException {
        Workspace workspace = persistWorkspace("Trainers Test", 2026);

        InputStream excel = createWorkbookWithTrainers();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.trainersInserted()).isEqualTo(1);

        List<Trainer> trainers = trainerRepository.findByWorkspaceId(workspace.getId());
        Trainer trainer = trainers.get(0);
        assertThat(trainer.getExternalId()).isEqualTo("T-001");
        assertThat(trainer.getName()).isEqualTo("Ali Trainer");
        assertThat(trainer.getSpecialties()).isEqualTo("Management, Leadership");
        assertThat(trainer.getCity()).isEqualTo("Riyadh");
        assertThat(trainer.getTrainerType()).isEqualTo("Internal");
        assertThat(trainer.getUnavailableDates()).isEqualTo("2026-02-15; 2026-02-16");
        assertThat(trainer.getMaxDaysPerMonth()).isEqualTo(12);
        assertThat(trainer.getMaxConsecutiveDays()).isEqualTo(5);
        assertThat(trainer.getCostPerDay()).isEqualTo(0);
    }

    @Test
    void importExcel_shouldMapAllVenueFields() throws IOException {
        Workspace workspace = persistWorkspace("Venues Test", 2026);

        InputStream excel = createWorkbookWithVenues();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.venuesInserted()).isEqualTo(2);

        List<Venue> venues = venueRepository.findByWorkspaceId(workspace.getId());
        Venue room = venues.stream().filter(v -> "V-001".equals(v.getExternalId())).findFirst().orElseThrow();
        assertThat(room.getName()).isEqualTo("Room A");
        assertThat(room.getCity()).isEqualTo("Jubail");
        assertThat(room.getCapacity()).isEqualTo(20);
        assertThat(room.getType()).isEqualTo(CourseType.IN_PERSON);
        assertThat(room.getAvailableFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(room.getAvailableTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(room.getEquipmentNotes()).isEqualTo("Projector, Whiteboard");

        Venue online = venues.stream().filter(v -> "V-002".equals(v.getExternalId())).findFirst().orElseThrow();
        assertThat(online.getType()).isEqualTo(CourseType.ONLINE);
        assertThat(online.getCapacity()).isEqualTo(999);
    }

    @Test
    void importExcel_shouldMapCalendarDays() throws IOException {
        Workspace workspace = persistWorkspace("Calendar Test", 2026);

        InputStream excel = createWorkbookWithCalendar();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.calendarDaysInserted()).isEqualTo(3);

        List<CalendarDay> days = calendarDayRepository.findByWorkspaceId(workspace.getId());
        CalendarDay day1 = days.stream().filter(d -> d.getDate().equals(LocalDate.of(2026, 1, 1))).findFirst().orElseThrow();
        assertThat(day1.getIsWorkDay()).isTrue();
        assertThat(day1.getIsHoliday()).isFalse();

        CalendarDay day2 = days.stream().filter(d -> d.getDate().equals(LocalDate.of(2026, 1, 2))).findFirst().orElseThrow();
        assertThat(day2.getIsWorkDay()).isFalse();
        assertThat(day2.getIsHoliday()).isTrue();
    }

    @Test
    void importExcel_shouldCreateCourseAssignments() throws IOException {
        Workspace workspace = persistWorkspace("Assignments Test", 2026);

        InputStream excel = createWorkbookWithAssignments();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.assignmentsInserted()).isEqualTo(1);

        List<CourseAssignment> assignments = courseAssignmentRepository.findByWorkspaceId(workspace.getId());
        assertThat(assignments).hasSize(1);

        CourseAssignment assignment = assignments.get(0);
        assertThat(assignment.getCourse().getExternalId()).isEqualTo("C-001");
        assertThat(assignment.getTrainer().getExternalId()).isEqualTo("T-001");
    }

    @Test
    void importExcel_unknownWorkspace_throwsException() throws IOException {
        InputStream excel = createMinimalWorkbook();
        UUID fakeId = UUID.randomUUID();

        assertThatThrownBy(() -> importService.importExcel(excel, fakeId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void importExcel_malformedStream_throwsExcelParseException() {
        Workspace workspace = persistWorkspace("Malformed Test", 2026);
        InputStream badStream = new ByteArrayInputStream("not an excel file".getBytes());

        assertThatThrownBy(() -> importService.importExcel(badStream, workspace.getId()))
            .isInstanceOf(ExcelParseException.class);
    }

    @Test
    void importExcel_emptyWorkbook_returnsZeroCounts() throws IOException {
        Workspace workspace = persistWorkspace("Empty Test", 2026);

        Workbook wb = new XSSFWorkbook();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();

        ImportResult result = importService.importExcel(new ByteArrayInputStream(bos.toByteArray()), workspace.getId());
        assertThat(result.hasError()).isFalse();
        assertThat(result.coursesInserted()).isZero();
        assertThat(result.trainersInserted()).isZero();
        assertThat(result.venuesInserted()).isZero();
        assertThat(result.calendarDaysInserted()).isZero();
        assertThat(result.assignmentsInserted()).isZero();
    }

    @Test
    void importExcel_missingAssignmentSheet_shouldSucceed() throws IOException {
        Workspace workspace = persistWorkspace("No Assign", 2026);

        InputStream excel = createWorkbookWithoutAssignmentSheet();
        ImportResult result = importService.importExcel(excel, workspace.getId());

        assertThat(result.coursesInserted()).isEqualTo(1);
        assertThat(result.trainersInserted()).isEqualTo(1);
        assertThat(result.venuesInserted()).isEqualTo(1);
        assertThat(result.assignmentsParsed()).isEqualTo(0);
        assertThat(result.assignmentsInserted()).isEqualTo(0);
    }

    @Test
    void importExcel_importIsAtomic_rollsBackOnFailure() throws IOException {
        Workspace workspace = persistWorkspace("Atomic Test", 2026);

        Workbook wb = new XSSFWorkbook();
        Sheet cs = wb.createSheet("Courses");
        cs.createRow(0).createCell(0).setCellValue("Course ID");
        Row cRow = cs.createRow(1);
        cRow.createCell(0).setCellValue("C-001");
        cRow.createCell(2).setCellValue("Valid Course");
        cRow.createCell(4).setCellValue(3);
        cRow.createCell(9).setCellValue("In-person");

        Sheet ts = wb.createSheet("Trainers");
        ts.createRow(0).createCell(0).setCellValue("Trainer ID");
        Row tRow = ts.createRow(1);
        tRow.createCell(0).setCellValue("T-001");
        tRow.createCell(1).setCellValue("Valid Trainer");

        Sheet vs = wb.createSheet("Venues");
        vs.createRow(0).createCell(0).setCellValue("Venue ID");
        Row vRow = vs.createRow(1);
        vRow.createCell(0).setCellValue("V-001");
        vRow.createCell(1).setCellValue("Valid Venue");
        vRow.createCell(4).setCellValue(20);

        Sheet cals = wb.createSheet("Calendar");
        cals.createRow(0).createCell(0).setCellValue("Date");
        Row calRow = cals.createRow(1);
        calRow.createCell(0).setCellValue("2026-01-01");
        calRow.createCell(2).setCellValue("Yes");
        calRow.createCell(3).setCellValue("No");

        Sheet as = wb.createSheet("assigned course");
        as.createRow(0).createCell(0).setCellValue("Assigned ID");
        Row aRow = as.createRow(1);
        aRow.createCell(1).setCellValue("C-001");
        aRow.createCell(2).setCellValue("NONEXISTENT_TRAINER");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();

        assertThatThrownBy(() -> importService.importExcel(new ByteArrayInputStream(bos.toByteArray()), workspace.getId()))
            .isInstanceOf(ExcelParseException.class)
            .hasMessageContaining("NONEXISTENT_TRAINER");
    }

    private Workspace persistWorkspace(String name, int year) {
        Workspace ws = new Workspace();
        ws.setName(name);
        ws.setYear(year);
        return workspaceRepository.save(ws);
    }

    private InputStream createMinimalWorkbook() throws IOException {
        Workbook wb = new XSSFWorkbook();

        Sheet cs = wb.createSheet("Courses");
        cs.createRow(0).createCell(0).setCellValue("Course ID");
        Row cRow = cs.createRow(1);
        cRow.createCell(0).setCellValue("C-001");
        cRow.createCell(2).setCellValue("Minimal Course");
        cRow.createCell(4).setCellValue(1);
        cRow.createCell(9).setCellValue("In-person");

        Sheet ts = wb.createSheet("Trainers");
        ts.createRow(0).createCell(0).setCellValue("Trainer ID");
        Row tRow = ts.createRow(1);
        tRow.createCell(0).setCellValue("T-001");
        tRow.createCell(1).setCellValue("Minimal Trainer");

        Sheet vs = wb.createSheet("Venues");
        vs.createRow(0).createCell(0).setCellValue("Venue ID");
        Row vRow = vs.createRow(1);
        vRow.createCell(0).setCellValue("V-001");
        vRow.createCell(1).setCellValue("Minimal Venue");
        vRow.createCell(4).setCellValue(10);

        Sheet cals = wb.createSheet("Calendar");
        cals.createRow(0).createCell(0).setCellValue("Date");
        Row calRow = cals.createRow(1);
        calRow.createCell(0).setCellValue("2026-01-01");
        calRow.createCell(2).setCellValue("Yes");
        calRow.createCell(3).setCellValue("No");

        Sheet as = wb.createSheet("assigned course");
        as.createRow(0).createCell(0).setCellValue("Assigned ID");
        Row aRow = as.createRow(1);
        aRow.createCell(1).setCellValue("C-001");
        aRow.createCell(2).setCellValue("T-001");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private InputStream createWorkbookWithCourses() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet cs = wb.createSheet("Courses");
        cs.createRow(0).createCell(0).setCellValue("Course ID");
        Row r1 = cs.createRow(1);
        r1.createCell(0).setCellValue("C-001");
        r1.createCell(2).setCellValue("Time Management");
        r1.createCell(3).setCellValue("Management");
        r1.createCell(4).setCellValue(1);
        r1.createCell(5).setCellValue(5);
        r1.createCell(6).setCellValue(15);
        r1.createCell(7).setCellValue("Riyadh");
        r1.createCell(8).setCellValue("HR");
        r1.createCell(9).setCellValue("In-person");
        r1.createCell(10).setCellValue("Medium");

        Row r2 = cs.createRow(2);
        r2.createCell(0).setCellValue("C-002");
        r2.createCell(2).setCellValue("AI Basics");
        r2.createCell(3).setCellValue("AI");
        r2.createCell(4).setCellValue(3);
        r2.createCell(5).setCellValue(4);
        r2.createCell(6).setCellValue(25);
        r2.createCell(7).setCellValue("Online");
        r2.createCell(8).setCellValue("IT");
        r2.createCell(9).setCellValue("Online");
        r2.createCell(10).setCellValue("Low");

        wb.createSheet("Trainers");
        wb.createSheet("Venues");
        wb.createSheet("Calendar");
        wb.createSheet("assigned course");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private InputStream createWorkbookWithTrainers() throws IOException {
        Workbook wb = new XSSFWorkbook();
        wb.createSheet("Courses");
        Sheet ts = wb.createSheet("Trainers");
        ts.createRow(0).createCell(0).setCellValue("Trainer ID");
        Row r = ts.createRow(1);
        r.createCell(0).setCellValue("T-001");
        r.createCell(1).setCellValue("Ali Trainer");
        r.createCell(2).setCellValue("Management, Leadership");
        r.createCell(3).setCellValue("Riyadh");
        r.createCell(4).setCellValue("Internal");
        r.createCell(5).setCellValue("2026-02-15; 2026-02-16");
        r.createCell(6).setCellValue(12);
        r.createCell(7).setCellValue(5);
        r.createCell(8).setCellValue(0);

        wb.createSheet("Venues");
        wb.createSheet("Calendar");
        wb.createSheet("assigned course");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private InputStream createWorkbookWithVenues() throws IOException {
        Workbook wb = new XSSFWorkbook();
        wb.createSheet("Courses");
        wb.createSheet("Trainers");
        Sheet vs = wb.createSheet("Venues");
        vs.createRow(0).createCell(0).setCellValue("Venue ID");
        Row r1 = vs.createRow(1);
        r1.createCell(0).setCellValue("V-001");
        r1.createCell(1).setCellValue("Room A");
        r1.createCell(2).setCellValue("Jubail");
        r1.createCell(3).setCellValue("Training Room");
        r1.createCell(4).setCellValue(20);
        r1.createCell(5).setCellValue("2026-01-01");
        r1.createCell(6).setCellValue("2026-12-31");
        r1.createCell(8).setCellValue("Projector, Whiteboard");

        Row r2 = vs.createRow(2);
        r2.createCell(0).setCellValue("V-002");
        r2.createCell(1).setCellValue("Online");
        r2.createCell(2).setCellValue("Online");
        r2.createCell(3).setCellValue("Online");
        r2.createCell(4).setCellValue(999);

        wb.createSheet("Calendar");
        wb.createSheet("assigned course");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private InputStream createWorkbookWithCalendar() throws IOException {
        Workbook wb = new XSSFWorkbook();
        wb.createSheet("Courses");
        wb.createSheet("Trainers");
        wb.createSheet("Venues");
        Sheet cals = wb.createSheet("Calendar");
        cals.createRow(0).createCell(0).setCellValue("Date");

        Row r1 = cals.createRow(1);
        r1.createCell(0).setCellValue("2026-01-01");
        r1.createCell(2).setCellValue("Yes");
        r1.createCell(3).setCellValue("No");

        Row r2 = cals.createRow(2);
        r2.createCell(0).setCellValue("2026-01-02");
        r2.createCell(2).setCellValue("No");
        r2.createCell(3).setCellValue("Yes");

        Row r3 = cals.createRow(3);
        r3.createCell(0).setCellValue("2026-01-03");
        r3.createCell(2).setCellValue("No");
        r3.createCell(3).setCellValue("No");

        wb.createSheet("assigned course");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private InputStream createWorkbookWithAssignments() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet cs = wb.createSheet("Courses");
        cs.createRow(0).createCell(0).setCellValue("Course ID");
        Row cRow = cs.createRow(1);
        cRow.createCell(0).setCellValue("C-001");
        cRow.createCell(2).setCellValue("Course One");
        cRow.createCell(4).setCellValue(2);
        cRow.createCell(9).setCellValue("In-person");

        Sheet ts = wb.createSheet("Trainers");
        ts.createRow(0).createCell(0).setCellValue("Trainer ID");
        Row tRow = ts.createRow(1);
        tRow.createCell(0).setCellValue("T-001");
        tRow.createCell(1).setCellValue("Trainer One");

        Sheet vs = wb.createSheet("Venues");
        vs.createRow(0).createCell(0).setCellValue("Venue ID");
        Row vRow = vs.createRow(1);
        vRow.createCell(0).setCellValue("V-001");
        vRow.createCell(1).setCellValue("Dummy Venue");
        vRow.createCell(4).setCellValue(10);

        Sheet cals = wb.createSheet("Calendar");
        cals.createRow(0).createCell(0).setCellValue("Date");
        Row calRow = cals.createRow(1);
        calRow.createCell(0).setCellValue("2026-01-01");
        calRow.createCell(2).setCellValue("Yes");
        calRow.createCell(3).setCellValue("No");

        Sheet as = wb.createSheet("assigned course");
        as.createRow(0).createCell(0).setCellValue("Assigned ID");
        Row aRow = as.createRow(1);
        aRow.createCell(1).setCellValue("C-001");
        aRow.createCell(2).setCellValue("T-001");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private InputStream createWorkbookWithoutAssignmentSheet() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet cs = wb.createSheet("Courses");
        cs.createRow(0).createCell(0).setCellValue("Course ID");
        Row cRow = cs.createRow(1);
        cRow.createCell(0).setCellValue("C-001");
        cRow.createCell(2).setCellValue("Only Course");
        cRow.createCell(4).setCellValue(2);
        cRow.createCell(9).setCellValue("In-person");

        Sheet ts = wb.createSheet("Trainers");
        ts.createRow(0).createCell(0).setCellValue("Trainer ID");
        Row tRow = ts.createRow(1);
        tRow.createCell(0).setCellValue("T-001");
        tRow.createCell(1).setCellValue("Only Trainer");

        Sheet vs = wb.createSheet("Venues");
        vs.createRow(0).createCell(0).setCellValue("Venue ID");
        Row vRow = vs.createRow(1);
        vRow.createCell(0).setCellValue("V-001");
        vRow.createCell(1).setCellValue("Only Venue");
        vRow.createCell(4).setCellValue(15);

        Sheet cals = wb.createSheet("Calendar");
        cals.createRow(0).createCell(0).setCellValue("Date");
        Row calRow = cals.createRow(1);
        calRow.createCell(0).setCellValue("2026-06-01");
        calRow.createCell(2).setCellValue("Yes");
        calRow.createCell(3).setCellValue("No");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }
}