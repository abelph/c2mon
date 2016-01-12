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

/**
 * Event Entity
 * @author EFACEC
 * @version 2.0
 */
public class CEfaEvent extends Object { 
	
	static int active=0;

	/** scada entity */ 
	private CEfaEntity scadaEntity = null;

	
	/**
	 * Constructor
	 *
	 */
	public CEfaEvent(){
		active++;
		//System.out.println("new CEfaEvent = "+active);
	}
	
	/**
	 * set reference of a entity
	 * @param ref - entity reference
	 */
	public void setEntityRef(CEfaEntity ref){
		// scadaEntity =  ref;
		scadaEntity = null;
	
		if( CEfaEntityDig.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityDig();
		}
		else if( CEfaEntityAnl.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityAnl();
		}
		else if( CEfaEntityCtrResp.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityCtrResp();
			((CEfaEntityCtrResp)scadaEntity).setOrder(((CEfaEntityCtrResp)ref).getOrder());
		}
		else if( CEfaEntityCtr.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityCtr();
			((CEfaEntityCtr)scadaEntity).setOrder(((CEfaEntityCtr)ref).getOrder());
		}
		else if( CEfaEntityCnt.class.isInstance(ref) ){
			scadaEntity = new CEfaEntityCnt();
		}
		else {
			System.out.println("ERRO NO setEntityRef");
		}
		
		scadaEntity.setId(ref.getSxId(),ref.getSxId());
		scadaEntity.bSetInvCode(ref.getInvCode());
		scadaEntity.bSetTTag(ref.getTTag().getSecs(),ref.getTTag().getMSecs());
		scadaEntity.bSetValue(ref.fGetValue());
				
	}
	
	
	/**
	 * get reference of entity
	 * @return a reference of a CEfaEntity
	 */
	public final CEfaEntity getEntityRef(){
		return scadaEntity;
	}
	
	
	/**
	 * implementation of memory destructor
	 *
	 */
	public void myDestruct()
	{
		// don't destruct the var because it's a reference and not a var itself
		if(scadaEntity!=null){
			scadaEntity.myDestruct();
		}
		scadaEntity = null;
		
		
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
