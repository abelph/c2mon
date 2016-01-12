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
package cern.c2mon.daq.ens;

import java.nio.ByteBuffer;
/**
 * Watchdog Structure
 * @author EFACEC
 *
 */
public class S_WDOG_REGINFO extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_WDOG_REGINFO(){
		active++;
		//System.out.println("new S_WDOG_REGINFO = "+active);
		
		this.setTam(DEF_S_WDOG_REGINFO); 
		}
	
	/** Timeout for WDog in seconds*/
	private int iTimRefresh;
	/**
	 * Gets Timeout for WDog 
	 * @return Timeout for WDog in seconds
	 */
	public int getITimRefresh(){ return iTimRefresh; }
	/**
	 * Sets Timeout for WDog 
	 * @param _value - Timeout for WDog in seconds
	 */
	public void setITimRefresh(int _value){ iTimRefresh = _value; }
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		msg.putInt(getITimRefresh());
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}
	
}
