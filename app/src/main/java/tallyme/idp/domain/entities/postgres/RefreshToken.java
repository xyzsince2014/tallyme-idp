package tallyme.idp.domain.entities.postgres;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_refresh_token")
public class RefreshToken implements Serializable {

  private static final long serialVersionUID = 4356732572768365275L;

  @Id
  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "created_at")
  @Nullable
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @Nullable
  private LocalDateTime updatedAt;

  public RefreshToken() {}

  public RefreshToken(String refreshToken, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.refreshToken = refreshToken;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Nullable
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(@Nullable LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Nullable
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(@Nullable LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
