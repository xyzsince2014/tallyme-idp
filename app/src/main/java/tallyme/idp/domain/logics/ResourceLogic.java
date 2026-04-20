package tallyme.idp.domain.logics;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tallyme.idp.domain.entities.postgres.Resource;
import tallyme.idp.domain.repositories.postgres.ResourceRepository;

@Component
public class ResourceLogic {

  private final ResourceRepository resourceRepository;

  @Autowired
  public ResourceLogic(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  /**
   * Fetches the resource for the given resourceId.
   *
   * @param resourceId
   * @return Resource
   */
  public Resource getResourceByResourceId(String resourceId) {
    Optional<Resource> optionalResource = this.resourceRepository.findById(resourceId);
    return optionalResource.orElse(null);
  }
}
