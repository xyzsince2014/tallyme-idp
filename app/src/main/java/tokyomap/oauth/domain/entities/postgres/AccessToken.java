package tokyomap.oauth.domain.entities.postgres;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "t_access_token")
public class AccessToken implements Serializable {

  private static final long serialVersionUID = -71410309563653769L;

  @Id
  @Column(name = "access_token")
  private String accessToken;

  @ManyToOne
  @JoinColumn(name = "refresh_token", referencedColumnName = "refresh_token")
  private RefreshToken refreshToken;

  @Column(name = "created_at")
  @Nullable
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @Nullable
  private LocalDateTime updatedAt;

  public AccessToken() {}

  public AccessToken(String accessToken, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.accessToken = accessToken;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public AccessToken(String accessToken, RefreshToken refreshToken, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public RefreshToken getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(RefreshToken refreshToken) {
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
