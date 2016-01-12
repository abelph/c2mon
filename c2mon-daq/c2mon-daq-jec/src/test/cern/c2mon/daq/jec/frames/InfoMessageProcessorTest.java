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
package cern.c2mon.daq.jec.frames;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.io.CharArrayReader;
import java.io.IOException;

import org.apache.kahadb.util.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.IJECFrameController;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.config.PLCConfiguration;
import cern.c2mon.daq.jec.plc.JECIndexOutOfRangeException;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.daq.jec.plc.StdConstants;

public class InfoMessageProcessorTest {

    private InfoMessageProcessor infoMessageProcessor;
    private PLCObjectFactory plcFactory;
    private IJECFrameController frameController;
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        frameController = createMock(IJECFrameController.class);
        infoMessageProcessor = new InfoMessageProcessor(plcFactory, StdConstants.INFO_MSG, frameController, equipmentLogger );
    }
    
    @Test
    public void testInvalidation() throws IOException, JECIndexOutOfRangeException {
        JECPFrames frame = plcFactory.getRawRecvFrame();
        frame.SetMessageIdentifier(StdConstants.INFO_MSG);
        frame.SetDataType((byte) StdConstants.DP_SLAVE_LOST);
        frame.SetDataStartNumber((short) StdConstants.SLAVE_INVALIDATE);
        String slaveName = "slave";
        writeJECString(frame, slaveName);
        
        // expected call
        frameController.invalidateSlaveTags(slaveName, frame.GetJECCurrTimeMilliseconds());
        
        replay(frameController);
        infoMessageProcessor.processJECPFrame(frame);
        verify(frameController);
    }
    
    @Test
    public void testValidation() throws IOException, JECIndexOutOfRangeException {
        JECPFrames frame = plcFactory.getRawRecvFrame();
        frame.SetMessageIdentifier(StdConstants.INFO_MSG);
        frame.SetDataType((byte) StdConstants.DP_SLAVE_LOST);
        frame.SetDataStartNumber((short) StdConstants.SLAVE_VALIDATE);
        String slaveName = "slave";
        writeJECString(frame, slaveName);
        
        // expected call
        frameController.revalidateSlaveTags(slaveName, frame.GetJECCurrTimeMilliseconds());
        
        replay(frameController);
        infoMessageProcessor.processJECPFrame(frame);
        verify(frameController);
    }
    
    @Test
    public void testERROR() throws IOException, JECIndexOutOfRangeException {
        JECPFrames frame = plcFactory.getRawRecvFrame();
        frame.SetMessageIdentifier(StdConstants.INFO_MSG);
        frame.SetDataType((byte) StdConstants.DP_SLAVE_LOST);
        frame.SetDataStartNumber((short) StdConstants.SLAVE_VALIDATE);
        String slaveName = "ERROR";
        writeJECString(frame, slaveName);
        
        // nothing should happen
        
        replay(frameController);
        infoMessageProcessor.processJECPFrame(frame);
        verify(frameController);
    }

    private void writeJECString(JECPFrames frame, String slaveName) throws IOException, JECIndexOutOfRangeException {
        CharArrayReader reader = new CharArrayReader(slaveName.toCharArray());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int curChar;
        while ((curChar = reader.read()) != -1) {
            out.write(curChar);
        }
        out.write(StdConstants.END_OF_TEXT);
        frame.AddJECData(out.toByteArray(), 0, slaveName.length() + 1);
    }
}
