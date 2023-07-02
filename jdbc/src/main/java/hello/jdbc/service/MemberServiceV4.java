package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예외 누수 문제 해결
 * SQL Exception 제거
 * MemberRepository 인터페이스 의존
 */
@Slf4j
public class MemberServiceV4 {

  private final MemberRepository memberRepository;

  public MemberServiceV4(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Transactional
  public void accountTransfer(String fromId, String toId, int money) {
    bizLogic(fromId, toId, money);
  }

  private void bizLogic(String fromId, String toId, int money) {
    //비즈니스 로직 수행
    Member fromMember = memberRepository.findById(fromId);
    Member toMember = memberRepository.findById(toId);

    memberRepository.update(fromId, fromMember.getMoney() - money);
    validation(toMember);
    memberRepository.update(toId, toMember.getMoney() + money);
  }

  private static void validation(Member toMember) {
    if (toMember.getMemberId().equals("ex")) {
      throw new IllegalStateException("이체 중 예외 발생");
    }
  }
}
