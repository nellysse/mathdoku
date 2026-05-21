# Mathdoku 

Mathdoku is an educational mathematical puzzle game that combines standard Sudoku mechanics with algorithmic equations (Algebra, Logarithms, Calculus) to challenge users.

---

## 🔌 REST API Endpoints

**Base URL:** `http://localhost:8080/api/game`

All endpoints are configured to allow CORS from any origin during development.

### 1. Start a New Game
**`GET /new?difficulty={difficulty}`**

Creates a new game session and returns the 9x9 masked grid. The grid contains plain digits, empty cells represented by `"0"`, and mathematical equations rendered in LaTeX.

- **Query Parameters**: 
  - `difficulty` (String: `easy`, `medium`, `hard`)
- **Response `200 OK`**: 
```json
{
  "sessionId": "a1b2c3d4-e5f6-...",
  "difficulty": "medium",
  "cells": [
    ["2", "0", "\\log_2 8", "0", "9", ...],
    ...
  ]
}
```

### 2. Instant Cell Validation
**`GET /{sessionId}/validate?row={row}&col={col}&value={value}`**

Validates a single user input in real-time. Useful for implementing instant "lives" or "mistakes" tracking on the frontend without waiting for a full board submission.

- **Path Variables**:
  - `sessionId` (UUID String)
- **Query Parameters**: 
  - `row` (int) - 0-indexed row number.
  - `col` (int) - 0-indexed column number.
  - `value` (int) - The number the user entered.
- **Response `200 OK`**: `Boolean` (`true` if the input is correct, `false` otherwise).

### 3. Get a Hint
**`GET /{sessionId}/hint?row={row}&col={col}`**

Reveals the correct digit for a specific cell.

- **Path Variables**:
  - `sessionId` (UUID String)
- **Query Parameters**: 
  - `row` (int) - 0-indexed row number.
  - `col` (int) - 0-indexed column number.
- **Response `200 OK`**: `Integer` (e.g., `5`).

### 4. Fetch Full Solution
**`GET /{sessionId}/solve`**

Retrieves the entire solved 9x9 grid for the current session.

- **Path Variables**:
  - `sessionId` (UUID String)
- **Response `200 OK`**: 
```json
[
  [2, 5, 3, 9, 8, ...],
  [8, 1, 9, 4, 2, ...],
  ...
]
```

### 5. Check Entire Board
**`POST /check`**

Submits the complete board to verify if the user's answers are entirely correct.

- **Request Body** (JSON): 
```json
{
  "sessionId": "a1b2c3d4-e5f6-...",
  "userGrid": [
    ["2", "5", "3", ...],
    ...
  ]
}
```
- **Response `200 OK`**:
```json
{
  "correct": false,
  "message": "Есть ошибки. Попробуйте ещё раз.",
  "solution": [[2, 5, 3, ...], ...]
}
```
