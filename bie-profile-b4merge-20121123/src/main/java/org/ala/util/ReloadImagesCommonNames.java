package org.ala.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ala.dao.StoreHelper;
import org.ala.hbase.ALANamesLoader;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.Image;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * A temporary loader class used to fix the order of the common name and images.
 * 
 * In the new Ordering a "preferred" has the highest ranking...
 * 
 * @author car61w
 *
 */
@Component
public class ReloadImagesCommonNames {
    @Inject
    protected StoreHelper storeHelper;
    public static void main(String[] args) throws Exception {

        ApplicationContext context = SpringUtils.getContext();
        ReloadImagesCommonNames r = context.getBean(ReloadImagesCommonNames.class);
        r.reload();
    }
    public void reload() throws Exception{
        ColumnType[] columns = new ColumnType[]{
                ColumnType.IMAGE_COL,
                ColumnType.VERNACULAR_COL
        };
        String lastKey = "";
        String startKey="";
        Map<String, Map<String,Object>> rowMaps = storeHelper.getPageOfSubColumns("tc",columns, "", 1000);
        processMaps(rowMaps);
        while (rowMaps.size() > 0) {
            lastKey = rowMaps.keySet().toArray()[rowMaps.size() - 1].toString();
            if (lastKey.equals(startKey)) {
                break;
            }
            startKey = lastKey;
            rowMaps = storeHelper.getPageOfSubColumns("tc", columns, startKey,
                    1000);
            processMaps(rowMaps);
        }
        storeHelper.shutdown();
    }
    
    private void processMaps(Map<String, Map<String,Object>> rowMaps) throws Exception{
        for(String guid : rowMaps.keySet()){
            Map<String, Object> map = rowMaps.get(guid);
            List<CommonName> cnames = (List<CommonName>)map.get(ColumnType.VERNACULAR_COL.getColumnName());
            List<Image> images = (List<Image>)map.get(ColumnType.IMAGE_COL.getColumnName());
            if(cnames !=  null && cnames.size()>0){
                //remove all the preferred names                
                for(CommonName cname:cnames)
                    cname.setPreferred(false);
                //putLists sorts the collection before adding it to Cassandra
                storeHelper.putList("tc", "tc", ColumnType.VERNACULAR_COL.getColumnName(), guid, (List)cnames, false);
            }
            if(images != null && images.size()>0)
                storeHelper.putList("tc", "tc", ColumnType.IMAGE_COL.getColumnName(), guid, (List)images, false);
            //System.out.println(rowMaps.get(guid));
        }
    }
}
