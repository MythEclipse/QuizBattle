# Database Schema Documentation

This document describes the SQLite database schema for the QuizBattle application. The database is implemented using the Room Persistence Library.

## Database Info

- **Name:** `quiz_battle_database`
- **Version:** 3

> **Note:** Version 3 removed the `friends` table as the friend feature was deprecated.

---

## 1. Table: `users`

Stores local user account information.

| Column Name  | Type    | Constraints                | Description                |
| ------------ | ------- | -------------------------- | -------------------------- |
| `id`         | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique user ID             |
| `username`   | TEXT    | NOT NULL                   | User's display name        |
| `email`      | TEXT    | NOT NULL                   | User's email address       |
| `password`   | TEXT    | NOT NULL                   | Hashed password            |
| `points`     | INTEGER | DEFAULT 0                  | Total points accumulated   |
| `wins`       | INTEGER | DEFAULT 0                  | Total battles won          |
| `losses`     | INTEGER | DEFAULT 0                  | Total battles lost         |
| `totalGames` | INTEGER | DEFAULT 0                  | Total games played         |
| `createdAt`  | INTEGER | DEFAULT (current time)     | Account creation timestamp |
| `isLoggedIn` | INTEGER | DEFAULT 0 (false)          | Login session status       |

---

## 2. Table: `questions`

Stores quiz questions for offline battle mode.

| Column Name          | Type    | Constraints                | Description                         |
| -------------------- | ------- | -------------------------- | ----------------------------------- |
| `id`                 | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique question ID                  |
| `questionText`       | TEXT    | NOT NULL                   | The question content                |
| `answer1`            | TEXT    | NOT NULL                   | Option A                            |
| `answer2`            | TEXT    | NOT NULL                   | Option B                            |
| `answer3`            | TEXT    | NOT NULL                   | Option C                            |
| `answer4`            | TEXT    | NOT NULL                   | Option D                            |
| `correctAnswerIndex` | INTEGER | NOT NULL                   | Index of correct answer (0-3)       |
| `category`           | TEXT    | DEFAULT "General"          | Question category (e.g. Science)    |
| `difficulty`         | TEXT    | DEFAULT "Medium"           | Difficulty level (Easy/Medium/Hard) |
| `isActive`           | INTEGER | DEFAULT 1 (true)           | Soft delete flag                    |

---

## 3. Table: `game_history`

Stores history of completed battles.

| Column Name      | Type    | Constraints                | Description                    |
| ---------------- | ------- | -------------------------- | ------------------------------ |
| `id`             | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique match ID                |
| `userId`         | INTEGER | NOT NULL                   | ID of the user who played      |
| `opponentName`   | TEXT    | NOT NULL                   | Name of opponent (or "AI Bot") |
| `userScore`      | INTEGER | NOT NULL                   | User's final score/health      |
| `opponentScore`  | INTEGER | NOT NULL                   | Opponent's final score/health  |
| `isVictory`      | INTEGER | NOT NULL                   | Boolean (1 = Win, 0 = Loss)    |
| `totalQuestions` | INTEGER | NOT NULL                   | Number of questions in match   |
| `playedAt`       | INTEGER | DEFAULT (current time)     | Timestamp of match completion  |
| `gameMode`       | TEXT    | DEFAULT "offline"          | Type of game (offline/online)  |

---

## 4. Table: `user_question_history`

Stores user's question answer history.

| Column Name      | Type    | Constraints                | Description                        |
| ---------------- | ------- | -------------------------- | ---------------------------------- |
| `id`             | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique history ID                  |
| `userId`         | INTEGER | NOT NULL                   | ID of the user                     |
| `questionId`     | INTEGER | NOT NULL                   | ID of the question answered        |
| `answeredAt`     | INTEGER | DEFAULT (current time)     | Timestamp when question answered   |

---

## Entity Relationships (ERD)

```mermaid
erDiagram
    USERS ||--o{ GAME_HISTORY : plays
    USERS ||--o{ USER_QUESTION_HISTORY : answers

    USERS {
        long id PK
        string username
        string email
    }

    GAME_HISTORY {
        long id PK
        long userId FK
        boolean isVictory
    }

    USER_QUESTION_HISTORY {
        long id PK
        long userId FK
        long questionId FK
    }

    QUESTIONS {
        long id PK
        string category
        string difficulty
    }
```

---

## Migration Notes

### Version 1 → 2
- Added `user_question_history` table

### Version 2 → 3
- Removed `friends` table (Friend feature deprecated)
