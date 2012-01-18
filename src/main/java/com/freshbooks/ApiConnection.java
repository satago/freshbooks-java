package com.freshbooks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freshbooks.model.Callback;
import com.freshbooks.model.Callbacks;
import com.freshbooks.model.Categories;
import com.freshbooks.model.Category;
import com.freshbooks.model.Client;
import com.freshbooks.model.Clients;
import com.freshbooks.model.Expense;
import com.freshbooks.model.Expenses;
import com.freshbooks.model.Invoice;
import com.freshbooks.model.Invoices;
import com.freshbooks.model.Item;
import com.freshbooks.model.Items;
import com.freshbooks.model.PagedResponseContent;
import com.freshbooks.model.Payment;
import com.freshbooks.model.Payments;
import com.freshbooks.model.Recurring;
import com.freshbooks.model.Recurrings;
import com.freshbooks.model.Request;
import com.freshbooks.model.RequestMethod;
import com.freshbooks.model.Response;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

public class ApiConnection {
    static final Logger logger = LoggerFactory.getLogger(ApiConnection.class);

    
    String apiHost;
    String apiScheme;
    String apiEntry;
    
    String apiKey;
    String userAgent;
    transient AbstractHttpClient httpclient;
    transient HttpContext localcontext;
    transient HttpHost targetHost;
    
    boolean debug;
    
    /**
     * The time zone in which the FreshBooks servers are. 
     */
    private TimeZone freshBooksTimeZone = TimeZone.getTimeZone("EST5EDT");
    
    public ApiConnection(String apiHost, String key, String userAgent) {
      this(apiHost, key,  userAgent, "https", "/api/2.1/xml-in", "EST5EDT");
    }
    
    public ApiConnection (String apiHost, String key, String userAgent, String apiScheme, String apiEntry, String freshBooksTimeZoneId) {
      
        this.apiHost = apiHost;
        this.apiKey = key;
        this.userAgent = userAgent;
        this.apiEntry  = apiEntry;
        this.apiScheme = apiScheme;
        
        if (freshBooksTimeZoneId != null) {
          try {
            this.freshBooksTimeZone =  TimeZone.getTimeZone(freshBooksTimeZoneId);
          }
          catch( Exception e) {
            logger.error("Error setting custom timezone, using default [" + freshBooksTimeZone.getID() + "]");
          }
        }
        
    }

    private AbstractHttpClient getClient() {
      
        if(httpclient == null) {
          
          targetHost = new HttpHost(apiHost, -1, apiScheme);
          
          httpclient = new DefaultHttpClient();
          
          httpclient.getCredentialsProvider().setCredentials(
              new AuthScope(targetHost.getHostName(), targetHost.getPort()),
              new UsernamePasswordCredentials(apiKey, ""));

          // Create AuthCache instance
          AuthCache authCache = new BasicAuthCache();
          // Generate BASIC scheme object and add it to the local
          // auth cache
          BasicScheme basicAuth = new BasicScheme();
          authCache.put(targetHost, basicAuth);

          // Add AuthCache to the execution context
          localcontext = new BasicHttpContext();
          localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

          httpclient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, (int) (90 * DateUtils.MILLIS_PER_SECOND));
          httpclient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, (int) (90 * DateUtils.MILLIS_PER_SECOND));
            
        }
        
        return httpclient;
    }
    
    protected void checkRequestAndOmitFields(Request request, XStream xs) {
    	//if this is an "invoice.update" method, and the lines property is empty
        //omit converting the lines field otherwise the call will erase all the
        //lines on our invoice
        if ( request.getMethod().equals("invoice.update") ) {
        	if ( request.getInvoice().getLines() == null || request.getInvoice().getLines().size() == 0 ) {
        		xs.omitField(Invoice.class, "lines");
        	}
        }
        else if ( request.getMethod().equals("recurring.update") ) {
        	if ( request.getRecurring().getLines() == null || request.getRecurring().getLines().size() == 0 ) {
        		xs.omitField(Recurring.class, "lines");
        	}
        }
    }

    /**
     * Send a request to the FreshBooks API and return the response object.
     * @param url
     * @param postObject
     * @return
     * @throws IOException 
     * @throws Error
     */
    protected Response performRequest(Request request) throws ApiException, IOException {
        try {
            XStream xs = new CustomXStream(this.freshBooksTimeZone);
            
            checkRequestAndOmitFields(request, xs);
            
            String paramString = xs.toXML(request);

            StringEntity dataEntity = new StringEntity(paramString, HTTP.UTF_8);
            HttpPost httpPost = new HttpPost(apiEntry);
            httpPost.setEntity(dataEntity);
            httpPost.setHeader("Content-Type","application/xml;charset=UTF-8");
            
            try {
                
                HttpResponse httpResponse = getClient().execute(targetHost, httpPost, localcontext);
                HttpEntity entity = httpResponse.getEntity();
                InputStream is = entity.getContent();
                
                if(debug) {
                  byte[] bytes = IOUtils.toByteArray(is);
                    logger.info("POST "+this.apiScheme + this.apiHost
                        + this.apiEntry+":\n"+paramString
                        +"\nYields "+entity.getContentLength()
                        +" bytes of UTF-8 data:\n"+
                        new String(bytes,"UTF-8"));
                }
                try {
                	
                  Response response = (Response) xs.fromXML(EntityUtils.toString(entity));
                  
                  // TODO Throw an error if we got one
                  if(response.isFail()) {
                      throw new ApiException(response.getError());
                  }
                  return response;
                  
                } catch(CannotResolveClassException cnrce) {
                    throw new ApiException("Error while parsing response from FreshBooks: "+cnrce.toString()+"; response body: "+is);
                }
                finally {
                  is.close();
                }
            } finally {
//                EntityUtils.consume(dataEntity);
//                httpPost.releaseConnection();
            }
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
    }
    
    /**
     * Create an invoice using the given information and return its id.
     * @throws ApiException If an error is returned from FreshBooks
     * @throws IOException If there is a communications error with the FreshBooks API server
     */
    public Long createInvoice(Invoice invoice) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.INVOICE_CREATE, invoice)).getInvoiceId();
    }

    public Long createItem(Item item) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.ITEM_CREATE, item)).getItemId();
    }
    
    public Long createClient(Client client) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.CLIENT_CREATE, client)).getClientId();
    }
    
    public Long createCallback(Callback callback) throws ApiException, IOException {
      return performRequest(new Request(RequestMethod.CALLBACK_CREATE, callback)).getCallbackId();
    }
    
    public Long createPayment(Payment payment) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.PAYMENT_CREATE, payment)).getPaymentId();
    }
    
    public Long createRecurring(Recurring recurring) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.RECURRING_CREATE, recurring)).getRecurringId();
    }

    public String getKey() {
        return apiKey;
    }

    public void setKey(String key) {
        this.apiKey = key;
    }
    
    /**
     * Iterate over the invoices matching the given filters, or all invoices.
     * 
     * Note that the Freshbooks API only returns summaries of the invoice, not the full details
     * of the invoice.
     */
    public Iterable<Invoice> listInvoices(final Integer perPage, final Date dateFrom, final Date dateTo, final Long clientId, final String status) {
        return new Iterable<Invoice>() {
            @Override
            public Iterator<Invoice> iterator() {
                try {
                    return new InvoicesIterator(perPage, dateFrom, dateTo, clientId, status, null, null);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    
    
    
    /**
     * Iterate over the invoices matching the given filters, or all invoices.
     * 
     * Note that the Freshbooks API only returns summaries of the invoice, not the full details
     * of the invoice.
     */
    public Iterable<Invoice> listInvoices(final Integer perPage, final Date dateFrom, final Date dateTo, final Long clientId, final String status, final Long recurringId) {
        return new Iterable<Invoice>() {
            @Override
            public Iterator<Invoice> iterator() {
                try {
                    return new InvoicesIterator(perPage, dateFrom, dateTo, clientId, status, recurringId, null);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    
    
    /**
     * Iterate over the invoices matching the given filters, or all invoices.
     * 
     * Note that the Freshbooks API only returns summaries of the invoice, not the full details
     * of the invoice.
     */
    public Iterable<Invoice> listInvoices(final Integer perPage, final Date dateFrom, final Date dateTo, final Long clientId, final String status, final Long recurringId, final String invoiceNumber) {
        return new Iterable<Invoice>() {
            @Override
            public Iterator<Invoice> iterator() {
                try {
                    return new InvoicesIterator(perPage, dateFrom, dateTo, clientId, status, recurringId, invoiceNumber);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    
    
    
    /**
     * Iterate over the payments matching the given filters, or all invoices.
     */
    public Iterable<Payment> listPayments(final Integer perPage, final Date dateFrom, final Date dateTo, final Long clientId) {
        return new Iterable<Payment>() {
            @Override
            public Iterator<Payment> iterator() {
                try {
                    return new PaymentsIterator(perPage, dateFrom, dateTo, clientId);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }

    /**
     * Iterate over the expenses matching the given filters, or all invoices.
     */
    public Iterable<Expense> listExpenses(final Integer perPage, final Date dateFrom, final Date dateTo, final Long clientId, final Long categoryId, final Long projectId) {
        return new Iterable<Expense>() {
            @Override
            public Iterator<Expense> iterator() {
                try {
                    return new ExpensesIterator(perPage, dateFrom, dateTo, clientId, categoryId, projectId);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    
    /**
     * Iterate over the clients matching the given filters, or all invoices.
     */
    public Iterable<Client> listClients(final Integer perPage, final String username, final String email) {
        return new Iterable<Client>() {
            @Override
            public Iterator<Client> iterator() {
                try {
                    return new ClientsIterator(perPage, username, email);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    
    public Iterable<Item> listItems(final Integer perPage) {
        return new Iterable<Item>() {
            @Override
            public Iterator<Item> iterator() {
                try {
                    return new ItemsIterator(perPage);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    abstract class RecordsIterator<T> implements Iterator<T> {
        PagedResponseContent<T> current;
        Iterator<T> currentIterator;
        final protected Integer perPage;
        final protected Date dateFrom;
        final protected Date dateTo;
        final protected Long clientId;
        final protected String status;
        final protected String username;
        final protected String email;
        final protected Long categoryId;
        final protected Long projectId;
        final protected Long recurringId;
        final protected String number;
        
        public RecordsIterator(Integer perPage, Date dateFrom, Date dateTo, Long clientId, String status, String username, String email, Long categoryId, Long projectId, Long recurringId, String invoiceNumber) throws ApiException, IOException {
            this.perPage = perPage;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
            this.clientId = clientId;
            this.categoryId = categoryId;
            this.projectId = projectId;
            this.status = status;
            this.username = username;
            this.email = email;
            this.recurringId = recurringId;
            this.number = invoiceNumber;
            this.current = list(1);
            this.currentIterator = current.iterator();
        }

        protected abstract PagedResponseContent<T> list(int page) throws ApiException, IOException;

        @Override
        public boolean hasNext() {
            return currentIterator.hasNext() || current.getPage() < current.getPages();
        }
        
        @Override
        public T next() {
            if(!currentIterator.hasNext()) {
                if(current.getPage() >= current.getPages())
                    throw new NoSuchElementException();
                try {
                    current = list(current.getPage()+1);
                } catch (ApiException e) {
                    throw new NoSuchElementException(e.getLocalizedMessage());
                } catch (IOException e) {
                    throw new NoSuchElementException(e.getLocalizedMessage());
                }
                currentIterator = current.iterator();
            }
            return currentIterator.next();
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    
    class InvoicesIterator extends RecordsIterator<Invoice> {

//        private InvoicesIterator(Integer perPage, Date dateFrom, Date dateTo, Long clientId, String status) throws ApiException, IOException {
//            super(perPage, dateFrom, dateTo, clientId, status, null, null, null, null, null);
//        }
        
        private InvoicesIterator(Integer perPage, Date dateFrom, Date dateTo, Long clientId, String status, Long recurringId, String invoiceNumber) throws ApiException, IOException {
            super(perPage, dateFrom, dateTo, clientId, status, null, null, null, null, recurringId, invoiceNumber);
        }

        @Override
        protected PagedResponseContent<Invoice> list(int page) throws ApiException, IOException {
            return listInvoices(page, perPage, dateFrom, dateTo, clientId, status, recurringId);
        }
    }
    
    class PaymentsIterator extends RecordsIterator<Payment> {

        private PaymentsIterator(Integer perPage, Date dateFrom, Date dateTo, Long clientId) throws ApiException, IOException {
            super(perPage, dateFrom, dateTo, clientId, null, null, null, null, null, null, null);
        }
        
        @Override
        protected PagedResponseContent<Payment> list(int page) throws ApiException, IOException {
            return listPayments(page, perPage, dateFrom, dateTo, clientId);
        }
    }
    class ExpensesIterator extends RecordsIterator<Expense> {

        private ExpensesIterator(Integer perPage, Date dateFrom, Date dateTo, Long clientId, Long categoryId, Long projectId) throws ApiException, IOException {
            super(perPage, dateFrom, dateTo, clientId, null, null, null, categoryId, projectId, null, null);
        }
        
        @Override
        protected PagedResponseContent<Expense> list(int page) throws ApiException, IOException {
            return listExpenses(page, perPage, dateFrom, dateTo, clientId, categoryId, projectId);
        }
    }
    class ClientsIterator extends RecordsIterator<Client> {

        private ClientsIterator(Integer perPage, String username, String email) throws ApiException, IOException {
            super(perPage, null, null, null, null, username, email, null, null, null, null);
        }
        
        @Override
        protected PagedResponseContent<Client> list(int page) throws ApiException, IOException {
            return listClients(page, perPage, username, email);
        }
    }
    
    class CallbacksIterator extends RecordsIterator<Callback> {

        private CallbacksIterator(Integer perPage) throws ApiException, IOException {
            super(perPage, null, null, null, null, null, null, null, null, null, null);
        }
        
        @Override
        protected PagedResponseContent<Callback> list(int page) throws ApiException, IOException {
            return listCallbacks(page, perPage);
        }
    }

    class ItemsIterator extends RecordsIterator<Item> {

        private ItemsIterator(Integer perPage) throws ApiException, IOException {
            super(perPage, null, null, null, null, null, null, null, null, null, null);
        }
        
        @Override
        protected PagedResponseContent<Item> list(int page) throws ApiException, IOException {
            return listItems(page, perPage);
        }
    }
    
    class RecurringsIterator extends RecordsIterator<Recurring> {

        private RecurringsIterator(Integer perPage, Long clientId) throws ApiException, IOException {
            super(perPage, null, null, clientId, null, null, null, null, null, null, null);
        }
        
        @Override
        protected PagedResponseContent<Recurring> list(int page) throws ApiException, IOException {
            return listRecurrings(page, perPage, clientId);
        }
    }
    
    /**
     * Return a list of invoices.
     * 
     * @param dateFrom If non-null, return only invoices after that day
     * @param dateTo If non-null, return only invoices before that day
     * @param clientId If non-null, return only invoices relevant to a particular client
     */
    public Invoices listInvoices(int page, Integer perPage, Date dateFrom, Date dateTo, Long clientId, String status, Long recurringId) throws ApiException, IOException {
        Request request = new Request(RequestMethod.INVOICE_LIST);
        request.setPage(page);
        request.setPerPage(perPage);
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        request.setClientId(clientId);
        request.setStatus(status);
        request.setRecurringId(recurringId);
        return performRequest(request).getInvoices();
    }
    
    
    /**
     * Return a list of invoices.
     * 
     * @param dateFrom If non-null, return only invoices after that day
     * @param dateTo If non-null, return only invoices before that day
     * @param clientId If non-null, return only invoices relevant to a particular client
     */
//    public Invoices listInvoices(Long clientId, String invoiceNumber) throws ApiException, IOException {
//        Request request = new Request(RequestMethod.INVOICE_LIST);
//        request.setClientId(clientId);
//        request.setInvoiceNumber(invoiceNumber);
//        return performRequest(request).getInvoices();
//    }
    
    
    
    /**
     * Get a list of payments.
     * 
     * @param dateFrom If non-null, return only payments after that day
     * @param dateTo If non-null, return only payments before that day
     * @param clientId If non-null, return only payments relevant to a particular client
     */
    public Payments listPayments(int page, Integer perPage, Date dateFrom, Date dateTo, Long clientId) throws ApiException, IOException {
        Request request = new Request(RequestMethod.PAYMENT_LIST);
        request.setPage(page);
        request.setPerPage(perPage);
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        request.setClientId(clientId);
        return performRequest(request).getPayments();
    }
    
    /**
     * Get a list of expenses.
     * 
     * @param dateFrom If non-null, return only expenses after that day
     * @param dateTo If non-null, return only expenses before that day
     * @param clientId If non-null, return only expenses relevant to a particular client
     */
    public Expenses listExpenses(int page, Integer perPage, Date dateFrom, Date dateTo, Long clientId, Long categoryId, Long projectId) throws ApiException, IOException {
        Request request = new Request(RequestMethod.EXPENSE_LIST);
        request.setPage(page);
        request.setPerPage(perPage);
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        request.setClientId(clientId);
        request.setProjectId(projectId);
        request.setCategoryId(categoryId);
        return performRequest(request).getExpenses();
    }
    
    
    /**
     * Get a list of clients.  The items returned are client summaries which do
     * not include the full address information.
     * 
     * @param username If non-null, include only clients with a matching username
     * @param email If non-null, include only clients with a matching email address
     * @return
     * @throws ApiException
     * @throws IOException
     */
    public Clients listClients(int page, Integer perPage, String username, String email) throws ApiException, IOException {
        Request request = new Request(RequestMethod.CLIENT_LIST);
        request.setPage(page);
        request.setPerPage(perPage);
        request.setUsername(username);
        request.setEmail(email);
        return performRequest(request).getClients();
    }
    
    /**
     * List recurring profiles for a client
     * 
     * @param page
     * @param perPage
     * @param clientId
     * @return
     * @throws ApiException
     * @throws IOException
     */
    public Recurrings listRecurrings(int page, Integer perPage, Long clientId) throws ApiException, IOException {
        Request request = new Request(RequestMethod.RECURRING_LIST);
        request.setPage(page);
        request.setPerPage(perPage);
        request.setClientId(clientId);
        return performRequest(request).getRecurrings();
    }

    /**
     * Get all the categories defined
     */
    public Categories listCategories() throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.CATEGORY_LIST)).getCategories();
    }
    
    /**
     * Get all the categories defined
     */
    public Items listItems(int page, Integer perPage) throws ApiException, IOException {
        final Request request = new Request(RequestMethod.ITEM_LIST);
        request.setPerPage(perPage);
        request.setPage(page);
        return performRequest(request).getItems();
    }
    
    /**
     * Get a list of callbacks
     * @throws IOException
     */
    public Callbacks listCallbacks(int page, Integer perPage) throws ApiException, IOException {
        Request request = new Request(RequestMethod.CALLBACK_LIST);
        request.setPage(page);
        request.setPerPage(perPage);
        return performRequest(request).getCallbacks();
    }
    
    /**
     * Iterate over the callbacks
     */
    public Iterable<Callback> listCallbacks(final Integer perPage) {
        return new Iterable<Callback>() {
            @Override
            public Iterator<Callback> iterator() {
                try {
                    return new CallbacksIterator(perPage);
                } catch (ApiException e) {
                    throw new Error(e);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        };
    }
    
//    public Recurrings listRecurrings() throws ApiException, IOException {
//        return performRequest(new Request(RequestMethod.RECURRING_LIST)).getRecurrings();
//    }
    
    public Iterable<Recurring> listRecurrings(final Integer perPage, final Long clientId) {
      return new Iterable<Recurring>() {
          @Override
          public Iterator<Recurring> iterator() {
              try {
                  return new RecurringsIterator(perPage, clientId);
              } catch (ApiException e) {
                  throw new Error(e);
              } catch (IOException e) {
                  throw new Error(e);
              }
          }
      };
    }
    
    
    /**
     * Fetch the details of a client.
     */
    public Client getClient(Long id) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.CLIENT_GET, id)).getClient();
    }
    
    /**
     * Fetch the details of an item.
     */
    public Item getItem(Long id) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.ITEM_GET, id)).getItem();
    }
    
    /**
     * Fetch the details of an invoice
     */
    public Invoice getInvoice(Long id) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.INVOICE_GET, id)).getInvoice();
    }
    
    /**
     * Fetch the details of an expense
     */
    public Expense getExpense(Long id) throws ApiException, IOException { 
        return performRequest(new Request(RequestMethod.EXPENSE_GET, id)).getExpense();
    }
    
    /**
     * Get category details by id
     */
    public Category getCategory(Long id) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.CATEGORY_GET, id)).getCategory();
    }
    
    /**
     * Get payment details by id
     */
    public Payment getPayment(Long paymentId) throws ApiException, IOException {
		 return performRequest(new Request(RequestMethod.PAYMENT_GET, paymentId)).getPayment();
	}
    
    public Recurring getRecurring(Long id) throws ApiException, IOException {
        return performRequest(new Request(RequestMethod.RECURRING_GET, id)).getRecurring();
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * Update item details
     */
    public void updateItem(Item item) throws ApiException, IOException {
        performRequest(new Request(RequestMethod.ITEM_UPDATE, item));
    }
    
    /**
     * Update client details
     */
	public void updateClient(Client client) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.CLIENT_UPDATE, client));
	}
	
	public void updateRecurring(Recurring recurring) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.RECURRING_UPDATE, recurring));
	}
	
	/**
     * Update invoice details
     */
	public void updateInvoice(Invoice invoice) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.INVOICE_UPDATE, invoice));
	}
	
	/**
	 * Update a payment
	 */
	public void updatePayment(Payment payment) throws ApiException, IOException {
        performRequest(new Request(RequestMethod.PAYMENT_UPDATE, payment));
    }
	
	/**
	 * Verify a callback, the Callback object should only have the callbackId and verifier property set
	 */
	public void verifyCallback(Callback callback) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.CALLBACK_VERIFY, callback));
	}
	
	/**
	 * Resend's the callback's verifier token
	 */
	public void resendToken(Long callbackId) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.CALLBACK_RESEND_TOKEN, callbackId));
	}
	
	/**
	 * Deletes a callback
	 */
	public void deleteCallback(Long callbackId) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.CALLBACK_DELETE, callbackId));
	}
	
	/**
	 * Deletes a payment
	 */
	public void deletePayment(Long paymentId) throws ApiException, IOException {
		 performRequest(new Request(RequestMethod.PAYMENT_DELETE, paymentId));
	}
	
	/**
	 * Deletes a client
	 */
	public void deleteClient(Long clientId) throws ApiException, IOException {
		 performRequest(new Request(RequestMethod.CLIENT_DELETE, clientId));
	}
	
	/**
	 * Deletes an invoice
	 */
	public void deleteInvoice(Long invoiceId) throws ApiException, IOException {
		 performRequest(new Request(RequestMethod.INVOICE_DELETE, invoiceId));
	}
	
	/**
	 * Deletes an item
	 */
	public void deleteItem(Long itemId) throws ApiException, IOException {
		 performRequest(new Request(RequestMethod.ITEM_DELETE, itemId));
	}
	
	/**
	 * Sends an invoice by email
	 */
	public void sendInvoiceByEmail(Long invoiceId) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.INVOICE_SEND_BY_EMAIL, invoiceId));
	}
	
	/**
	 * Sends an invoice by email with custom subject and message 
	 */
	public void sendInvoiceByEmail(Long invoiceId, String emailSubject, String emailMessage) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.INVOICE_SEND_BY_EMAIL, invoiceId, emailSubject, emailMessage));
	}

	
	/**
	 * Sends an invoice by email
	 */
	public void sendInvoiceBySnailMail(Long invoiceId) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.INVOICE_SEND_BY_SNAIL_MAIL, invoiceId));
	}
	
	public void deleteRecurring(Long recurringId) throws ApiException, IOException {
		performRequest(new Request(RequestMethod.RECURRING_DELETE, recurringId));
	}
	
//	public byte[] getInvoicePDF(Long id) throws ApiException, IOException {
//        Invoice inv = performRequest(new Request(RequestMethod.INVOICE_GET, id)).getInvoice();
//        String clientViewUrl = inv.getLinks().getClientView();
//        byte[] pdfBytes = PDFGrabber.getPDF(id, clientViewUrl);
//        return pdfBytes;
//  }
	
	
//	public static void main(String[] args) throws FileNotFoundException {
//	  XStream xs = new CustomXStream();
//	  String fileName = "/home/caco/logs/output2.xml";
//	  
//	  FileInputStream file = new FileInputStream(new File(fileName));
//	  
//	  
//	  Response response = (Response) xs.fromXML(file);
//	  
//	  System.out.println("ok");
//  }
	
	
	
}
