package org.ala.web;


public class TaxonDTO {

    protected String guid;
    protected String scientificName;
    protected String scientificNameAuthorship;
    protected String year;
    protected String rank;
    protected Integer rankID;
    protected String author;
    protected String family;
    protected String kingdom;
    protected String commonName;
    protected String commonNameGuid;
    protected String imageURL;
    protected String thumbnail;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getRankID() {
        return rankID;
    }

    public void setRankID(Integer rankID) {
        this.rankID = rankID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getScientificNameAuthorship() {
        return scientificNameAuthorship;
    }

    public void setScientificNameAuthorship(String scientificNameAuthorship) {
        this.scientificNameAuthorship = scientificNameAuthorship;
    }

    public String getCommonNameGuid() {
        return commonNameGuid;
    }

    public void setCommonNameGuid(String commonNameGuid) {
        this.commonNameGuid = commonNameGuid;
    }
}
