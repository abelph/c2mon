/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.server.history.structure;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.type.TypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mruizgar
 */
@Slf4j
public final class HistoricTagConverter implements LoggerConverter<Tag> {

    /** The maximum length for the reportText */
    private static final int MAX_LENGTH = 1000;

    /** The index to which the text will be significative */
    private static final int SPLIT_INDEX = 99;

    /**
     * Jackson object used for converting DataTagQuality to String.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a new object whose type is set up based in the parameters
     *
     * @param tag
     *            HistoricTag object containing the value and type
     *            to which a new object has to be created
     *
     * @return An object of the same type and value as indicated per the
     *         parameters
     */
    private static Object toTagValue(final HistoricTag tag) {
        Object tagValue = null;
        if (tag.getTagValue() != null) {
            tagValue = TypeConverter.cast(tag.getTagValue(), tag
                    .getTagDataType());
        }
        return tagValue;
    }

    @Override
    public Loggable convertToLogged(Tag tag) {
   // create an empty DataHistoricTag object
      HistoricTag historicTag = new HistoricTag();
      // Populate its fields with data from the object received as a parameter
      historicTag.setTagId(tag.getId().longValue());
      historicTag.setTagName(tag.getName());

      if (tag.getValue() != null) {
        try {

          historicTag.setTagValue(mapper.writeValueAsString(tag.getValue()));

        } catch (JsonProcessingException e) {
          log.error("Could nor parse the invalid states of the tag "+tag.getId()+" into a String.", e);
        }
      } else {

          historicTag.setTagValue(null);
      }

      historicTag.setTagValueDesc(tag.getValueDescription());
      historicTag.setTagDataType(tag.getDataType());

      if (tag instanceof DataTag || tag instanceof ControlTag) {
        historicTag.setSourceTimestamp(((DataTag) tag).getSourceTimestamp());
        historicTag.setDaqTimestamp(((DataTag) tag).getDaqTimestamp());
      }

      historicTag.setServerTimestamp(tag.getCacheTimestamp());

      int code = 0;
      if (tag.getDataTagQuality() != null) {
        for (TagQualityStatus status : tag.getDataTagQuality().getInvalidQualityStates().keySet()) {
          code = (int) (code + Math.pow(2, status.getCode()));
        }
      }

      historicTag.setTagQualityCode(code); //for longterm log and statistics purpose

      try {
        historicTag.setTagQualityDesc(mapper.writeValueAsString(tag.getDataTagQuality().getInvalidQualityStates()));
      } catch (JsonProcessingException e) {
        log.error("Could not parse the invalid states of the tag "+tag.getId()+" into a String.", e);
      }

      if (historicTag.getTagQualityDesc() != null && historicTag.getTagQualityDesc().length() > MAX_LENGTH) {
          historicTag.setTagQualityDesc("{\"UNKNOWN_REASON\":\"Quality description was too long: unable to store in history table\"}");
      }

      historicTag.setTagDir("I");
      historicTag.setTagMode(tag.getMode());
      return historicTag;
    }

}