package pl.eurokawa.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TokenRepository extends JpaRepository<Token,Long>, JpaSpecificationExecutor<Token> {

    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND t.type = :tokenType ORDER BY t.id DESC LIMIT 1")
    Token findLastUserTokenByTokenType(@Param("userId") Integer userId,@Param("tokenType") TokenType tokenType);
}
