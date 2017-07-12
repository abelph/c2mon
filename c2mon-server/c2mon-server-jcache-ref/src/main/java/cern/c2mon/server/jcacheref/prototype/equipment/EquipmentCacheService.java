package cern.c2mon.server.jcacheref.prototype.equipment;

import javax.cache.processor.EntryProcessorException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.jcacheref.prototype.common.RuntimeCacheReconfigurator;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class EquipmentCacheService implements EquipmentCommandCRUD {

  private RuntimeCacheReconfigurator runtimeCacheReconfigurator;

  private EquipmentCommandCRUD equipmentCommandCRUD;

  @Autowired
  public EquipmentCacheService(EquipmentCommandCRUD equipmentCommandCRUD) {
    this.equipmentCommandCRUD = equipmentCommandCRUD;
  }

  @Override
  public void addCommandToEquipment(Long equipmentId, Long commandId) throws EntryProcessorException {
    equipmentCommandCRUD.addCommandToEquipment(equipmentId, commandId);
  }

  @Override
  public void removeCommandFromEquipment(Long equipmentId, Long commandId) {
    equipmentCommandCRUD.removeCommandFromEquipment(equipmentId, commandId);
  }
}
