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

package cern.c2mon.daq.jec.plc;

/**
 * This interface is used to implement the basic communication methods developed
 * for both PLC brands - Siemens S7 and Schneider Premium
 * If a new PLC brand or protocol is used at CERN, the developer just needs to
 * create a new class with this new brand/protocol methids and the system is
 * ready to use again.
 */
public interface PLCDriver 
{
/**
 * This method is implemented in both Siemens and Schneider classes and is used
 * to establish a connection between the driver and the PLC.
 * @param ConnectionData param - object containing the necessary data for the 
 * connection depending on if its for Siemens or for Schneider.
 * @return int - Returns a result with the operation status (0=succeeded; 
 * -1=error)
 */
  // Establishes a connection between a PC and a PLC
  public int Connect(ConnectionData param);                                     

/**
 * This method is implemented in both Siemens and Schneider classes and is used
 * to close a established connection between the driver and the PLC.
 * @param ConnectionData param - object containing the necessary data for the 
 * disconnection depending on if its for Siemens or for Schneider.
 * @return int - Returns a result with the operation status (0=succeeded; 
 * -1=error)
 */
  // Closes a established connection between PC and PLC
  public int Disconnect(ConnectionData param);                                  

/**
 * This method is implemented in both Siemens and Schneider classes and is used
 * to send a JECP frame through a established a connection between the driver and the PLC.
 * @param JECPFrames Frame - JECP message to be sent to the PLC.
 * @return int - Returns a result with the operation status (0=succeeded; 
 * -1=error)
 */  
  // Send an array of byte to a PLC
  public int Send(JECPFrames Frame);                              

/**
 * This method is implemented in both Siemens and Schneider classes and is used
 * to receive a JECP frame from a established a connection between the driver and the PLC.
 * @params JECPFrames - the buffer where the received data should be stored
 * @params int - Timeout for message reception (ms)
 * @return int  - Execution code (0 : success, -1 : error)
 */  
  // Receive data from PLC and stores it into a byte array
  public int Receive(JECPFrames buffer, int timeout);

/**
 * This method is implemented in both Siemens and Schneider classes and is used
 * to receive a JECP frame from a established a connection between the driver and the PLC.
 * The difference is that this method assumes timeout value as being 0 - infinit
 * @params JECPFrames - the buffer where the received data should be stored
 * @return int  - execution code (0 : success, -1 : error)
 */
  public int Receive(JECPFrames buffer);                                                      

}
/*//////////////////////////////////////////////////////////////////////////////
//                                 END OF CLASS                               //
//////////////////////////////////////////////////////////////////////////////*/
