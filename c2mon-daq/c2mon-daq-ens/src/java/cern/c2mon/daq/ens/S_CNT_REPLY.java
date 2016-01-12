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
 * Control Reply Structure received from ScateX
 * @author EFACEC
 *
 */
public class S_CNT_REPLY extends C_Efa_Structures {
	/**
	 * Constructor
	 *
	 */
	S_CNT_REPLY(){
		active++;
		//System.out.println("new S_CNT_REPLY = "+active);
		
		this.setTam(DEF_S_CNT_REPLY); 
	}
	
	/** Control Tag */
	private char[] caTag = new char[SIZE_SCATEXTAG + 1];
	/**
	 * Returns Tag
	 * @return control tag
	 */
	public final char[] getCTag(){ return caTag; }
	/**
	 * Sets control tag
	 * @param _value - control tag
	 */
	public void setCaTag( char[] _value)
	{ 
		char[] auxchar = new char[SIZE_SCATEXTAG + 1];
		int realsize = 0;
		for (int i = 0; i < _value.length; i++)
		{	
			auxchar[i] = _value[i];
			if( auxchar[i]!=0 ){
				realsize++;
			}
		}
		
		caTag=null;
		caTag = new char [realsize];
		// IN TEST
		//for(int i=0;i<realsize;i++)
		//	caTag[i] = auxchar[i];
		System.arraycopy( auxchar, 0, caTag , 0, realsize);		

		auxchar=null;
	}

	/** Control Response indication */
	private char cCntReply;
	/**
	 * Gets control response
	 * @return control response
	 */
	public final char getCCntReply(){ return cCntReply; }
	/**
	 * Sets control response
	 * @param _value - control response
	 */
	public final void setCCntReply(char _value){ cCntReply = _value; }
	
	/**
	 * Retrieve data from buffer into variables
	 */
	public boolean enqueue(ByteBuffer msg)
	{
		// 1 - copy ttag
		try {
			char[] auxchar = new char[SIZE_SCATEXTAG + 1];
			int realsize = 0;
			for (int i = 0; i < SIZE_SCATEXTAG + 1; i++)
			{	
				auxchar[i] = (char)msg.get();
				if( auxchar[i]==0 && realsize==0 ){
					realsize=i;
				}
			}
			
			caTag=null;
			caTag = new char [realsize];
			// IN TEST
			//for(int i=0;i<realsize;i++)
			//	caTag[i] = auxchar[i];
			System.arraycopy( auxchar, 0, caTag , 0, realsize);
			
			auxchar=null;
			
			// 2- serialize control result
			setCCntReply((char)msg.get());
			
			return true;
			
		} catch (Exception e) {
			
			return false;
		}
	}

	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// free mem
		caTag = null;
	}
	
	/**
	 * finalize method
	 */
	protected void finalize(){
		myDestruct();
		active--;
		try { super.finalize(); } catch (Throwable e) { e.printStackTrace(); }
	}
}
