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
package cern.c2mon.daq.jec.address;

import org.junit.Test;

import cern.c2mon.daq.jec.address.AnalogJECProfibusWagoAddressSpace;
import static org.junit.Assert.*;

public class AnalogJECProfibusWagoAddressSpaceTest {

    private AnalogJECProfibusWagoAddressSpace addressSpace = new AnalogJECProfibusWagoAddressSpace();
    
    @Test
    public void testWordId() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(10, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 0, 2, 3));
        assertEquals(11, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(11, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxWordId());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxWordId());
    }
    
    @Test
    public void testWordIdPLC() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(10, addressSpace.getMaxWordIdPLC());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 0, 2, 3));
        assertEquals(10, addressSpace.getMaxWordIdPLC());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(11, addressSpace.getMaxWordIdPLC());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxWordIdPLC());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxWordIdPLC());
    }
    
    @Test
    public void testWordIdMMD() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(-1, addressSpace.getMaxMMDWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("PWA", 0, 2, 3));
        assertEquals(2, addressSpace.getMaxMMDWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(2, addressSpace.getMaxMMDWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("PWA", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxMMDWordId());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxMMDWordId());
    }
    
    
}
