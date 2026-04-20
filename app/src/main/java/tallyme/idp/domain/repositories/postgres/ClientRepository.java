package tallyme.idp.domain.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tallyme.idp.domain.entities.postgres.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {}
