package com.freshbooks.model;

import java.io.Serializable;
import java.util.Date;
//import java.util.ArrayList;
//import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("invoice")
public class Invoice extends AbstractInvoiceTemplate implements Serializable {
	
    private static final long serialVersionUID = -6382862130184354619L;

    
    @XStreamAlias("invoice_id")
    Long id;
    
    String number;

    String status;
    
    String url;
    @XStreamAlias("auth_url")
    String authUrl;
    @XStreamAlias("recurring_id")
    String recurringId; 

    @XStreamAlias("amount_outstanding")
    Double amountOutstanding;
    
    @XStreamAlias("paid")
    Double amountPaid;
    

    Date updated;
    
    
    Links links;
    
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getAuthUrl() {
        return authUrl;
    }
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }
    public String getRecurringId() { 
        return recurringId;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Double getAmountOutstanding() {
        return amountOutstanding;
    }
    public void setAmountOutstanding(Double amountOutstanding) {
        this.amountOutstanding = amountOutstanding;
    }
    public Double getAmountPaid() {
        return amountPaid;
    }
    public void setAmountPaid(Double paid) {
        this.amountPaid = paid;
    }
    public Links getLinks() {
        return links;
    }
    public void setLinks(Links links) {
        this.links = links;
    }
    
    public Date getUpdated() {
      return updated;
  }



    @Override
    public boolean equals(Object obj) {
    	
    	if (!super.equals(obj)){
    		return false;
    	}
    	else{	
    		
    		if (!(obj instanceof Invoice))
                return false;
    		
			Invoice other = (Invoice) obj;
    		
    		if (id != other.id ){
                return false;
            } 
    		if (amountOutstanding == null) {
                if (other.amountOutstanding != null)
                    return false;
            } else if (!amountOutstanding.equals(other.amountOutstanding))
                return false;
            if (amountPaid == null) {
                if (other.amountPaid != null)
                    return false;
            } else if (!amountPaid.equals(other.amountPaid))
                return false;
            if (authUrl == null) {
                if (other.authUrl != null)
                    return false;
            } else if (!authUrl.equals(other.authUrl))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (updated == null) {
              if (other.updated != null)
                return false;
            } else if (!updated.equals(other.updated))
              return false;
            if (links == null) {
                if (other.links != null)
                    return false;
            } else if (!links.equals(other.links))
                return false;
            if (number == null) {
                if (other.number != null)
                    return false;
            } else if (!number.equals(other.number))
                return false;
            if (recurringId == null) {
                if (other.recurringId != null)
                    return false;
            } else if (!recurringId.equals(other.recurringId))
                return false;
            if (status == null) {
                if (other.status != null)
                    return false;
            } else if (!status.equals(other.status))
                return false;
            if (url == null) {
                if (other.url != null)
                    return false;
            } else if (!url.equals(other.url))
                return false;
    	}
    	
        return true;
    }
    
    
    
}
