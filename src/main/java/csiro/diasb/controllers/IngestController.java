/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csiro.diasb.controllers;

import csiro.diasb.datamodels.InfoSource;
import com.opensymphony.xwork2.ActionSupport;

/**
 *
 * @author oak021
 */
public class IngestController extends ActionSupport {

    @Override
    public String execute() throws Exception {
        //return new DefaultHttpHeaders("IngestController").disableCaching();
        //if (isInvalid(getUsername())) return INPUT;
       // if (isInvalid(getPassword())) return INPUT;
        int u = 6+78;
        int h = u*56;
        String desc  = infoSource.getDescription();
        if (desc.isEmpty()) return INPUT;

        return "SUCCESS";
    }
private InfoSource infoSource;

    public InfoSource getInfoSource() {
        return infoSource;
    }

    public void setInfoSource(InfoSource infoSource) {
        this.infoSource = infoSource;
    }

}
