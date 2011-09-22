package org.ala.web.admin.dao;

public interface CollectionDao {
	public boolean reloadCollections()  throws Exception;
	public boolean reloadInstitutions()  throws Exception;
	public boolean reloadDataProviders()  throws Exception;
	public boolean reloadDataResources()  throws Exception;
}
