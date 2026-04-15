package tallyme.idp.domain.entities.postgres;

import java.io.Serializable;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "t_rsa_public_key")
public class RsaPublicKey implements Serializable {

  private static final long serialVersionUID = 4876975004642880378L;

  @Id
  @Column(name = "kid")
  private String kid;

  @Column(name = "rsa_public_key")
  @NotNull
  private RSAPublicKey rsaPublicKey;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public RsaPublicKey() {}

  public RsaPublicKey(String kid, RSAPublicKey rsaPublicKey, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.kid = kid;
    this.rsaPublicKey = rsaPublicKey;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getKid() { return kid; }

  public void setKid(String kid) { this.kid = kid; }

  public RSAPublicKey getRsaPublicKey() {
    return rsaPublicKey;
  }

  public void setRsaPublicKey(RSAPublicKey rsaPublicKey) {
    this.rsaPublicKey = rsaPublicKey;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
