#!/usr/bin/env bash
#
# Water Academy — API smoke test script
# Tests all 49+ endpoints, reports pass/fail per test.
#
# Usage:  ./test-api.sh [base_url]
#         Default base_url = http://localhost:8080

set -uo pipefail

BASE="${1:-http://localhost:8080}"
PASS=0
FAIL=0

assert() {
    local label="$1" expected="$2" actual="$3"
    if [[ "$expected" == "$actual" ]]; then
        echo "  PASS  $label"
        ((PASS++))
    else
        echo "  FAIL  $label  (expected=$expected actual=$actual)"
        ((FAIL++))
    fi
}

http_status() {
    curl -s -o /dev/null -w '%{http_code}' "$@"
}

http_body() {
    curl -s "$@"
}

extract_id() {
    echo "$1" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4
}

echo "====================================="
echo " Water Academy — API Smoke Test"
echo " Base URL: $BASE"
echo "====================================="
echo ""

# ── Health ──
echo "── Health ──"
assert "GET /hello" "200" "$(http_status $BASE/hello)"

# ── Workspace CRUD ──
echo "── Workspace CRUD ──"
assert "GET /api/workspaces (list)" "200" "$(http_status $BASE/api/workspaces)"

WS_JSON=$(http_body -X POST $BASE/api/workspaces \
    -H 'Content-Type: application/json' \
    -d '{"name":"Smoke Test WS","description":"Created by smoke test","year":2026,"color":"#FF5733"}')
WS_ID=$(extract_id "$WS_JSON")
[[ -n "$WS_ID" ]] && assert "POST /api/workspaces (create)" "1" "1" || assert "POST /api/workspaces (create)" "1" "0"

assert "GET /api/workspaces/{id} (by id)" "200" "$(http_status $BASE/api/workspaces/$WS_ID)"

assert "PUT /api/workspaces/{id} (update)" "200" \
    "$(http_status -X PUT $BASE/api/workspaces/$WS_ID \
        -H 'Content-Type: application/json' \
        -d '{"name":"Updated WS","description":"Updated","year":2027,"color":"#000"}')"

assert "PUT /api/workspaces/{id}/status" "200" \
    "$(http_status -X PUT $BASE/api/workspaces/$WS_ID/status \
        -H 'Content-Type: application/json' \
        -d '{"status":"IMPORTED"}')"

assert "GET /api/workspaces/{id} (404)" "404" \
    "$(http_status $BASE/api/workspaces/00000000-0000-0000-0000-000000000000)"

# ── Course CRUD ──
echo "── Course CRUD ──"
assert "GET /api/workspaces/{wsId}/courses (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/courses)"

COURSE_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/courses \
    -H 'Content-Type: application/json' \
    -d '{"name":"Smoke Course","specialization":"Engineering","durationDays":3,"hoursPerDay":5,"expectedTrainees":20,"city":"Riyadh","beneficiary":"Test","priority":"HIGH","type":"IN_PERSON","earliestStart":"2026-06-01","latestEnd":"2026-06-05"}')
COURSE_ID=$(extract_id "$COURSE_JSON")
[[ -n "$COURSE_ID" ]] && assert "POST /api/workspaces/{wsId}/courses (create)" "1" "1" || assert "POST .../courses (create)" "1" "0"

assert "GET .../courses/{id}" "200" "$(http_status $BASE/api/workspaces/$WS_ID/courses/$COURSE_ID)"

assert "PUT .../courses/{id} (update)" "200" \
    "$(http_status -X PUT $BASE/api/workspaces/$WS_ID/courses/$COURSE_ID \
        -H 'Content-Type: application/json' \
        -d '{"name":"Updated Course","durationDays":5,"type":"ONLINE"}')"

assert "GET .../courses/{id} (404)" "404" \
    "$(http_status $BASE/api/workspaces/$WS_ID/courses/00000000-0000-0000-0000-000000000000)"

# ── Trainer CRUD ──
echo "── Trainer CRUD ──"
assert "GET /api/workspaces/{wsId}/trainers (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/trainers)"

TRAINER_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/trainers \
    -H 'Content-Type: application/json' \
    -d '{"name":"Dr. Smoke","city":"Jeddah","specialties":"Testing","trainerType":"Internal","maxDaysPerMonth":10,"maxConsecutiveDays":3,"costPerDay":500}')
TRAINER_ID=$(extract_id "$TRAINER_JSON")
[[ -n "$TRAINER_ID" ]] && assert "POST .../trainers (create)" "1" "1" || assert "POST .../trainers (create)" "1" "0"

assert "GET .../trainers/{id}" "200" "$(http_status $BASE/api/workspaces/$WS_ID/trainers/$TRAINER_ID)"
assert "PUT .../trainers/{id} (update)" "200" \
    "$(http_status -X PUT $BASE/api/workspaces/$WS_ID/trainers/$TRAINER_ID \
        -H 'Content-Type: application/json' -d '{"name":"Updated Trainer","city":"Riyadh"}')"

# ── Venue CRUD ──
echo "── Venue CRUD ──"
assert "GET /api/workspaces/{wsId}/venues (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/venues)"

VENUE_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/venues \
    -H 'Content-Type: application/json' \
    -d '{"name":"Smoke Hall","city":"Dammam","capacity":50,"type":"IN_PERSON"}')
VENUE_ID=$(extract_id "$VENUE_JSON")
[[ -n "$VENUE_ID" ]] && assert "POST .../venues (create)" "1" "1" || assert "POST .../venues (create)" "1" "0"

assert "GET .../venues/{id}" "200" "$(http_status $BASE/api/workspaces/$WS_ID/venues/$VENUE_ID)"
assert "PUT .../venues/{id} (update)" "200" \
    "$(http_status -X PUT $BASE/api/workspaces/$WS_ID/venues/$VENUE_ID \
        -H 'Content-Type: application/json' -d '{"name":"Updated Hall","capacity":100,"type":"ONLINE"}')"

# ── CalendarDay CRUD ──
echo "── CalendarDay CRUD ──"
assert "GET .../calendar-days (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/calendar-days)"

DAY_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/calendar-days \
    -H 'Content-Type: application/json' \
    -d '{"date":"2026-07-01","isWorkDay":true,"isHoliday":false}')
DAY_ID=$(extract_id "$DAY_JSON")
[[ -n "$DAY_ID" ]] && assert "POST .../calendar-days (create)" "1" "1" || assert "POST .../calendar-days (create)" "1" "0"

assert "GET .../calendar-days/{id}" "200" "$(http_status $BASE/api/workspaces/$WS_ID/calendar-days/$DAY_ID)"
assert "POST .../calendar-days (duplicate → 409)" "409" \
    "$(http_status -X POST $BASE/api/workspaces/$WS_ID/calendar-days \
        -H 'Content-Type: application/json' \
        -d '{"date":"2026-07-01","isWorkDay":true,"isHoliday":false}')"

# Bulk create
BULK_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/calendar-days/bulk \
    -H 'Content-Type: application/json' \
    -d '[{"date":"2026-08-01","isWorkDay":true,"isHoliday":false},{"date":"2026-08-02","isWorkDay":false,"isHoliday":true}]')
BULK_COUNT=$(echo "$BULK_JSON" | grep -o '"id"' | wc -l)
assert "POST .../calendar-days/bulk (2 items)" "2" "$BULK_COUNT"
# Collect bulk day IDs for cleanup
BULK_IDS=$(echo "$BULK_JSON" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

# ── CourseAssignment ──
echo "── CourseAssignment ──"
assert "GET .../assignments (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/assignments)"

ASSIGN_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/assignments \
    -H 'Content-Type: application/json' \
    -d "{\"trainerId\":\"$TRAINER_ID\",\"courseId\":\"$COURSE_ID\"}")
ASSIGN_ID=$(extract_id "$ASSIGN_JSON")
[[ -n "$ASSIGN_ID" ]] && assert "POST .../assignments (create)" "1" "1" || assert "POST .../assignments (create)" "1" "0"

assert "POST .../assignments (duplicate → 409)" "409" \
    "$(http_status -X POST $BASE/api/workspaces/$WS_ID/assignments \
        -H 'Content-Type: application/json' \
        -d "{\"trainerId\":\"$TRAINER_ID\",\"courseId\":\"$COURSE_ID\"}")"

# ── ScheduleEntry ──
echo "── ScheduleEntry ──"
assert "GET .../schedule-entries (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/schedule-entries)"

ENTRY_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/schedule-entries \
    -H 'Content-Type: application/json' \
    -d "{\"courseId\":\"$COURSE_ID\",\"trainerId\":\"$TRAINER_ID\",\"venueId\":\"$VENUE_ID\",\"startDate\":\"2026-09-01\",\"endDate\":\"2026-09-03\"}")
ENTRY_ID=$(extract_id "$ENTRY_JSON")
[[ -n "$ENTRY_ID" ]] && assert "POST .../schedule-entries (create)" "1" "1" || assert "POST .../schedule-entries (create)" "1" "0"

# Status transition
assert "PUT .../schedule-entries/{id}/status (CONFIRMED)" "200" \
    "$(http_status -X PUT $BASE/api/workspaces/$WS_ID/schedule-entries/$ENTRY_ID/status \
        -H 'Content-Type: application/json' -d '{"status":"CONFIRMED"}')"

# Overlapping entry -> conflict notes
ENTRY2_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/schedule-entries \
    -H 'Content-Type: application/json' \
    -d "{\"courseId\":\"$COURSE_ID\",\"trainerId\":\"$TRAINER_ID\",\"venueId\":\"$VENUE_ID\",\"startDate\":\"2026-09-02\",\"endDate\":\"2026-09-05\"}")
ENTRY2_ID=$(extract_id "$ENTRY2_JSON")
conflict_note=$(echo "$ENTRY2_JSON" | grep -o '"conflictNotes"' | wc -l)
assert "POST overlapping entry (conflictNotes populated)" "1" "$conflict_note"

# Conflict query endpoints
conflict_url="$BASE/api/workspaces/$WS_ID/schedule-entries/conflicts/venue?venueId=$VENUE_ID&startDate=2026-09-01&endDate=2026-09-10"
assert "GET .../conflicts/venue" "200" "$(http_status "$conflict_url")"
conflict_url="$BASE/api/workspaces/$WS_ID/schedule-entries/conflicts/trainer?trainerId=$TRAINER_ID&startDate=2026-09-01&endDate=2026-09-10"
assert "GET .../conflicts/trainer" "200" "$(http_status "$conflict_url")"

# ── Task ──
echo "── Task ──"
assert "GET /api/workspaces/{wsId}/tasks (list)" "200" "$(http_status $BASE/api/workspaces/$WS_ID/tasks)"

TASK_JSON=$(http_body -X POST $BASE/api/workspaces/$WS_ID/tasks)
TASK_ID=$(extract_id "$TASK_JSON")
[[ -n "$TASK_ID" ]] && assert "POST .../tasks (create)" "1" "1" || assert "POST .../tasks (create)" "1" "0"

assert "GET .../tasks/{id}" "200" "$(http_status $BASE/api/workspaces/$WS_ID/tasks/$TASK_ID)"
assert "POST .../tasks/{id}/start (RUNNING)" "200" "$(http_status -X POST $BASE/api/workspaces/$WS_ID/tasks/$TASK_ID/start)"
assert "POST .../tasks/{id}/complete" "200" \
    "$(http_status -X POST $BASE/api/workspaces/$WS_ID/tasks/$TASK_ID/complete \
        -H 'Content-Type: text/plain' -d 'Smoke test completed')"

# ── Import ──
echo "── Import ──"

# Generate a valid multi-sheet xlsx
TMP_XLSX=$(mktemp)
python3 << PYEOF 2>/dev/null
import zipfile, io

buf = io.BytesIO()
with zipfile.ZipFile(buf, 'w') as z:
    # Content_Types
    z.writestr('[Content_Types].xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/worksheets/sheet5.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>''')

    # Rels
    z.writestr('_rels/.rels', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>''')

    # Workbook
    z.writestr('xl/workbook.xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>
<sheet name="Courses" sheetId="1" r:id="rId1"/>
<sheet name="Trainers" sheetId="2" r:id="rId2"/>
<sheet name="Venues" sheetId="3" r:id="rId3"/>
<sheet name="Calendar" sheetId="4" r:id="rId4"/>
<sheet name="assigned course" sheetId="5" r:id="rId5"/>
</sheets>
</workbook>''')

    # Workbook rels
    z.writestr('xl/_rels/workbook.xml.rels', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
<Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
<Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet5.xml"/>
</Relationships>''')

    # Sheet 1: Courses - columns match ImportService.parseCourses (extId=0, name=2, specialization=3, durationDays=4, ...)
    z.writestr('xl/worksheets/sheet1.xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="A1" t="inlineStr"><is><t>ExternalID</t></is></c><c r="C1" t="inlineStr"><is><t>Name</t></is></c><c r="D1" t="inlineStr"><is><t>Specialization</t></is></c><c r="E1" t="inlineStr"><is><t>Duration</t></is></c><c r="F1" t="inlineStr"><is><t>Hours</t></is></c><c r="J1" t="inlineStr"><is><t>Type</t></is></c></row>
<row r="2"><c r="A2" t="inlineStr"><is><t>C-SMOKE</t></is></c><c r="C2" t="inlineStr"><is><t>Smoke Course</t></is></c><c r="D2" t="inlineStr"><is><t>Test</t></is></c><c r="E2"><v>3</v></c><c r="F2"><v>5</v></c><c r="J2" t="inlineStr"><is><t>In-person</t></is></c></row>
</sheetData>
</worksheet>''')

    # Sheet 2: Trainers - columns: extId=0, name=1, specialties=2, city=3, trainerType=4 ...
    z.writestr('xl/worksheets/sheet2.xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="A1" t="inlineStr"><is><t>ExternalID</t></is></c><c r="B1" t="inlineStr"><is><t>Name</t></is></c><c r="C1" t="inlineStr"><is><t>Specialties</t></is></c><c r="D1" t="inlineStr"><is><t>City</t></is></c></row>
<row r="2"><c r="A2" t="inlineStr"><is><t>T-SMOKE</t></is></c><c r="B2" t="inlineStr"><is><t>Smoke Trainer</t></is></c><c r="C2" t="inlineStr"><is><t>Testing</t></is></c><c r="D2" t="inlineStr"><is><t>Jeddah</t></is></c></row>
</sheetData>
</worksheet>''')

    # Sheet 3: Venues - columns: extId=0, name=1, city=2, type=3, capacity=4
    z.writestr('xl/worksheets/sheet3.xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="A1" t="inlineStr"><is><t>ExternalID</t></is></c><c r="B1" t="inlineStr"><is><t>Name</t></is></c><c r="C1" t="inlineStr"><is><t>City</t></is></c><c r="D1" t="inlineStr"><is><t>Type</t></is></c><c r="E1"><v>5</v></c></row>
<row r="2"><c r="A2" t="inlineStr"><is><t>V-SMOKE</t></is></c><c r="B2" t="inlineStr"><is><t>Smoke Venue</t></is></c><c r="C2" t="inlineStr"><is><t>Dammam</t></is></c><c r="D2" t="inlineStr"><is><t>In-person</t></is></c><c r="E2"><v>30</v></c></row>
</sheetData>
</worksheet>''')

    # Sheet 4: Calendar - columns: date=0, day=1, workDay=2, holiday=3
    z.writestr('xl/worksheets/sheet4.xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="A1" t="inlineStr"><is><t>Date</t></is></c></row>
<row r="2"><c r="A2" t="inlineStr"><is><t>2026-10-01</t></is></c><c r="C2" t="inlineStr"><is><t>Yes</t></is></c><c r="D2" t="inlineStr"><is><t>No</t></is></c></row>
</sheetData>
</worksheet>''')

    # Sheet 5: assigned course - columns: id=0, courseExtId=1, trainerExtId=2
    z.writestr('xl/worksheets/sheet5.xml', '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetData>
<row r="1"><c r="B1" t="inlineStr"><is><t>Course</t></is></c><c r="C1" t="inlineStr"><is><t>Trainer</t></is></c></row>
<row r="2"><c r="B2" t="inlineStr"><is><t>C-SMOKE</t></is></c><c r="C2" t="inlineStr"><is><t>T-SMOKE</t></is></c></row>
</sheetData>
</worksheet>''')

with open('$TMP_XLSX', 'wb') as f:
    f.write(buf.getvalue())
PYEOF

# Upload the generated xlsx
status=$(http_status -X POST $BASE/api/workspaces/$WS_ID/import -F "file=@$TMP_XLSX")
assert "POST .../import (valid file)" "200" "$status"
rm -f "$TMP_XLSX"

# Malformed file
assert "POST .../import (malformed → 400)" "400" \
    "$(http_status -X POST $BASE/api/workspaces/$WS_ID/import -F "file=@/dev/stdin;filename=bad.xlsx" <<< "not an excel")"

# Import unknown workspace
assert "POST .../import (unknown WS → 404)" "404" \
    "$(http_status -X POST $BASE/api/workspaces/00000000-0000-0000-0000-000000000000/import -F "file=@/dev/stdin;filename=test.xlsx" <<< "dummy")"

# ── Export ──
echo "── Export ──"
assert "GET /api/workspaces/{wsId}/export" "200" "$(http_status $BASE/api/workspaces/$WS_ID/export)"

content_type=$(curl -s -o /dev/null -w '%{content_type}' $BASE/api/workspaces/$WS_ID/export)
assert "GET .../export (Content-Type octet-stream)" "application/octet-stream" "$content_type"

assert "GET .../export (unknown WS → 404)" "404" \
    "$(http_status $BASE/api/workspaces/00000000-0000-0000-0000-000000000000/export)"

# ── Cleanup: delete everything in reverse dependency order ──
echo "── Cleanup ──"

# Delete entries (both created)
[ -n "${ENTRY2_ID:-}" ] && assert "DELETE schedule-entries (2nd)" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/schedule-entries/$ENTRY2_ID")"
[ -n "${ENTRY_ID:-}" ] && assert "DELETE schedule-entries (1st)" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/schedule-entries/$ENTRY_ID")"

# Delete assignments
[ -n "${ASSIGN_ID:-}" ] && assert "DELETE assignments" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/assignments/$ASSIGN_ID")"

# Delete bulk calendar days
for bid in $BULK_IDS; do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/calendar-days/$bid" >/dev/null 2>&1 || true
done

# Delete single calendar day
[ -n "${DAY_ID:-}" ] && assert "DELETE calendar-days" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/calendar-days/$DAY_ID")"

# Delete task (clean up any remaining tasks)
for tid in $(curl -s "$BASE/api/workspaces/$WS_ID/tasks" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/tasks/$tid" >/dev/null 2>&1 || true
done

# Delete course
[ -n "${COURSE_ID:-}" ] && assert "DELETE courses" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/courses/$COURSE_ID")"

# Delete trainer
[ -n "${TRAINER_ID:-}" ] && assert "DELETE trainers" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/trainers/$TRAINER_ID")"

# Delete venue
[ -n "${VENUE_ID:-}" ] && assert "DELETE venues" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID/venues/$VENUE_ID")"

# Delete any import-created calendar days
for cal_day in $(curl -s "$BASE/api/workspaces/$WS_ID/calendar-days" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/calendar-days/$cal_day" >/dev/null 2>&1 || true
done

# Delete any import-created schedule entries
for se in $(curl -s "$BASE/api/workspaces/$WS_ID/schedule-entries" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/schedule-entries/$se" >/dev/null 2>&1 || true
done

# Delete any import-created assignments
for a in $(curl -s "$BASE/api/workspaces/$WS_ID/assignments" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/assignments/$a" >/dev/null 2>&1 || true
done

# Delete all remaining courses
for c in $(curl -s "$BASE/api/workspaces/$WS_ID/courses" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/courses/$c" >/dev/null 2>&1 || true
done

# Delete all remaining trainers
for t in $(curl -s "$BASE/api/workspaces/$WS_ID/trainers" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/trainers/$t" >/dev/null 2>&1 || true
done

# Delete all remaining venues
for v in $(curl -s "$BASE/api/workspaces/$WS_ID/venues" | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
    http_status -X DELETE "$BASE/api/workspaces/$WS_ID/venues/$v" >/dev/null 2>&1 || true
done

# Delete workspace
[ -n "${WS_ID:-}" ] && assert "DELETE workspaces" "204" \
    "$(http_status -X DELETE "$BASE/api/workspaces/$WS_ID")"

echo ""
echo "====================================="
echo " RESULTS:  $PASS passed,  $FAIL failed"
echo "====================================="
exit $FAIL
