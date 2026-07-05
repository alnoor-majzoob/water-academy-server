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
import com.wateracademy.repository.CalendarDayRepository;
import com.wateracademy.repository.CourseAssignmentRepository;
import com.wateracademy.repository.CourseRepository;
import com.wateracademy.repository.TrainerRepository;
import com.wateracademy.repository.VenueRepository;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportService {

    private final WorkspaceService workspaceService;
    private final CourseRepository courseRepository;
    private final TrainerRepository trainerRepository;
    private final VenueRepository venueRepository;
    private final CalendarDayRepository calendarDayRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;

    public ImportService(WorkspaceService workspaceService,
                         CourseRepository courseRepository,
                         TrainerRepository trainerRepository,
                         VenueRepository venueRepository,
                         CalendarDayRepository calendarDayRepository,
                         CourseAssignmentRepository courseAssignmentRepository) {
        this.workspaceService = workspaceService;
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
        this.venueRepository = venueRepository;
        this.calendarDayRepository = calendarDayRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
    }

    @Transactional
    public ImportResult importExcel(InputStream excelStream, Long workspaceId) {
        Workspace workspace = workspaceService.findEntity(workspaceId);

        try (Workbook workbook = WorkbookFactory.create(excelStream)) {
            Sheet courseSheet = workbook.getSheet("Courses");
            Sheet trainerSheet = workbook.getSheet("Trainers");
            Sheet venueSheet = workbook.getSheet("Venues");
            Sheet calendarSheet = workbook.getSheet("Calendar");
            Sheet assignmentSheet = workbook.getSheet("assigned course");

            List<Course> courses = parseCourses(courseSheet, workspace);
            List<Trainer> trainers = parseTrainers(trainerSheet, workspace);
            List<Venue> venues = parseVenues(venueSheet, workspace);
            List<CalendarDay> calendarDays = parseCalendarDays(calendarSheet, workspace);
            List<CourseAssignmentRaw> assignmentRaws = parseAssignments(assignmentSheet, workspace);

            List<Course> savedCourses = courseRepository.saveAll(courses);
            List<Trainer> savedTrainers = trainerRepository.saveAll(trainers);
            List<Venue> savedVenues = venueRepository.saveAll(venues);
            List<CalendarDay> savedCalendarDays = calendarDayRepository.saveAll(calendarDays);

            Map<String, Course> courseMap = new HashMap<>();
            for (Course c : savedCourses) {
                if (c.getExternalId() != null) {
                    courseMap.put(c.getExternalId(), c);
                }
            }
            Map<String, Trainer> trainerMap = new HashMap<>();
            for (Trainer t : savedTrainers) {
                if (t.getExternalId() != null) {
                    trainerMap.put(t.getExternalId(), t);
                }
            }

            List<CourseAssignment> assignments = new ArrayList<>();
            for (CourseAssignmentRaw raw : assignmentRaws) {
                Course course = courseMap.get(raw.courseExtId());
                Trainer trainer = trainerMap.get(raw.trainerExtId());
                if (course == null) {
                    throw new ExcelParseException("Course not found for external ID: " + raw.courseExtId());
                }
                if (trainer == null) {
                    throw new ExcelParseException("Trainer not found for external ID: " + raw.trainerExtId());
                }
                CourseAssignment assignment = new CourseAssignment();
                assignment.setWorkspace(workspace);
                assignment.setCourse(course);
                assignment.setTrainer(trainer);
                assignments.add(assignment);
            }

            List<CourseAssignment> savedAssignments = courseAssignmentRepository.saveAll(assignments);

            return new ImportResult(
                courses.size(), savedCourses.size(),
                trainers.size(), savedTrainers.size(),
                venues.size(), savedVenues.size(),
                calendarDays.size(), savedCalendarDays.size(),
                assignmentRaws.size(), savedAssignments.size(),
                null
            );
        } catch (ExcelParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelParseException("Failed to import Excel file: " + e.getMessage(), e);
        }
    }

    private List<Course> parseCourses(Sheet sheet, Workspace workspace) {
        if (sheet == null) return List.of();
        List<Course> courses = new ArrayList<>();
        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (rowNum == 1) continue;
            if (isEmptyRow(row)) continue;

            Course course = new Course();
            course.setWorkspace(workspace);
            course.setExternalId(getStringCell(row, 0));
            course.setName(getStringCell(row, 2));
            course.setSpecialization(getStringCell(row, 3));
            course.setDurationDays(getIntCell(row, 4));
            course.setHoursPerDay(getIntCell(row, 5));
            course.setExpectedTrainees(getIntCell(row, 6));
            course.setCity(getStringCell(row, 7));
            course.setBeneficiary(getStringCell(row, 8));
            course.setType(parseCourseType(getStringCell(row, 9)));
            course.setPriority(getStringCell(row, 10));
            course.setEarliestStart(getDateCell(row, 11));
            course.setLatestEnd(getDateCell(row, 12));
            course.setFixedDate(getDateCell(row, 13));
            course.setNotes(getStringCell(row, 14));
            courses.add(course);
        }
        return courses;
    }

    private List<Trainer> parseTrainers(Sheet sheet, Workspace workspace) {
        if (sheet == null) return List.of();
        List<Trainer> trainers = new ArrayList<>();
        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (rowNum == 1) continue;
            if (isEmptyRow(row)) continue;

            Trainer trainer = new Trainer();
            trainer.setWorkspace(workspace);
            trainer.setExternalId(getStringCell(row, 0));
            trainer.setName(getStringCell(row, 1));
            trainer.setSpecialties(getStringCell(row, 2));
            trainer.setCity(getStringCell(row, 3));
            trainer.setTrainerType(getStringCell(row, 4));
            trainer.setUnavailableDates(getStringCell(row, 5));
            trainer.setMaxDaysPerMonth(getIntCell(row, 6));
            trainer.setMaxConsecutiveDays(getIntCell(row, 7));
            trainer.setCostPerDay(getIntCell(row, 8));
            trainer.setNotes(getStringCell(row, 9));
            trainers.add(trainer);
        }
        return trainers;
    }

    private List<Venue> parseVenues(Sheet sheet, Workspace workspace) {
        if (sheet == null) return List.of();
        List<Venue> venues = new ArrayList<>();
        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (rowNum == 1) continue;
            if (isEmptyRow(row)) continue;

            Venue venue = new Venue();
            venue.setWorkspace(workspace);
            venue.setExternalId(getStringCell(row, 0));
            venue.setName(getStringCell(row, 1));
            venue.setCity(getStringCell(row, 2));
            venue.setType(parseVenueType(getStringCell(row, 3)));
            venue.setCapacity(getIntCell(row, 4));
            venue.setAvailableFrom(getDateCell(row, 5));
            venue.setAvailableTo(getDateCell(row, 6));
            venue.setUnavailableDates(getStringCell(row, 7));
            venue.setEquipmentNotes(getStringCell(row, 8));
            venues.add(venue);
        }
        return venues;
    }

    private List<CalendarDay> parseCalendarDays(Sheet sheet, Workspace workspace) {
        if (sheet == null) return List.of();
        List<CalendarDay> days = new ArrayList<>();
        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (rowNum == 1) continue;
            if (isEmptyRow(row)) continue;

            CalendarDay day = new CalendarDay();
            day.setWorkspace(workspace);
            day.setDate(getDateCell(row, 0));
            day.setIsWorkDay(parseYesNo(getStringCell(row, 2)));
            day.setIsHoliday(parseYesNo(getStringCell(row, 3)));
            days.add(day);
        }
        return days;
    }

    private List<CourseAssignmentRaw> parseAssignments(Sheet sheet, Workspace workspace) {
        if (sheet == null) return List.of();
        List<CourseAssignmentRaw> raws = new ArrayList<>();
        int rowNum = 0;
        for (Row row : sheet) {
            rowNum++;
            if (rowNum == 1) continue;
            if (isEmptyRow(row)) continue;

            String courseExtId = getStringCell(row, 1);
            String trainerExtId = getStringCell(row, 2);
            if (courseExtId != null && trainerExtId != null) {
                raws.add(new CourseAssignmentRaw(courseExtId, trainerExtId));
            }
        }
        return raws;
    }

    private String getStringCell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> {
                String val = cell.getStringCellValue();
                yield (val == null || val.isBlank() || val.equalsIgnoreCase("NULL")) ? null : val.trim();
            }
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield String.valueOf((int) val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf((int) cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e2) {
                        yield null;
                    }
                }
            }
            default -> null;
        };
    }

    private Integer getIntCell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                yield val.isEmpty() ? null : Integer.parseInt(val);
            }
            case FORMULA -> {
                try {
                    yield (int) cell.getNumericCellValue();
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private LocalDate getDateCell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception ignored) {
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val > 40000 && val < 100000) {
                return DateUtil.getJavaDate(val).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }
        }
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (!val.isEmpty()) {
                try {
                    return LocalDate.parse(val);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private CourseType parseCourseType(String value) {
        if (value == null) return CourseType.IN_PERSON;
        return switch (value.toLowerCase()) {
            case "online" -> CourseType.ONLINE;
            case "external site" -> CourseType.EXTERNAL;
            default -> CourseType.IN_PERSON;
        };
    }

    private CourseType parseVenueType(String value) {
        if (value == null) return CourseType.IN_PERSON;
        return switch (value.toLowerCase()) {
            case "online" -> CourseType.ONLINE;
            case "external site" -> CourseType.EXTERNAL;
            default -> CourseType.IN_PERSON;
        };
    }

    private Boolean parseYesNo(String value) {
        if (value == null) return false;
        return value.equalsIgnoreCase("Yes");
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 10; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                    default -> null;
                };
                if (val != null && !val.trim().isEmpty()) return false;
            }
        }
        return true;
    }

    private record CourseAssignmentRaw(String courseExtId, String trainerExtId) {}
}