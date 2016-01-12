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
 * Data Value Structure 
 * @author EFACEC
 *
 */
public class S_DATA_VALUE extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_DATA_VALUE(){
		active++;
		//System.out.println("new S_DATA_VALUE = "+active);
		
		this.setTam(DEF_S_DATA_VALUE); 
		}
	
	/** Entity digital value */
	private int nValue=0;
	/**
	 * Gets Entity digital value 
	 * @return Entity digital value 
	 */
	public final int getNValue(){ return nValue; }
	/**
	 * Sets Gets Entity digital value 
	 * @param _value - value to set
	 */
	public final void setNValue( int _value){ nValue = _value; }
	
	/** Entity analog value */
	private float fValue=0;
	/**
	 * Gets Entity analog value (analogs and counters)
	 * @return Entity analog value 
	 */
	public final float getFValue(){ return fValue; }
	/**
	 * Sets Entity analog value (analogs and counters)
	 * @param _value - value to set
	 */
	public final void setFValue( float _value){ fValue = _value; }
	//** Is digital value */
	public boolean bIntValue=true; 
	/**
	 * Fills buffer with all data to send to ScateX
	 */
	public void getSerialized(ByteBuffer msg)
	{
		msg.putFloat(fValue);	 
	}
	/**
	 * Retrieve data from buffer into variables
	 */
	public boolean enqueue(ByteBuffer msg)
	{
		try {
			if (bIntValue){
				nValue = msg.getInt();
			}
			else{
				fValue = msg.getFloat();
			}
			
			return true;
		} catch (Exception e) {
			
			return false;
		}
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}
	
}
