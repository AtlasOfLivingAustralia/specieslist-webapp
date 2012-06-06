package org.ala.web;


public class TaxonDTO {

    protected String guid;
    protected String taxonInfosourceName;
    protected String taxonInfosourceURL;
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
    protected String imageisPartOf;
    protected String imageLicence;
    protected String imageRights;
    //additional image fields to be populated
    protected String smallImageURL;
    protected String largeImageURL;
    protected String imageCreator;
    protected String imageInfosourceName;
    protected String imageInfosourceURL;

    public String getImageInfosourceName() {
        return imageInfosourceName;
    }

    public void setImageInfosourceName(String imageInfosourceName) {
        this.imageInfosourceName = imageInfosourceName;
    }

    public String getImageInfosourceURL() {
        return imageInfosourceURL;
    }

    public void setImageInfosourceURL(String imageInfosourceURL) {
        this.imageInfosourceURL = imageInfosourceURL;
    }

    public String getSmallImageURL() {
        return smallImageURL;
    }

    public void setSmallImageURL(String smallImageURL) {
        this.smallImageURL = smallImageURL;
    }

    public String getImageCreator() {
        return imageCreator;
    }

    public void setImageCreator(String imageCreator) {
        this.imageCreator = imageCreator;
    }

    public String getImageisPartOf() {
        return imageisPartOf;
    }

    public void setImageisPartOf(String imageisPartOf) {
        this.imageisPartOf = imageisPartOf;
    }

    public String getImageLicence() {
        return imageLicence;
    }

    public void setImageLicence(String imageLicence) {
        this.imageLicence = imageLicence;
    }

    public String getImageRights() {
        return imageRights;
    }

    public void setImageRights(String imageRights) {
        this.imageRights = imageRights;
    }

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

    public String getLargeImageURL() {
        return largeImageURL;
    }

    public void setLargeImageURL(String largeImageURL) {
        this.largeImageURL = largeImageURL;
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

    public String getTaxonInfosourceName() {
        return taxonInfosourceName;
    }

    public void setTaxonInfosourceName(String taxonInfosourceName) {
        this.taxonInfosourceName = taxonInfosourceName;
    }

    public String getTaxonInfosourceURL() {
        return taxonInfosourceURL;
    }

    public void setTaxonInfosourceURL(String taxonInfosourceURL) {
        this.taxonInfosourceURL = taxonInfosourceURL;
    }
}
