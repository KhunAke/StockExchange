package com.javath.mapping;
// Generated Sep 17, 2013 2:05:15 PM by Hibernate Tools 4.0.0


import java.util.Date;

/**
 * IndicatorRsiId generated by hbm2java
 */
public class IndicatorRsiId  implements java.io.Serializable {


     private String symbol;
     private Date datetime;
     private short parameter;

    public IndicatorRsiId() {
    }

    public IndicatorRsiId(String symbol, Date datetime, short parameter) {
       this.symbol = symbol;
       this.datetime = datetime;
       this.parameter = parameter;
    }
   
    public String getSymbol() {
        return this.symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public Date getDatetime() {
        return this.datetime;
    }
    
    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
    public short getParameter() {
        return this.parameter;
    }
    
    public void setParameter(short parameter) {
        this.parameter = parameter;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof IndicatorRsiId) ) return false;
		 IndicatorRsiId castOther = ( IndicatorRsiId ) other; 
         
		 return ( (this.getSymbol()==castOther.getSymbol()) || ( this.getSymbol()!=null && castOther.getSymbol()!=null && this.getSymbol().equals(castOther.getSymbol()) ) )
 && ( (this.getDatetime()==castOther.getDatetime()) || ( this.getDatetime()!=null && castOther.getDatetime()!=null && this.getDatetime().equals(castOther.getDatetime()) ) )
 && (this.getParameter()==castOther.getParameter());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getSymbol() == null ? 0 : this.getSymbol().hashCode() );
         result = 37 * result + ( getDatetime() == null ? 0 : this.getDatetime().hashCode() );
         result = 37 * result + this.getParameter();
         return result;
   }   


}

