# Offset / Cursor 방식, 10,000건 기준 성능 비교 리포트

## 시나리오 목적

Offset 방식과 Cursor 방식의 페이징 서능 검증 비교 목적을 위한 테스트입니다.

- 가상 사용자 수

### 테스트 환경

- 데이터 셋: 10,000건의 이미지 더미 데이터
- Image 테이블 스키마

    | **컬럼명** | **데이터 타입** | **제약 조건** | **설명** |
    | --- | --- | --- | --- |
    | `image_id` | `BIGINT` | `PRIMARY KEY`, `AUTO_INCREMENT` | 이미지의 고유 식별자 |
    | `project_id` | `BIGINT` | `NOT NULL` | 이미지가 속한 프로젝트의 ID |
    | `file_name` | `VARCHAR(255)` | `NOT NULL` | 원본 파일명 |
    | `stored_file_name` | `VARCHAR(255)` | `NOT NULL` | MinIO에 실제로 저장된 고유 파일명 |
    | `bucket_file_url` | `VARCHAR(255)` | (없음) | 원본 이미지가 MinIO에 저장된 URL |
    | `memo` | `VARCHAR(255)` | (없음) | 이미지에 대한 메모 |
    | `tag` | `VARCHAR(255)` | (없음) | 이미지 태그 |
    | `thumbnail_url` | `VARCHAR(255)` | (없음) | 썸네일 이미지가 MinIO에 저장된 경로 |
    | `thumbnail_status` | `VARCHAR(20)` | (없음, Enum Type.STRING) | 썸네일 생성 상태 (`NONE`, `PROCESSING`, `READY`, `FAILED`) |
    | `hash_value` | `VARCHAR(255)` | `UNIQUE`, `NOT NULL` | 이미지 해시 값 (중복 방지) |
    | `soft_delete` | `BOOLEAN` | (없음) | 소프트 삭제 여부 (True/False) |
    | `created_at` | `DATETIME` | `NOT NULL` | 생성 시각 (자동 생성) |
    | `updated_at` | `DATETIME` | (없음) | 최종 수정 시각 (자동 업데이트) |

- 하드웨어
    - 칩: Apple M3 Pro
    - 메모리: 36GB
    - macOS: Sequoia 15.6.1
- 소프트 웨어
    - DB: SQLite
    - 애플리케이션 서버: Java21, Spring Boot 3.5.5
    - 성능 측정 도구: JMeter

### 테스트 시나리오

- Offset 시나리오

### Offset 시나리오

- 테스트 시나리오
    - Number of Threads(user): 1
    - Ramp-up period (seconds): 1
    - Loop count: 1000
    - Request API: http://localhost:8080/projects/11/images?page=1

- 성능 측정 결과(ms)

| **Label** | **# Samples** | **Average** | **Median** | **90% Line** | **95% Line** | **99% Line** | **Min** | **Max** | **Error %** | **Throughput** | **Received KB/sec** | **Sent KB/sec** |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Offset 페이징 조회** | 1000 | 16 | 14 | 22 | 26 | 37 | 9 | 93 | 0.000% | 58.75786 | 370.89 | 8.09 |
| **TOTAL** | 1000 | 16 | 14 | 22 | 26 | 37 | 9 | 93 | 0.000% | 58.75786 | 370.89 | 8.09 |

### Cursor 페이징 시나리오

- 테스트 시나리오
    - Number of Threads(user): 1
    - Ramp-up period (seconds): 1
    - Loop count: 1000
    - Request API: http://localhost:8080/projects/11/images

- 성능 측정 결과(ms)

| **Label** | **# Samples** | **Average** | **Median** | **90% Line** | **95% Line** | **99% Line** | **Min** | **Max** | **Error %** | **Throughput** | **Received KB/sec** | **Sent KB/sec** |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Cursor Request** | 1000 | 12 | 12 | 17 | 19 | 30 | 8 | 252 | 0.000% | 75.27853 | 460.03 | 9.85 |
| **TOTAL** | 1000 | 12 | 12 | 17 | 19 | 30 | 8 | 252 | 0.000% | 75.27853 | 460.03 | 9.85 |

### 결과

Offset 페이징 방식과 Cursor 페이징 방식에서 P95부분에 결과 차이는 없다고 볼 수 있습니다.