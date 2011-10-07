package com.freshbooks.model;

import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * recurring.create
 * Create a new recurring profile. The method arguments are nearly identical to invoice.create, but include five additional fields:
 */
@XStreamAlias("recurring")
public class Recurring extends AbstractInvoiceTemplate implements Serializable {
	private static final long serialVersionUID = -4322862130184353619L;
	
	
	@XStreamAlias("recurring_id")
    Long id;
    
    private int occurrences;
    
    private String frequency;
    
    private boolean stopped;
	
	@XStreamAlias("send_email")
	private boolean sendEmail;
	
	@XStreamAlias("send_snail_mail")
	private boolean sendSnailMail;
   

    private Autobill autobill;


	public int getOccurrences() {
		return occurrences;
	}

	@Override
	public Long getId(){
		return id;
	}
	
	@Override
	public void setId(Long id){
		this.id = id;
	}

	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public boolean isSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	public boolean isSendSnailMail() {
		return sendSnailMail;
	}

	public void setSendSnailMail(boolean sendSnailMail) {
		this.sendSnailMail = sendSnailMail;
	}

	public Autobill getAutobill() {
		return autobill;
	}

	public void setAutobill(Autobill autobill) {
		this.autobill = autobill;
	}
	
	@Override
    public boolean equals(Object obj) {
    	
    	if (!super.equals(obj)){
    		return false;
    	}
    	else{
    		
    		if (!(obj instanceof Recurring))
                return false;
    		
    		Recurring other = (Recurring) obj;
    		
    		if (occurrences != other.occurrences){
                return false;
    		}
    		if (!frequency.equals(other.frequency) ){
    			return false;
    		}
    		if ( stopped != other.stopped ){
    			return false;
    		}
    		if ( sendEmail != other.sendEmail){
    			return false;
    		}
    		if ( sendSnailMail != other.sendSnailMail){
    			return false;
    		}
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
    	}
    	
        return true;
    }
}
