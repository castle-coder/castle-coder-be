## 커밋 컨벤션
[Conventional Commits](https://www.conventionalcommits.org/ko/v1.0.0/)를 따른다.

### 커밋 메세지 구조
커밋 메시지는 다음과 같은 구조로 되어야 한다.
```
<타입>[적용 범위(선택 사항)]: <설명> #이슈번호

[본문(선택 사항)]

[꼬리말(선택 사항)]
```

## 커밋 타입
커밋 타입은 다음과 같다.
- `fix`: 코드베이스에서 버그를 패치
- `feat`: 코드베이스에서 새 기능이 추가됨
- `refactor`: 코드를 리팩토링함
- `BREAKING CHANGE`: 단절적 API 변경(breaking API change). 타입/스코프 뒤에 !를 붙이기도 함
- `build`: 빌드 관련 커밋
- `ci`: ci 관련 커밋
- `cd`: cd 관련 커밋
- `chore`: 코드와 관련없는 설정들을 변경했을 때의 커밋
- `docs`: 문서 변경 커밋
- `revert`: 커밋을 되돌렸을 때
- `style`: 단순히 코드를 포맷팅 했을 때
- `test`: 테스트 관련 커밋
- `perf`: 성능개선에 대한 커밋

## 코드 컨벤션
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)를 베이스로, 좀 더 가독성 있게 수정한 **우테코 Java Style Guide**를 따른다.

- IntelliJ Settings > Editor > Code Style > Java에서 [intellij-java-wooteco-style.xml](https://github.com/woowacourse/woowacourse-docs/blob/main/styleguide/java/intellij-java-wooteco-style.xml)을 Import해 적용한다.
- IntelliJ Settings > Tools > Actions on Save > Reformat code 를 체크하고, 되도록 정렬 후 커밋하도록 한다.
