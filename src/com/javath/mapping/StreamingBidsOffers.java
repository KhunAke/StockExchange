package com.javath.mapping;
// Generated Aug 4, 2013 4:58:19 PM by Hibernate Tools 4.0.0



/**
 * StreamingBidsOffers generated by hbm2java
 */
public class StreamingBidsOffers  implements java.io.Serializable {


     private StreamingBidsOffersId id;
     private Long bidVolume;
     private Long offerVolume;

    public StreamingBidsOffers() {
    }

	
    public StreamingBidsOffers(StreamingBidsOffersId id) {
        this.id = id;
    }
    public StreamingBidsOffers(StreamingBidsOffersId id, Long bidVolume, Long offerVolume) {
       this.id = id;
       this.bidVolume = bidVolume;
       this.offerVolume = offerVolume;
    }
   
    public StreamingBidsOffersId getId() {
        return this.id;
    }
    
    public void setId(StreamingBidsOffersId id) {
        this.id = id;
    }
    public Long getBidVolume() {
        return this.bidVolume;
    }
    
    public void setBidVolume(Long bidVolume) {
        this.bidVolume = bidVolume;
    }
    public Long getOfferVolume() {
        return this.offerVolume;
    }
    
    public void setOfferVolume(Long offerVolume) {
        this.offerVolume = offerVolume;
    }




}

