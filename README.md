# Oops-Server

### File Directory Structure
```
📘server
  ├─📂compositekey     : 복합키 클래스
  ├─📂context          : Alert/Exception Message, Status 코드 클래스
  ├─📂controller
  ├─📂dto
  │  ├─🔍etc           : 다양한 곳에서 쓰이는 기본 단위의 DTO
  │  ├─🔍request       : 요청 시 쓰이는 DTO
  │  └─🔍response      : 응답 시 쓰이는 DTO
  ├─📂entity           : DB와 직접적으로 매핑되는 객체
  ├─📂initializer      : FCM, S3 설정 파일
  ├─📂repository
  ├─📂security         : token, password encoder 등의 보안 관련 파일
  └─📂service
```

### Commit Message Convention
```
feat: 새로운 기능을 추가하는 경우(일반적인 구현)

test: 테스트 코드를 추가하는 경우

refactor: 코드를 리펙토링한 경우

fix: 버그를 고친 경우

docs: 문서를 수정한 경우

style: 코드 포맷 변경, 세미콜론 누락 수정 등 코드 수정이 없는 경우

rename: 파일명 또는 폴더명을 수정한 경우

move: 코드 또는 파일의 이동이 있는 경우

remove: 코드 또는 파일을 삭제한 경우

comment: 필요한 주석 추가 및 변경
```

```
ex) feat: commit message
```
