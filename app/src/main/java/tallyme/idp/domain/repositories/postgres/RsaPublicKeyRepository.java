package tallyme.idp.domain.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import tallyme.idp.domain.entities.postgres.RsaPublicKey;

public interface RsaPublicKeyRepository extends JpaRepository<RsaPublicKey, String> {}
