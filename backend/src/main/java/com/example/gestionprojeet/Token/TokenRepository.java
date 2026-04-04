package com.example.gestionprojeet.Token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TokenRepository extends JpaRepository<Token,Integer> {
    @Query("""
select  t from  Token  t inner  join Utlisateur  u on t.user.id= u.id
where u.id =:user_id and (t.expired=false  or t.revoked=false )
""")
    List<Token>findAllValidTokensByUtlisateur(@Param("user_id")Long user);
    Optional<Token> findByToken(String token);
}
