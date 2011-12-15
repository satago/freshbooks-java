package com.freshbooks;



import java.util.TimeZone;

import com.freshbooks.model.Autobill;
import com.freshbooks.model.Callback;
import com.freshbooks.model.Callbacks;
import com.freshbooks.model.Card;
import com.freshbooks.model.Categories;
import com.freshbooks.model.Category;
import com.freshbooks.model.Client;
import com.freshbooks.model.Clients;
import com.freshbooks.model.Credit;
import com.freshbooks.model.Expense;
import com.freshbooks.model.Expenses;
import com.freshbooks.model.Invoice;
import com.freshbooks.model.InvoiceLine;
import com.freshbooks.model.InvoiceLines;
import com.freshbooks.model.Item;
import com.freshbooks.model.Items;
import com.freshbooks.model.Links;
import com.freshbooks.model.Payment;
import com.freshbooks.model.Payments;
import com.freshbooks.model.Recurring;
import com.freshbooks.model.Recurrings;
import com.freshbooks.model.Request;
import com.freshbooks.model.Response;
import com.freshbooks.model.ResponseStatus;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.MapperWrapper;


public class CustomXStream extends XStream {
  
    /**
     * The time zone in which the FreshBooks servers are. 
     */
  
//    private TimeZone freshBooksTimeZone;
  
    public CustomXStream() {
        this(TimeZone.getTimeZone("EST5EDT"));
    }
    
    /**
     * Starts a CustomXStream with a specified time zone for FreshBooks date 
     * interpretation
     * @param freshBooksTimeZone the FreshBooks time zone (default "EST5EDT")
     */
    public CustomXStream(TimeZone freshBooksTimeZone) {
        super(null, new XppDriver(new XmlFriendlyReplacer("::", "_")));
        
        registerConverter(BooleanConverter.BINARY);
        
        registerConverter(new FreshBooksDateConverter(freshBooksTimeZone));
        
        registerConverter(new SingleValueConverter() {
            @Override
            public boolean canConvert(Class type) {
                return type.equals(Long.class);
            }
            
            @Override
            public Object fromString(String str) {
                if(str.isEmpty())
                    return null;
                return Long.valueOf(str, 10);
            }
            
            @Override
            public String toString(Object obj) {
                return obj.toString();
            }
        });
        
        registerConverter(new SingleValueConverter() {
            @Override
            public boolean canConvert(Class type) {
                return type.equals(Double.class);
            }
            
            @Override
            public Object fromString(String str) {
                if(str.isEmpty())
                    return null;
                return Double.valueOf(str);
            }
            
            @Override
            public String toString(Object obj) {
                return obj.toString();
            }
        });
        
//        registerConverter(new DateConverter("yyyy-MM-dd", new String[0], true) {
//            @Override
//            public Object fromString(String str) {
//                // FreshBooks returns these bogus dates sometimes ... no idea why.
//                // We'll treat them as a "zero", but use 1970 Jan 1 instead
//                if(str.startsWith("0000-")) {
//                    return new Date(0);
//                }
//                return super.fromString(str);
//            }
//        });
        
        registerConverter(new Converter() {
    			@Override
    			public boolean canConvert(Class arg0) {
    				return arg0.equals(Credit.class);
    			}
    			
    			@Override
    			public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    				Credit c = new Credit();
    				c.setCurrency( reader.getAttribute("currency") );
    				c.setAmount( Double.parseDouble(reader.getValue()) );
    				return c;
    			}
    			
    			@Override
    			public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    				// TODO Auto-generated method stub
    				final Credit c = (Credit)value;
    				writer.addAttribute("currency", c.getCurrency());
    				writer.setValue(String.valueOf(c.getAmount()));
    			}
    		});
        
        
        registerConverter(new Converter() {

          @Override
          public boolean canConvert(Class type) {
            return type.equals(InvoiceLines.class);
          }

          @Override
          public void marshal(Object source, HierarchicalStreamWriter writer,
              MarshallingContext context) {
            //to XML
            InvoiceLines lines = (InvoiceLines) source;
            for (InvoiceLine line : lines) {
              writer.startNode("line");
              context.convertAnother(line);
              writer.endNode();
            }
            
          }

          /**
           * Take care of null lines
           */
          @Override
          public Object unmarshal(HierarchicalStreamReader reader,
              UnmarshallingContext context) {
            InvoiceLines lines = new InvoiceLines();
            while (reader.hasMoreChildren()) {
              reader.moveDown();
              
              InvoiceLine line  = (InvoiceLine) context.convertAnother(reader.getValue(), InvoiceLine.class);
              if (line != null) {
                lines.add(line);
              }
              
              reader.moveUp();
            }
            return lines;
          }
          
        });
        
        
        registerConverter(new Converter() {
          @Override
          public boolean canConvert(Class arg0) {
            return arg0.equals(InvoiceLine.class);
          }
          
          /**
           * Ignores empty lines
           */
          @Override
          public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            
            InvoiceLine line = new InvoiceLine();
            
            while (reader.hasMoreChildren()) {
              reader.moveDown();
              
              //this is SO DAMN UGLY!
              if (reader.getNodeName().equals("name")) {
                line.setName(reader.getValue());
              }
              else if (reader.getNodeName().equals("description")) {
                line.setDescription(reader.getValue());
              }
              else if (reader.getNodeName().equals("amount")) {
                line.setAmount(Double.valueOf(reader.getValue()));
              }
              else if (reader.getNodeName().equals("unit_cost")) {
                line.setUnitCost(Double.valueOf(reader.getValue()));
              }
              else if (reader.getNodeName().equals("quantity")) {
                line.setQuantity(Double.valueOf(reader.getValue()));
              }
              else if (reader.getNodeName().equals("tax1_name")) {
                line.setTax1Name(reader.getValue());
              }
              else if (reader.getNodeName().equals("tax2_name")) {
                line.setTax2Name(reader.getValue());
              }
              else if (reader.getNodeName().equals("tax1_percent")) {
                line.setTax1Percent(reader.getValue());
              }
              else if (reader.getNodeName().equals("tax2_percent")) {
                line.setTax2Percent(reader.getValue());
              }

              reader.moveUp();
            }
            
            if (line.getAmount() == 0 && line.getName().length() == 0) {
              return null;
            }
            else {
              return line;
            }
            
          }

          @Override
          public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            // TODO test this
            final InvoiceLine line = (InvoiceLine)value;
            
            writer.startNode("name");
            if (line.getName() != null) {
              writer.setValue(line.getName());
            }
            writer.endNode();
            
            writer.startNode("amount");
            if (line.getAmount() != null) {
              writer.setValue(line.getAmount().toString());  
            }
            writer.endNode();
            
            writer.startNode("unit_cost");
            writer.setValue(String.valueOf(line.getUnitCost()));
            writer.endNode();
            
            writer.startNode("quantity");
            writer.setValue(String.valueOf(line.getQuantity()));
            writer.endNode();
            
            
            writer.startNode("tax1_name");
            if (line.getTax1Name() != null){
              writer.setValue(line.getTax1Name());
            }
            writer.endNode();
            
            writer.startNode("tax2_name");
            if (line.getTax2Name() != null) {
              writer.setValue(line.getTax2Name());
            }
            writer.endNode();
            
            writer.startNode("tax1_percent");
            if (line.getTax1Percent() != null) {
              writer.setValue(line.getTax1Percent());
            }
            writer.endNode();
            
            writer.startNode("tax2_percent");
            if (line.getTax2Percent() != null) {
              writer.setValue(line.getTax2Percent());
            }
            writer.endNode();
          }
          
          
        });
        processAnnotations(new Class[] {
            Request.class,
            Response.class,
            ResponseStatus.class,
            Invoice.class,
            InvoiceLine.class,
//            InvoiceLines.class,
            Client.class,
            Clients.class,
            Item.class,
            Items.class,
            Category.class,
            Categories.class,
            Callback.class,
            Callbacks.class,
            Expense.class,
            Expenses.class,
            Recurring.class,
            Recurrings.class,
            Payment.class,
            Payments.class,
            Item.class,
            Items.class,
            Links.class,
            Autobill.class,
            Exception.class,
            Card.class,
            Credit.class
        });
    }
    
    /**
     * Allow and ignore unexpected xml tags
     */  
    protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
            public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                try {
                	//check wrapped mapper first so that omitField will still work
                	if ( !super.shouldSerializeMember(definedIn, fieldName) ) {
                		return false;
                	}
                    return definedIn != Object.class || realClass(fieldName) != null;
                } catch(CannotResolveClassException cnrce) {
                    return false;
                }
            }
        };
    }
    
    
}
