package com.javath.mapping;
// Generated Sep 17, 2013 2:05:15 PM by Hibernate Tools 4.0.0


import java.util.Date;

/**
 * SettradeIndexId generated by hbm2java
 */
public class SettradeIndexId  implements java.io.Serializable {


     private String symbol;
     private Date date;

    public SettradeIndexId() {
    }

    public SettradeIndexId(String symbol, Date date) {
       this.symbol = symbol;
       this.date = date;
    }
   
    public String getSymbol() {
        return this.symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public Date getDate() {
        return this.date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof SettradeIndexId) ) return false;
		 SettradeIndexId castOther = ( SettradeIndexId ) other; 
         
		 return ( (this.getSymbol()==castOther.getSymbol()) || ( this.getSymbol()!=null && castOther.getSymbol()!=null && this.getSymbol().equals(castOther.getSymbol()) ) )
 && ( (this.getDate()==castOther.getDate()) || ( this.getDate()!=null && castOther.getDate()!=null && this.getDate().equals(castOther.getDate()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getSymbol() == null ? 0 : this.getSymbol().hashCode() );
         result = 37 * result + ( getDate() == null ? 0 : this.getDate().hashCode() );
         return result;
   }   


}


