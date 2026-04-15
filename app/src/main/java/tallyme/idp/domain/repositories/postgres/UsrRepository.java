package tallyme.idp.domain.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tallyme.idp.domain.entities.postgres.Usr;

@Repository
public interface UsrRepository extends JpaRepository<Usr, String> {
  Usr findByEmail(String email);
}
