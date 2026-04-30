## Traffic Control System API Testing Guide

### Base URL
```
http://localhost:8080/api
```

### API Endpoints

#### 1. Initialize Intersection
**URL:** `POST http://localhost:8080/api/traffic/intersection/{intersectionId}/initialize`

**Example:**
```bash
curl -X POST http://localhost:8080/api/traffic/intersection/MAIN_5TH/initialize
```

**Response:**
```json
{
  "id": 1,
  "intersectionId": "MAIN_5TH",
  "currentGreenDirection": "NORTH",
  "isPaused": false,
  "totalElapsedTime": 0
}
```

---

#### 2. Get Intersection State
**URL:** `GET http://localhost:8080/api/traffic/intersection/{intersectionId}/state`

**Example:**
```bash
curl -X GET http://localhost:8080/api/traffic/intersection/MAIN_5TH/state
```

---

#### 3. Get All Traffic Lights
**URL:** `GET http://localhost:8080/api/traffic/intersection/{intersectionId}/lights`

**Example:**
```bash
curl -X GET http://localhost:8080/api/traffic/intersection/MAIN_5TH/lights
```

---

#### 4. Get Specific Traffic Light
**URL:** `GET http://localhost:8080/api/traffic/intersection/{intersectionId}/light/{direction}`

**Valid Directions:** `NORTH`, `SOUTH`, `EAST`, `WEST` (case-insensitive)

**Example:**
```bash
curl -X GET http://localhost:8080/api/traffic/intersection/MAIN_5TH/light/NORTH
```

---

#### 5. Change Light Status
**URL:** `PUT http://localhost:8080/api/traffic/intersection/{intersectionId}/light/{direction}/status/{status}`

**Valid Directions:** `NORTH`, `SOUTH`, `EAST`, `WEST` (case-insensitive)
**Valid Statuses:** `RED`, `YELLOW`, `GREEN` (case-insensitive)

**Valid State Transitions:**
- RED → GREEN
- RED → RED
- GREEN → YELLOW
- YELLOW → RED

**Example:**
```bash
# Correct - Valid transition
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/light/NORTH/status/YELLOW

# Correct - Also works with lowercase
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/light/north/status/red

# Error - Invalid transition (GREEN cannot go directly to RED)
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/light/NORTH/status/RED
```

---

#### 6. Set Green Light for Direction
**URL:** `PUT http://localhost:8080/api/traffic/intersection/{intersectionId}/green/{direction}`

**Valid Directions:** `NORTH`, `SOUTH`, `EAST`, `WEST` (case-insensitive)

**Note:** This endpoint automatically transitions the previous green light to yellow then red.

**Example:**
```bash
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/green/SOUTH
```

---

#### 7. Pause Traffic
**URL:** `PUT http://localhost:8080/api/traffic/intersection/{intersectionId}/pause`

**Example:**
```bash
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/pause
```

---

#### 8. Resume Traffic
**URL:** `PUT http://localhost:8080/api/traffic/intersection/{intersectionId}/resume`

**Example:**
```bash
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/resume
```

---

#### 9. Update Remaining Times
**URL:** `PUT http://localhost:8080/api/traffic/intersection/{intersectionId}/update-times`

**Example:**
```bash
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/update-times
```

---

### Test Data

The application automatically initializes 3 intersections on startup:
- `MAIN_5TH`
- `BROADWAY_PARK`
- `DOWNTOWN_CENTER`

Each intersection has 4 traffic lights (NORTH, SOUTH, EAST, WEST):
- **NORTH:** Initially GREEN with 30 seconds remaining
- **SOUTH, EAST, WEST:** Initially RED with 90 seconds remaining

---

### Swagger UI Documentation
Visit: `http://localhost:8080/api/swagger-ui.html`

---

### H2 Database Console
Visit: `http://localhost:8080/api/h2-console`

**JDBC URL:** `jdbc:h2:mem:traffic_db`
**Username:** `sa`
**Password:** (leave empty)

---

### Common Error Codes

| Code | Status | Meaning |
|------|--------|---------|
| `INVALID_ENUM_VALUE` | 400 | Invalid direction or status value |
| `INVALID_STATE_TRANSITION` | 400 | Cannot transition from current state to requested state |
| `CONFLICTING_DIRECTIONS` | 409 | Cannot set green light (conflicting direction already green) |
| `INTERSECTION_NOT_FOUND` | 400 | Intersection ID does not exist |
| `LIGHT_NOT_FOUND` | 400 | Traffic light not found for given intersection and direction |
| `ALREADY_PAUSED` | 400 | Intersection is already paused |
| `NOT_PAUSED` | 400 | Intersection is not paused |

---

### Example Complete Workflow

```bash
# 1. Initialize intersection (already done on startup, but can reinitialize)
curl -X POST http://localhost:8080/api/traffic/intersection/MAIN_5TH/initialize

# 2. Get current state
curl -X GET http://localhost:8080/api/traffic/intersection/MAIN_5TH/state

# 3. Get all lights
curl -X GET http://localhost:8080/api/traffic/intersection/MAIN_5TH/lights

# 4. Transition current green (NORTH) to yellow
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/light/NORTH/status/YELLOW

# 5. Transition NORTH to red
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/light/NORTH/status/RED

# 6. Set green for SOUTH (this will automatically transition SOUTH from RED to GREEN)
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/green/SOUTH

# 7. Pause traffic
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/pause

# 8. Resume traffic
curl -X PUT http://localhost:8080/api/traffic/intersection/MAIN_5TH/resume
```

