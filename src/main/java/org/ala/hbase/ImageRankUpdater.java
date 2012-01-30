
package org.ala.hbase;

import javax.inject.Inject;
import org.ala.dao.RankingDao;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
/**
 * Updates the Image ranks after a the keyspace tc has been reloaded.
 *
 *@Deprecated Use the webservice to reload ranks.
 *
 * @author Natasha Carter
 */
@Component("imageRankUpdater")
@Deprecated
public class ImageRankUpdater {
    protected static Logger logger = Logger.getLogger(ImageRankUpdater.class);
    @Inject
    protected TaxonConceptDao taxonConceptDao;
    @Inject
    protected RankingDao rankingDao;

    public static void main(String[] args){
        ApplicationContext context = SpringUtils.getContext();
        ImageRankUpdater iru = (ImageRankUpdater) context.getBean(ImageRankUpdater.class);
        iru.updateRank();
        System.exit(0);
    }

    public void updateRank(){
        rankingDao.reloadImageRanks();
    }
}

