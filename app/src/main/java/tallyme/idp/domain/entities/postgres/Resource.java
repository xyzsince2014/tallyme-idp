package tallyme.idp.domain.entities.postgres;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_resource")
public class Resource implements Serializable {

  private static final long serialVersionUID = -7055018741024014123L;

  @Id
  @Column(name = "resource_id")
  private String resourceId;

  @Column(name = "resource_secret")
  private String resourceSecret;

  @Column(name = "created_at")
  @Nullable
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @Nullable
  private LocalDateTime updatedAt;

  public Resource() {}

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceSecret() {
    return resourceSecret;
  }

  public void setResourceSecret(String resourceSecret) {
    this.resourceSecret = resourceSecret;
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
