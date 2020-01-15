package cern.c2mon.server.common.control;

import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.common.tag.AbstractInfoTagCacheObject;
import cern.c2mon.server.common.tag.InfoTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

import static cern.c2mon.server.common.util.Java9Collections.setOf;

@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class ControlTag extends AbstractInfoTagCacheObject implements InfoTag {
  /**
   * Id of the {@link Supervised} object
   */
  final long supervisedId;

  /**
   * Type of the {@link Supervised} object
   */
  final SupervisionEntity supervisedEntity;

  private final Boolean faultValue = Boolean.FALSE; // always FALSE in TIM; TRUE not supported

  public ControlTag(@NonNull Long id, @NonNull Long supervisedId, SupervisionEntity supervisedEntity) {
    super(id);
    setDataType("Boolean");
    this.supervisedId = supervisedId;
    this.supervisedEntity = supervisedEntity;
  }

  @Override
  public Boolean getValue() {
    return (Boolean) super.getValue();
  }

  @Override
  public Set<Long> getEquipmentIds() {
    return supervisedEntity == SupervisionEntity.EQUIPMENT ? setOf(supervisedId) : Collections.emptySet();
  }

  @Override
  public Set<Long> getProcessIds() {
    return supervisedEntity == SupervisionEntity.PROCESS ? setOf(supervisedId) : Collections.emptySet();
  }

  @Override
  public Set<Long> getSubEquipmentIds() {
    return supervisedEntity == SupervisionEntity.SUBEQUIPMENT ? setOf(supervisedId) : Collections.emptySet();
  }
}
