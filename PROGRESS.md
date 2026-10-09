# 프로젝트 진행 기록

이 파일을 채워 10월 1일 발표용 PPT를 준비합니다. 구현하지 않은 기능은 '미구현'으로 표시하고, 심화 항목은 진행한 경우에만 작성하세요. 발표는 분석·조회·저장 시연을 포함해 5~10분입니다. 10월 1일 전후로 코드와 이 파일을 본인의 GitHub 저장소에 업로드하고, 저장소 링크를 강사에게 전달해 리뷰를 받습니다. 자세한 안내는 [진행 기록과 발표](docs/project-guide.md)에 있습니다.

## 1. 실행 방법

- JDK: 21
- IntelliJ에서 실행할 클래스: kr.sesac.wordcounter.Main
- 작업 디렉터리(`pom.xml`이 있는 폴더): 저장소 루트 (`java-word-counter/`)
- 설정 위치와 현재 값: `src/main/java/kr/sesac/wordcounter/Main.java` 클래스 상수
    - CSV 열: `CSV_COLUMNS = {"text"}` (Chatbot 데이터는 `{"Q", "A"}`로 바꿔 실행 후 되돌립니다.)
    - TSV 열: `TSV_COLUMNS = {"document"}`
    - HTML 본문 선택자: `HTML_SELECTOR = "#content"` (본문 안에서 제외: `script, style, nav, header, footer`)
    - 심화 6번 설정(기본값 모두 꺼짐): `USE_STOPWORDS = false`, `STOPWORDS_PATH = config/stopwords.txt`, `MIN_WORD_LENGTH = 1`, `INCLUDE_SUBFOLDERS = false`
    - 상수를 바꾼 뒤에는 프로그램을 다시 실행해야 반영이 됩니다.

## 2. 구현한 기능

| 기능 | 상태(완료·진행 중·미구현) | 확인한 입력과 결과                                                                                                                                                                                                                                                  |
|---|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TXT 카운팅 | 완료 | `samples/equivalent/basic.txt` → 9개·6종류, `expected/basic-counts.tsv`와 identical / `samples/edge/leading-symbols.txt` → 8개·7종류 / `samples/edge/empty.txt` → 0개·0종류                                                                                            |
| CSV·TSV·HTML 처리 | 완료 | `basic.csv`, `basic.html` → 각각 9개·6종류 / `samples/equivalent` 폴더(4형식) → 시도 4, 36개·6종류 / `samples/edge/quoted-lines.csv` → 5개·3종류 / `data/chatbot/chatbot.csv`(Q, A) → 86,119개·20,675종류, `expected/chatbot-counts.tsv`와 identical                                 |
| 여러 파일 순차 처리 | 완료 | `data/klue-ynat/many` → 16개 파일, 321,084개·78,309종류 (단일 파일과 동일)                                                                                                                                                                                                |
| 상위 단어·특정 단어 조회 | 완료 | `basic.txt` 분석 후 상위 2개 → `java 3회`, `자료구조 2회` / `JAVA!` 조회 → `java : 3회`                                                                                                                                                                                    |
| 전체 결과 저장 | 완료 | `out/counts.tsv`에 UTF-8·탭 구분 저장, `basic`·`chatbot`·`news-full` 정답 파일과 identical                                                                                                                                                                             |
| 잘못된 입력·실패 파일·빈 파일 처리 | 완료 | 분석 전 저장 → `먼저 1번으로 분석을 실행하세요.` / 없는 경로 → 경로 안내 후 재입력 / 메뉴에 경로 입력 → `0~5 중 하나를 입력하세요.` / N에 `abc`, `0` → `1 이상의 정수를 입력하세요.` / 조회에 `java 자바` → `단어 하나를 입력하세요.` / `samples/invalid` 폴더 → 시도 4·성공 0·실패 4, 조회·저장 불가 / 저장 실패(`out/counts.tsv` 폴더) → 실패 안내 후 조회 유지 |

## 3. 정확성 확인과 처리 시간

- 작은 기본 샘플의 전체 결과를 정답과 비교한 방법
    - 프로그램 메뉴 4번으로 `out/counts.tsv`를 저장한 뒤, IntelliJ Project 창에서 `out/counts.tsv`와 `expected/basic-counts.tsv`를 함께 선택하고 Compare Files(Cmd + D)로 비교 → "Contents are identical"
- CSV 따옴표·줄바꿈을 확인한 결과
    - `samples/edge/quoted-lines.csv` → 5개·3종(정답과 일치). Commons CSV로 파싱해 따옴표 안의 쉼표·줄바꿈·`""`를 처리. 닫히지 않은 따옴표(`samples/invalid/broken-quote.csv`)는 반복 중 발생하는 `UncheckedIOException`을 잡아 그 파일만 실패 처리
- 일부 파일이 실패했을 때 확인한 결과
    - `data/local/error-demo`에 `basic.txt`와 `broken-quote.csv`를 넣고 분석 → 시도 2·성공 1·실패 1, 9개·6종. 저장 결과가 `basic-counts.tsv`와 identical(실패 파일의 앞부분 단어가 섞이지 않음). 같은 폴더에 `memo.bin` 추가 → 건너뜀만 0→1, 나머지 결과 동일
- 결과 저장 파일 위치: `out/counts.tsv`


| 입력 | 데이터 건수 / 파일 수 | 전체 단어 수 | 종류 수 | 처리 시간 | 완료·오류 |
|---|---|---|---|---|---|
| `data/klue-ynat/news-1000.csv` | 1,000 / 1 | 6,991 | 5,052 | 14.4ms | 완료 |
| `data/klue-ynat/news-10000.csv` | 10,000 / 1 | 70,374 | 28,871 | 41.2ms | 완료 |
| `data/klue-ynat/news-full.csv` | 45,678 / 1 | 321,084 | 78,309 | 114.5ms | 완료 |
| `data/klue-ynat/many` | 45,678 / 16, 순차 처리 | 321,084 | 78,309 | 122.5ms | 완료 |


- 전체 파일 하나와 16개 파일의 **모든 단어별 횟수**를 비교한 방법과 결과:  
  - 방법 : news-full 분석 후 저장한 `out/counts.tsv`와 `expected/news-full-counts.tsv`를 Compare Files로 비교 → identical. many는 같은 정답을 쓰도록 되어 있어 전체 단어 수·종류 수 일치로 확인
  - 결과 : 두 입력 모두 321,084개·78,309종류.

## 4. 구현 중 해결한 문제

1~3가지를 골라 적으세요. 잘 해결되지 않은 문제도 시도한 내용과 함께 적어도 됩니다.

### 문제 1. 실패한 CSV의 앞부분 단어가 합계에 섞일 수 있음
- 문제: `broken-quote.csv`처럼 읽는 도중에 오류가 나는 파일은 오류 전까지 읽은 단어가 이미 집계되어 있을 수 있습니다.
- 원인: 전체 결과 Map 하나에 바로 더하면 예외가 나기 전까지 더한 값이 그대로 남는다.
- 해결하거나 시도한 방법: 파일마다 임시 집계를 새로 만들어 세고, 파일 하나를 끝까지 성공한 경우에만 전체 집계에 합쳤습니다. 실패하면 예외로 빠져나갑니다.
- 확인한 입력과 결과: `data/local/error-demo`(basic.txt + broken-quote.csv) → 시도 2·성공 1·실패 1, 저장 결과가 `basic-counts.tsv`와 identical

### 문제 2. Chatbot 데이터 분석 실패
- 문제: `data/chatbot/chatbot.csv`를 분석하자 `분석 열이 헤더에 없습니다: text`로 실패했습니다.
- 원인: Chatbot CSV의 분석 열은 `Q`, `A`인데 설정은 뉴스용 `text` 그대로였습니다. 처음에는 경로 입력 뒤에 설정을 붙여 입력했지만 설정은 실행 중 입력이 아니라 코드 상수입니다.
- 해결하거나 시도한 방법: `CSV_COLUMNS`를 `{"Q", "A"}`로 바꾸고 다시 실행했습니다. 확인 후 `{"text"}`로 되돌림.
- 확인한 입력과 결과: 86,119개·20,675종, `expected/chatbot-counts.tsv`와 identical. 열이 없는 경우에도 프로그램이 멈추지 않고 그 파일만 실패 처리되는 것을 함께 확인했습니다.

## 5. 심화(진행한 경우만)

### 6번 분석 기능 확장: 불용어 파일, 최소 단어 길이, 하위 폴더 탐색

- 사용법: `Main.java` 상수로 켜고 끕니다. 기본값은 모두 꺼짐이고 이때 결과는 필수 요구사항 정답과 같습니다.
    - 불용어: `USE_STOPWORDS = true`, 파일은 `config/stopwords.txt`(UTF-8, 한 줄에 단어 하나, 빈 줄·`#` 줄 무시). 각 줄에도 단어 규칙을 적용해 `JAVA!`라고 적어도 `java`로 등록
    - 최소 길이: `MIN_WORD_LENGTH = N` → N글자 미만 단어 제외(한글,영문 모두 1글자 = 1)
    - 하위 폴더: `INCLUDE_SUBFOLDERS = true` → 이름 순서로 탐색. 지원하지 않는 파일은 건너뜀 수에 포함하고 폴더 자체는 세지 않습니다.. 심볼릭 링크 폴더는 따라가지 않음.


| 기능 | 설정 | 입력 | 결과                                                            |
|---|---|---|---------------------------------------------------------------|
| 불용어 | `java`, `자료구조` 등록 | `basic.txt` | 9 → 4개·4종류, 불용어 5개 제외(java 3 + 자료구조 2), `java` 조회 0회          |
| 불용어 파일 없음 | 경로의 파일이 없음 | `basic.txt` | `불용어 파일을 읽을 수 없어 분석을 시작하지 않습니다 ... (파일이 없습니다)`, 이전 결과 유지      |
| 최소 길이 | 2 | `basic.txt` | 9개·6종류 그대로 (1글자 단어 없음)                                        |
| 최소 길이 | 2 | `news-1000.csv` | 6,658개·4,910종류, 짧은 단어 333개 제외 (6,658 + 333 = 6,991)           |
| 최소 길이(경계값) | 4 | `basic.txt` | 7개·4종류, `ㅋㅋ`(2글자)·`자바를`(3글자) 제외, `java`·`자료구조`·`공부했다`(4글자) 유지 |
| 하위 폴더 | 끔 | `data/local` | `폴더에 지원하는 파일이 없습니다 (건너뜀 0개)`, 다시 입력                           |
| 하위 폴더 | 켬 | `data/local` | 시도 2·성공 1·실패 1·건너뜀 1, 9개·6종                                   |
| 필터 모두 끔 | 기본값 | `basic.txt`, `news-full.csv` | 정답 파일과 Compare Files identical                                |

### 8번 객체지향 구조 개선

- `Main` 클래스(static 메서드·static 상태)를 역할별 클래스 14개, 패키지 5개로 나눴습니다.

| 패키지 | 클래스 | 역할 |
|---|---|---|
| `kr.sesac.wordcounter` | `Main` | 설정 값, 확장자→파서 등록, 객체 연결 후 실행 |
| `console` | `ConsoleApp` | 메뉴, 입력 검증, 결과 출력 (파일 형식은 모름) |
| `analysis` | `Analyzer` | 경로→파일 목록, 불용어 읽기, 파일별 집계, 성공한 결과만 합산, 시간 측정 |
| `analysis` | `AnalysisResult` | 분석 1회의 결과 (기존 static 필드를 객체 하나로 묶음) |
| `parser` | `DocumentParser` | 파서 공통 인터페이스: `List<String> extractTexts(Path)` |
| `parser` | `TxtParser`, `CsvParser`, `TsvParser`, `HtmlParser` | 형식별로 분석할 텍스트 조각을 꺼냄 |
| `parser` | `HeaderColumns` | CSV·TSV 공통 헤더 열 찾기 |
| `parser` | `ParserRegistry` | 확장자 → 파서 등록부, 지원 확장자 안내문 생성 |
| `word` | `WordRules` | 단어 분리, 소문자·숫자 규칙, 불용어·최소 길이 판단 (파일 형식은 모름) |
| `word` | `WordCounts` | 단어 집계, 파일별 결과 합치기, 정렬, 제외 수 (파일·콘솔은 모름) |
| `output` | `TsvResultWriter` | `out/counts.tsv` 저장 (집계 방법은 모름) |

- 새 형식을 추가할 때 바꾸는 곳: `parser` 패키지에 `DocumentParser`를 구현한 새 클래스(예: `JsonParser`) 1개를 만들고, `Main`에 `registry.register("json", new JsonParser(...));` 한 줄을 추가한다.
- 확인 질문
    - 콘솔은 형식을 몰라도 되게 만들었습니다. 형식은 `Analyzer`와 `ParserRegistry`가 하고, 콘솔은 받은 메시지만 보여줍니다. 그래서 JSON을 추가해도 콘솔 코드는 그대로입니다.
    - 저장 위치를 바꿔도 파서·집계 코드는 그대로다. (저장은 `TsvResultWriter` 하나에만 있음)
    - 병렬 처리: 파일 하나를 처리하는 `Analyzer.countFile()`은 호출마다 새 `WordCounts`를 만들고 공유 상태를 바꾸지 않아 내용은 그대로 쓸 수 있습니다. 다만 지금은 `private`이고 `analyze()`가 순서대로 불러서 병렬로 바꾸려면 `analyze()`의 반복,합산 부분은 고쳐야 한다고 생각합니다.
    - 저장 위치를 DB로 바꿀 때: 파서와 집계 코드는 그대로입니다. 저장 인터페이스를 만들지 않아서 `ConsoleApp`과 `Main`에서 저장 클래스 타입을 바꿔야 한다고 생각합니다.
- 바꾸기 전과 후의 검증 결과: 구조를 바꾼 뒤 아래를 다시 실행해 모두 이전과 같은 결과를 확인했습니다.
    - 분석 전 저장 안내, N 오류(`abc`, `0`), 조회 오류(`java 자바`), `JAVA!` 조회
    - `basic.txt`·`basic.csv`·`basic.html` 각 9개·6종, `samples/equivalent` 36개·6종, `quoted-lines.csv` 5개·3종
    - `samples/invalid` 시도 4·성공 0·실패 4
    - `news-full.csv`, `many` 각 321,084개·78,309종
    - `chatbot.csv`(Q, A) 86,119개·20,675종
    - 6번 기능: 불용어 4개·4종류, 최소 길이 4 → 7개·4종, 하위 폴더 시도 2·성공 1·실패 1·건너뜀 1

## 6. AI 대화 또는 참고 자료


- 웹 대화에서 물어본 개념·힌트·오류 설명
    - TSV에서 `split("\t", -1)`의 `-1`이 없으면 줄 끝 빈 셀이 사라져 셀 수 검사가 틀림
    - CSV는 따옴표 안 쉼표·줄바꿈 때문에 `split(",")`로 처리할 수 없음 → Commons CSV 사용, 반복 중 오류는 `UncheckedIOException`으로 던져짐
    - jsoup에 파일을 직접 넘기면 UTF-8이 깨진 파일도 조용히 읽음 → `Files.readString`으로 먼저 읽어 실패를 잡음
    - 실패 파일의 앞부분 단어가 섞이지 않게 파일별 임시 집계를 만든 뒤 성공한 것만 합치는 방법
    - 하위 폴더 탐색 시 심볼릭 링크 폴더를 따라가면 무한하게 돌 수 있음
- 도움을 바탕으로 직접 구현한 내용: (본인이 직접 작성·수정한 부분을 적으세요)
- 직접 확인한 입력과 결과:
- 참고 링크:

## 7. 발표할 내용

- 구현한 기능과 전체 처리 흐름: 경로 입력 → 파일 목록 결정(확장자 판별, 건너뜀 집계, 하위 폴더 옵션) → 파일마다 파서 선택(`ParserRegistry`) → 텍스트 조각 추출(`DocumentParser`) → 단어 분리·필터(`WordRules`) → 파일별 임시 집계(`WordCounts`) → 성공 파일만 합산 → 정렬 → 조회·저장
- 시연할 파일·폴더와 정답: `samples/equivalent/basic.txt`(9개·6종), `samples/equivalent` 폴더(36개·6종), `data/local/error-demo`(시도 2·성공 1·실패 1·건너뜀 1)
- 분석 → 조회 → 저장 시연: basic.txt 분석 → 상위 2개(`java 3회`, `자료구조 2회`) → `JAVA!` 조회 → 저장 후 Compare Files로 정답과 identical 확인
- 해결한 문제 또는 성능 실험에서 알게 된 점: 파일별 임시 집계로 실패 파일의 앞부분 단어 제외, 설정 상수(`CSV_COLUMNS`)를 바꾸지 않아 Chatbot 분석이 실패한 문제. 구조 개선 후 새 형식 추가 시 파서 1개 + 등록 1줄만 바뀌는 구조
- 남은 문제와 더 개선하고 싶은 부분: 파서가 파일 하나의 텍스트를 `List`로 한꺼번에 돌려줘 아주 큰 파일에서는 메모리를 많이 씁니다. CSV 형식 오류 메시지가 Commons CSV 영어 원문 그대로 출력됩니다. 설정 변경에 재실행이 필요함(설정 화면·설정 파일 미구현)