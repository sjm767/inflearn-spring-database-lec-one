package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

  private final DataSource dataSource;
  private final MemberRepositoryV2 memberRepository;

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    Connection con = dataSource.getConnection();
    try {
      con.setAutoCommit(false);

      bizLogic(con, fromId, toId, money);

      con.commit(); //성공시 커밋
    } catch (Exception e) {
      con.rollback(); //실패시 롤백
      throw new IllegalStateException(e);
    } finally {
      release(con);
    }
  }

  private void bizLogic(Connection con, String fromId, String toId, int money)
      throws SQLException {
    //비즈니스 로직 수행
    Member fromMember = memberRepository.findById(con, fromId);
    Member toMember = memberRepository.findById(con, toId);

    memberRepository.update(con, fromId, fromMember.getMoney() - money);
    validation(toMember);
    memberRepository.update(con, toId, toMember.getMoney() + money);
  }

  private static void validation(Member toMember) {
    if (toMember.getMemberId().equals("ex")) {
      throw new IllegalStateException("이체 중 예외 발생");
    }
  }

  private static void release(Connection con) {
    if (con != null) {
      try {
        con.setAutoCommit(true); //Auto Commit 설정을 원복한다.
        con.close();

      } catch (Exception e) {
        log.info("error", e);
      }
    }
  }



}
