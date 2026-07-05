package com.wateracademy.service;

import com.wateracademy.entity.CalendarDay;
import com.wateracademy.entity.Course;
import com.wateracademy.entity.CourseAssignment;
import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.Trainer;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.entity.Venue;
import com.wateracademy.repository.CalendarDayRepository;
import com.wateracademy.repository.CourseAssignmentRepository;
import com.wateracademy.repository.CourseRepository;
import com.wateracademy.repository.ScheduleEntryRepository;
import com.wateracademy.repository.TrainerRepository;
import com.wateracademy.repository.VenueRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    private final WorkspaceService workspaceService;
    private final CourseRepository courseRepository;
    private final TrainerRepository trainerRepository;
    private final VenueRepository venueRepository;
    private final CalendarDayRepository calendarDayRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public ExportService(WorkspaceService workspaceService,
                         CourseRepository courseRepository,
                         TrainerRepository trainerRepository,
                         VenueRepository venueRepository,
                         CalendarDayRepository calendarDayRepository,
                         CourseAssignmentRepository courseAssignmentRepository,
                         ScheduleEntryRepository scheduleEntryRepository) {
        this.workspaceService = workspaceService;
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
        this.venueRepository = venueRepository;
        this.calendarDayRepository = calendarDayRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
    }

    public byte[] exportToExcel(Long workspaceId) {
        return exportToExcel(workspaceId, null, null);
    }

    public byte[] exportToExcel(Long workspaceId, Set<String> sheets, String type) {
        workspaceService.findEntity(workspaceId);

        List<Course> courses = Collections.emptyList();
        List<Trainer> trainers = Collections.emptyList();
        List<Venue> venues = Collections.emptyList();
        List<CalendarDay> calendarDays = Collections.emptyList();
        List<CourseAssignment> assignments = Collections.emptyList();
        List<ScheduleEntry> scheduleEntries = Collections.emptyList();

        if (type != null) {
            switch (type) {
                case "schedule" -> {
                    List<ScheduleEntry> all = scheduleEntryRepository.findByWorkspaceId(workspaceId);
                    scheduleEntries = all.stream()
                        .filter(e -> e.getStatus() == ScheduleStatus.CONFIRMED)
                        .collect(Collectors.toList());
                }
                case "conflicts" -> {
                    List<ScheduleEntry> all = scheduleEntryRepository.findByWorkspaceId(workspaceId);
                    scheduleEntries = all.stream()
                        .filter(e -> e.getConflictNotes() != null && !e.getConflictNotes().isBlank())
                        .collect(Collectors.toList());
                }
                case "unscheduled" -> {
                    courses = courseRepository.findUnscheduledByWorkspaceId(workspaceId);
                }
                default -> {
                    courses = courseRepository.findByWorkspaceId(workspaceId);
                    trainers = trainerRepository.findByWorkspaceId(workspaceId);
                    venues = venueRepository.findByWorkspaceId(workspaceId);
                    calendarDays = calendarDayRepository.findByWorkspaceId(workspaceId);
                    assignments = courseAssignmentRepository.findByWorkspaceId(workspaceId);
                    scheduleEntries = scheduleEntryRepository.findByWorkspaceId(workspaceId);
                }
            }
        } else if (sheets != null && !sheets.isEmpty()) {
            if (sheets.contains("courses")) courses = courseRepository.findByWorkspaceId(workspaceId);
            if (sheets.contains("trainers")) trainers = trainerRepository.findByWorkspaceId(workspaceId);
            if (sheets.contains("venues")) venues = venueRepository.findByWorkspaceId(workspaceId);
            if (sheets.contains("calendar")) calendarDays = calendarDayRepository.findByWorkspaceId(workspaceId);
            if (sheets.contains("assignments")) assignments = courseAssignmentRepository.findByWorkspaceId(workspaceId);
            if (sheets.contains("schedule-entries")) scheduleEntries = scheduleEntryRepository.findByWorkspaceId(workspaceId);
        } else {
            courses = courseRepository.findByWorkspaceId(workspaceId);
            trainers = trainerRepository.findByWorkspaceId(workspaceId);
            venues = venueRepository.findByWorkspaceId(workspaceId);
            calendarDays = calendarDayRepository.findByWorkspaceId(workspaceId);
            assignments = courseAssignmentRepository.findByWorkspaceId(workspaceId);
            scheduleEntries = scheduleEntryRepository.findByWorkspaceId(workspaceId);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            if (type != null) {
                switch (type) {
                    case "schedule" -> writeScheduleEntriesSheet(workbook, scheduleEntries, headerStyle);
                    case "conflicts" -> writeScheduleEntriesSheet(workbook, scheduleEntries, headerStyle);
                    case "unscheduled" -> writeCoursesSheet(workbook, courses, headerStyle);
                    default -> {
                        writeCoursesSheet(workbook, courses, headerStyle);
                        writeTrainersSheet(workbook, trainers, headerStyle);
                        writeVenuesSheet(workbook, venues, headerStyle);
                        writeCalendarSheet(workbook, calendarDays, headerStyle);
                        writeAssignmentsSheet(workbook, assignments, headerStyle);
                        writeScheduleEntriesSheet(workbook, scheduleEntries, headerStyle);
                    }
                }
            } else if (sheets != null && !sheets.isEmpty()) {
                if (sheets.contains("courses")) writeCoursesSheet(workbook, courses, headerStyle);
                if (sheets.contains("trainers")) writeTrainersSheet(workbook, trainers, headerStyle);
                if (sheets.contains("venues")) writeVenuesSheet(workbook, venues, headerStyle);
                if (sheets.contains("calendar")) writeCalendarSheet(workbook, calendarDays, headerStyle);
                if (sheets.contains("assignments")) writeAssignmentsSheet(workbook, assignments, headerStyle);
                if (sheets.contains("schedule-entries")) writeScheduleEntriesSheet(workbook, scheduleEntries, headerStyle);
            } else {
                writeCoursesSheet(workbook, courses, headerStyle);
                writeTrainersSheet(workbook, trainers, headerStyle);
                writeVenuesSheet(workbook, venues, headerStyle);
                writeCalendarSheet(workbook, calendarDays, headerStyle);
                writeAssignmentsSheet(workbook, assignments, headerStyle);
                writeScheduleEntriesSheet(workbook, scheduleEntries, headerStyle);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }

    private void writeCoursesSheet(Workbook workbook, List<Course> courses, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Courses");
        String[] headers = {"External ID", "", "Name", "Specialization", "Duration Days", "Hours Per Day",
            "Expected Trainees", "City", "Beneficiary", "Type", "Priority", "Earliest Start",
            "Latest End", "Fixed Date", "Notes"};
        writeHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (Course c : courses) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, c.getExternalId());
            setCell(row, 1, "");
            setCell(row, 2, c.getName());
            setCell(row, 3, c.getSpecialization());
            setCell(row, 4, c.getDurationDays());
            setCell(row, 5, c.getHoursPerDay());
            setCell(row, 6, c.getExpectedTrainees());
            setCell(row, 7, c.getCity());
            setCell(row, 8, c.getBeneficiary());
            setCell(row, 9, c.getType() != null ? c.getType().name() : null);
            setCell(row, 10, c.getPriority());
            setCell(row, 11, c.getEarliestStart());
            setCell(row, 12, c.getLatestEnd());
            setCell(row, 13, c.getFixedDate());
            setCell(row, 14, c.getNotes());
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void writeTrainersSheet(Workbook workbook, List<Trainer> trainers, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Trainers");
        String[] headers = {"External ID", "Name", "Specialties", "City", "Trainer Type",
            "Unavailable Dates", "Max Days Per Month", "Max Consecutive Days", "Cost Per Day", "Notes"};
        writeHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (Trainer t : trainers) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, t.getExternalId());
            setCell(row, 1, t.getName());
            setCell(row, 2, t.getSpecialties());
            setCell(row, 3, t.getCity());
            setCell(row, 4, t.getTrainerType());
            setCell(row, 5, t.getUnavailableDates());
            setCell(row, 6, t.getMaxDaysPerMonth());
            setCell(row, 7, t.getMaxConsecutiveDays());
            setCell(row, 8, t.getCostPerDay());
            setCell(row, 9, t.getNotes());
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void writeVenuesSheet(Workbook workbook, List<Venue> venues, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Venues");
        String[] headers = {"External ID", "Name", "City", "Type", "Capacity",
            "Available From", "Available To", "Unavailable Dates", "Equipment Notes"};
        writeHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (Venue v : venues) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, v.getExternalId());
            setCell(row, 1, v.getName());
            setCell(row, 2, v.getCity());
            setCell(row, 3, v.getType() != null ? v.getType().name() : null);
            setCell(row, 4, v.getCapacity());
            setCell(row, 5, v.getAvailableFrom());
            setCell(row, 6, v.getAvailableTo());
            setCell(row, 7, v.getUnavailableDates());
            setCell(row, 8, v.getEquipmentNotes());
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void writeCalendarSheet(Workbook workbook, List<CalendarDay> days, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Calendar");
        String[] headers = {"Date", "Day", "Work Day", "Holiday"};
        writeHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (CalendarDay d : days) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, d.getDate());
            setCell(row, 1, d.getDate() != null ? d.getDate().getDayOfWeek().toString() : null);
            setCell(row, 2, d.getIsWorkDay() != null && d.getIsWorkDay() ? "Yes" : "No");
            setCell(row, 3, d.getIsHoliday() != null && d.getIsHoliday() ? "Yes" : "No");
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void writeAssignmentsSheet(Workbook workbook, List<CourseAssignment> assignments, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("assigned course");
        String[] headers = {"", "Course External ID", "Trainer External ID"};
        writeHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (CourseAssignment a : assignments) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, "");
            setCell(row, 1, a.getCourse().getExternalId());
            setCell(row, 2, a.getTrainer().getExternalId());
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void writeScheduleEntriesSheet(Workbook workbook, List<ScheduleEntry> entries, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Schedule Entries");
        String[] headers = {"Course", "Trainer", "Venue", "Start Date", "End Date", "Status", "Conflict Notes"};
        writeHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (ScheduleEntry e : entries) {
            Row row = sheet.createRow(rowNum++);
            setCell(row, 0, e.getCourse().getName());
            setCell(row, 1, e.getTrainer().getName());
            setCell(row, 2, e.getVenue() != null ? e.getVenue().getName() : null);
            setCell(row, 3, e.getStartDate());
            setCell(row, 4, e.getEndDate());
            setCell(row, 5, e.getStatus() != null ? e.getStatus().name() : null);
            setCell(row, 6, e.getConflictNotes());
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void writeHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void setCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private void setCell(Row row, int col, Integer value) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private void setCell(Row row, int col, LocalDate value) {
        if (value != null) {
            setCell(row, col, value.format(DATE_FMT));
        }
    }
}