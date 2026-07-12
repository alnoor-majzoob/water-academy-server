# Backend Performance Issues

This report reviews the Spring Boot backend in `water-academy` with focus on database access, scheduler execution, import/export, async task behavior, and runtime configuration.

## Executive Summary

The backend is functional, but several paths will degrade quickly as workspace data grows. The most important risks are unbounded `/all` endpoints, full-table-in-workspace loads for scheduler/export/filter options, repeated dashboard aggregate queries, missing composite indexes for date/status filters, and expensive genetic-algorithm fitness evaluation. The current defaults are acceptable for small datasets, but larger workspaces with thousands of courses, assignments, schedule entries, or calendar days will see slow responses, high memory use, and long-running background tasks.

## High Priority Issues

### 1. Unbounded `findByWorkspaceId` APIs load full workspaces into memory

Affected code:

- `src/main/java/com/wateracademy/service/ScheduleEntryService.java:57`
- `src/main/java/com/wateracademy/service/CourseService.java:41`
- `src/main/java/com/wateracademy/service/TaskService.java:44`
- `src/main/java/com/wateracademy/service/CourseAssignmentService.java:45`
- `src/main/java/com/wateracademy/service/ExportService.java:118`
- `src/main/java/com/wateracademy/controller/TaskController.java:41`

Problem:

- Several services expose or use unpaged `findByWorkspaceId` calls.
- The `/all` endpoints and export paths materialize complete workspace datasets before mapping or writing responses.
- The frontend already uses paginated endpoints in many places, but `/tasks/all`, schedule export, unscheduled views, and scheduler input still rely on full loads.

Impact:

- Memory grows linearly with workspace size.
- Large responses increase GC pressure and response latency.
- A single large export or `/all` request can compete with normal API traffic.

Recommended fixes:

- Keep `/all` only for bounded reference data; otherwise replace with paginated endpoints.
- For export, stream rows from the database or page through records instead of loading all lists first.
- For scheduler input, load only fields needed by the GA using projection queries.
- Add explicit maximums or server-side safeguards for unpaged endpoints.

### 2. Schedule entry queries need composite indexes for common filters

Affected code:

- `src/main/java/com/wateracademy/entity/ScheduleEntry.java:18`
- `src/main/java/com/wateracademy/repository/ScheduleEntryRepository.java:78`
- `src/main/java/com/wateracademy/repository/ScheduleEntryRepository.java:84`
- `src/main/java/com/wateracademy/service/ScheduleEntryService.java:71`

Problem:

- `schedule_entry` currently has single-column indexes on `workspace_id`, `course_id`, `trainer_id`, and `venue_id`.
- The hot queries filter by workspace plus trainer/venue plus overlapping dates, or workspace plus status/date/city.
- Single-column indexes are less effective for overlap queries such as `workspace_id = ? AND trainer_id = ? AND start_date <= ? AND end_date >= ?`.

Impact:

- Conflict checks slow down as schedule entries grow.
- Schedule table/gantt filtering can require broad scans inside a workspace.
- Dashboard upcoming sessions and counts can become expensive.

Recommended indexes:

```sql
CREATE INDEX idx_schedule_workspace_start_end
  ON schedule_entry (workspace_id, start_date, end_date);

CREATE INDEX idx_schedule_workspace_trainer_dates
  ON schedule_entry (workspace_id, trainer_id, start_date, end_date);

CREATE INDEX idx_schedule_workspace_venue_dates
  ON schedule_entry (workspace_id, venue_id, start_date, end_date);

CREATE INDEX idx_schedule_workspace_status_start
  ON schedule_entry (workspace_id, status, start_date);
```

Notes:

- Validate with `EXPLAIN ANALYZE` on PostgreSQL before and after.
- In H2 dev, these indexes may not show the same benefit as PostgreSQL.

### 3. Dashboard performs many independent aggregate queries per request

Affected code:

- `src/main/java/com/wateracademy/service/DashboardService.java:37`
- `src/main/java/com/wateracademy/repository/ScheduleEntryRepository.java:25`
- `src/main/java/com/wateracademy/repository/ScheduleEntryRepository.java:31`
- `src/main/java/com/wateracademy/repository/ScheduleEntryRepository.java:34`
- `src/main/java/com/wateracademy/repository/ScheduleEntryRepository.java:49`

Problem:

- One dashboard request executes multiple count/group/projection queries: total courses, scheduled courses, course type counts, trainers, venues, conflicts, monthly counts, trainer utilization, and upcoming sessions.
- This is simple and maintainable, but database round trips add up.

Impact:

- Dashboard latency increases with DB/network latency even when each query is individually fast.
- Under frequent polling or multiple users, repeated aggregate work can become a significant load.

Recommended fixes:

- Combine course counts by type into one grouped query.
- Consider a single dashboard summary query or materialized summary table per workspace if dashboard traffic is high.
- Cache dashboard responses at the backend for a short TTL and evict after mutations/import/schedule completion.
- Add composite indexes for dashboard filters, especially `schedule_entry(workspace_id, status, start_date)`.

### 4. Scheduler loads full domain data and runs CPU-heavy GA inside app workers

Affected code:

- `src/main/java/com/wateracademy/service/GaRunnerService.java:66`
- `src/main/java/com/wateracademy/service/GaRunnerService.java:76`
- `src/main/java/com/wateracademy/service/GaRunnerService.java:90`
- `src/main/java/com/wateracademy/scheduler/SchedulerService.java:21`
- `src/main/java/com/wateracademy/config/AsyncConfig.java:14`

Problem:

- GA task startup loads existing entries, courses, trainers, venues, calendar days, and assignments fully into memory.
- The GA runs inside the same JVM as API traffic using `gaTaskExecutor` with core pool 2, max pool 4, and queue capacity 10.
- `TaskService.create` only blocks existing `RUNNING` tasks, not queued `PENDING` tasks, so users can enqueue multiple pending scheduling jobs before the first starts.

Impact:

- Large workspaces can consume high heap during scheduling.
- CPU-heavy GA work can reduce API responsiveness on small hosts.
- Multiple pending GA jobs can become stale work and increase queue latency.

Recommended fixes:

- Treat both `PENDING` and `RUNNING` as active task statuses for scheduling concurrency checks.
- Limit GA executor concurrency based on CPU cores and deployment size.
- Consider moving GA execution to a separate worker process if scheduling becomes heavy.
- Use projection queries for scheduler input instead of full JPA entities.
- Add task cancellation or idempotency for repeated scheduling requests.

### 5. GA fitness evaluation allocates heavily in inner loops

Affected code:

- `src/main/java/com/wateracademy/scheduler/ga/ScheduleFitnessEvaluator.java:65`
- `src/main/java/com/wateracademy/scheduler/ga/ScheduleFitnessEvaluator.java:100`
- `src/main/java/com/wateracademy/scheduler/ga/ScheduleFitnessEvaluator.java:111`
- `src/main/java/com/wateracademy/scheduler/ga/ScheduleFitnessEvaluator.java:155`

Problem:

- Every candidate fitness evaluation creates maps, sets, unavailable-date sets, and occupied-day lists.
- GA evolution evaluates many candidates across generations, so these allocations multiply quickly.
- `occupiedWorkingDays` allocates a new `ArrayList` for each scheduled gene.

Impact:

- Increased GC pressure during scheduling.
- Slower GA execution as course count, population size, offspring multiplier, or generations grow.

Recommended fixes:

- Precompute trainer unavailable sets, venue unavailable sets, and course duration metadata once in the evaluator constructor.
- Replace `occupiedWorkingDays` list allocation with direct iteration over working-day indexes and callback-style scoring.
- Reuse primitive-friendly structures where practical, such as `Map<Integer, BitSet>` for occupied trainer/venue days.
- Benchmark before/after using a realistic workspace fixture.

## Medium Priority Issues

### 6. Filter option endpoints scan full datasets in application code

Affected code:

- `src/main/java/com/wateracademy/service/ScheduleEntryService.java:90`
- `src/main/java/com/wateracademy/service/TrainerService.java:73`

Problem:

- Schedule filter months are calculated by loading all schedule entries, mapping start dates, formatting month strings, then distinct/sort in Java.
- Trainer specialties appear to be derived similarly from full trainer rows.
- Course filters use database `DISTINCT` queries, which is better.

Impact:

- Filter option calls become expensive for large schedules/trainers.
- These endpoints are often called on page load, so they affect perceived UI speed.

Recommended fixes:

- Add repository-level month projection query, for example `SELECT DISTINCT FUNCTION('to_char', s.startDate, 'YYYY-MM') ...` for PostgreSQL or derive with year/month functions.
- Keep filter extraction in SQL where possible.
- Cache filter option responses and invalidate after relevant mutations.

### 7. Export uses streaming workbook but still preloads data and auto-sizes columns

Affected code:

- `src/main/java/com/wateracademy/service/ExportService.java:74`
- `src/main/java/com/wateracademy/service/ExportService.java:84`
- `src/main/java/com/wateracademy/service/ExportService.java:133`
- `src/main/java/com/wateracademy/service/ExportService.java:313`

Problem:

- `SXSSFWorkbook` helps with Excel row memory, but the service first loads each selected dataset into lists.
- `autoSizeColumn` is expensive because it scans cell contents and can be slow for large sheets.
- For `type=schedule` and `type=conflicts`, the service loads all schedule entries and filters in Java.

Impact:

- Large exports can consume high heap despite streaming workbook usage.
- Export latency increases disproportionately with row count and column count.

Recommended fixes:

- Query filtered exports directly in SQL, such as confirmed schedules and conflicts.
- Page through each sheet and write rows incrementally.
- Disable auto-size for large exports or use fixed column widths.
- Consider returning an async export task for large workspaces.

### 8. Import holds the whole workbook and all parsed entities in memory

Affected code:

- `src/main/java/com/wateracademy/service/ImportService.java:61`
- `src/main/java/com/wateracademy/service/ImportService.java:65`
- `src/main/java/com/wateracademy/service/ImportService.java:72`
- `src/main/java/com/wateracademy/service/ImportService.java:78`

Problem:

- `WorkbookFactory.create` loads the workbook model.
- Parsed courses, trainers, venues, calendar days, and assignment raws are accumulated in lists before saving.
- `saveAll` runs inside a single transaction for the whole import.

Impact:

- Large Excel files can cause high heap usage and long transactions.
- A failed late row rolls back all work after spending time parsing and inserting.

Recommended fixes:

- Add upload size limits and validation before parsing.
- Batch inserts with `spring.jpa.properties.hibernate.jdbc.batch_size`.
- Flush/clear persistence context in chunks for very large imports.
- Consider Apache POI streaming/SAX mode for large `.xlsx` files.

### 9. Duplicate assignment check loads assignments instead of using existence query

Affected code:

- `src/main/java/com/wateracademy/service/CourseAssignmentService.java:84`
- `src/main/java/com/wateracademy/repository/CourseAssignmentRepository.java:13`

Problem:

- Creation checks duplicates by loading every assignment for a course and scanning in Java.
- The table already has a unique constraint on `(workspace_id, trainer_id, course_id)`.

Impact:

- Assignment creation slows down for courses with many trainer assignments.
- Extra entity loading is unnecessary.

Recommended fixes:

- Replace with `existsByWorkspaceIdAndCourseIdAndTrainerId(...)`.
- Keep the database unique constraint as the final correctness guarantee.
- Catch unique constraint violations and return a conflict response for race conditions.

### 10. Search filters use leading-wildcard `LIKE`, which prevents normal index usage

Affected code:

- `src/main/java/com/wateracademy/util/PaginationUtils.java:25`
- `src/main/java/com/wateracademy/service/CourseService.java:55`
- `src/main/java/com/wateracademy/service/CourseAssignmentService.java:61`

Problem:

- Search terms become `%term%`.
- Normal b-tree indexes cannot efficiently serve leading-wildcard searches.

Impact:

- Search queries scan many rows inside a workspace as data grows.

Recommended fixes:

- For PostgreSQL, use trigram indexes with `pg_trgm` for contains searches.
- If prefix search is acceptable, use `term%` with lower-case functional indexes.
- Keep workspace filtering first with composite or partial indexes.

## Low Priority Issues

### 11. SQL logging is enabled in default application properties

Affected code:

- `src/main/resources/application.properties:11`
- `src/main/resources/application.properties:12`

Problem:

- `spring.jpa.show-sql=true` and formatted SQL logging are enabled by default.

Impact:

- Dev logs become noisy.
- If default properties are accidentally used in production-like environments, SQL logging can noticeably reduce throughput and increase log volume.

Recommended fixes:

- Disable SQL logging by default.
- Enable SQL logs only in a local profile.
- Use slow-query logging or datasource-proxy in diagnostic profiles instead of printing every SQL statement.

### 12. `ddl-auto=update` is not ideal for production performance management

Affected code:

- `src/main/resources/application.properties:9`
- `src/main/resources/application-postgres.properties:5`

Problem:

- Hibernate schema updates are convenient but not a replacement for controlled migrations.
- Performance-critical indexes may be missed, changed unpredictably, or not reviewed with execution plans.

Impact:

- Production schema can drift from expected performance assumptions.
- Index creation strategy is not explicit.

Recommended fixes:

- Use Flyway or Liquibase migrations.
- Put all performance indexes in versioned migrations.
- Set production `ddl-auto=validate` or `none`.

## Database Index Review

Current entity indexes are mostly single-column workspace or FK indexes. That is a good baseline, but the backend query patterns need composite indexes.

Recommended additions:

```sql
-- Schedule browsing, dashboard upcoming sessions, and month/date filtering
CREATE INDEX idx_schedule_workspace_start_end
  ON schedule_entry (workspace_id, start_date, end_date);

-- Trainer conflict checks
CREATE INDEX idx_schedule_workspace_trainer_dates
  ON schedule_entry (workspace_id, trainer_id, start_date, end_date);

-- Venue conflict checks
CREATE INDEX idx_schedule_workspace_venue_dates
  ON schedule_entry (workspace_id, venue_id, start_date, end_date);

-- Status-filtered dashboard and schedule queries
CREATE INDEX idx_schedule_workspace_status_start
  ON schedule_entry (workspace_id, status, start_date);

-- Task polling and active-task checks
CREATE INDEX idx_task_workspace_status_created
  ON task (workspace_id, status, created_at DESC);

-- Course list filters
CREATE INDEX idx_course_workspace_type
  ON course (workspace_id, type);

CREATE INDEX idx_course_workspace_city
  ON course (workspace_id, city);
```

For PostgreSQL text search:

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_course_name_trgm
  ON course USING gin (lower(name) gin_trgm_ops);

CREATE INDEX idx_course_beneficiary_trgm
  ON course USING gin (lower(beneficiary) gin_trgm_ops);
```

## Recommended Implementation Order

1. Add PostgreSQL migrations for composite schedule indexes and task active-status index.
2. Change scheduling concurrency check to block both `PENDING` and `RUNNING` tasks.
3. Move schedule filter months and trainer filter specialties into repository projection queries.
4. Replace duplicate assignment scan with an `exists` query.
5. Optimize export to query filtered data directly and avoid auto-sizing for large sheets.
6. Add JDBC batching for imports and scheduler `saveAll` operations.
7. Profile GA with realistic data, then remove high-frequency allocations in `ScheduleFitnessEvaluator`.
8. Introduce backend caching for dashboard/filter options with explicit invalidation after mutations and schedule completion.

## Suggested Measurements

Before changing behavior, capture baselines:

- `GET /api/workspaces/{id}/dashboard` p50/p95 latency and SQL count.
- `GET /api/workspaces/{id}/schedule-entries?page=0&size=100&sort=startDate,asc` p50/p95 latency.
- Conflict-check query `EXPLAIN ANALYZE` for trainer and venue overlap queries.
- GA task elapsed time for workspaces with 100, 500, and 1000 courses.
- Heap usage during full export and Excel import.
- Number of SQL statements for dashboard, schedule page load, export, import, and scheduler startup.

## Notes

- Some issues are only visible with production-sized data. H2 behavior can hide PostgreSQL query planning problems.
- The frontend now caches aggressively, which reduces API traffic, but backend endpoints still need to behave predictably when cache is cold or invalidated.
- The scheduler path is both CPU-heavy and memory-heavy. It should be treated separately from normal CRUD performance.
