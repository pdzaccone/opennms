/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.siteStatusViews;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Level element containing surveillance view definitions
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "site-status-view-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiteStatusViewConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_DEFAULT_VIEW = "default";

    @XmlAttribute(name = "default-view")
    private String defaultView;

    @XmlElement(name = "views", required = true)
    private Views views;

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof SiteStatusViewConfiguration) {
            SiteStatusViewConfiguration temp = (SiteStatusViewConfiguration)obj;
            boolean equals = Objects.equals(temp.defaultView, defaultView)
                && Objects.equals(temp.views, views);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultView'.
     * 
     * @return the value of field 'DefaultView'.
     */
    public String getDefaultView() {
        return this.defaultView != null ? this.defaultView : DEFAULT_DEFAULT_VIEW;
    }

    /**
     * Returns the value of field 'views'.
     * 
     * @return the value of field 'Views'.
     */
    public Views getViews() {
        return this.views;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            defaultView, 
            views);
        return hash;
    }

    /**
     * Sets the value of field 'defaultView'.
     * 
     * @param defaultView the value of field 'defaultView'.
     */
    public void setDefaultView(final String defaultView) {
        this.defaultView = defaultView;
    }

    /**
     * Sets the value of field 'views'.
     * 
     * @param views the value of field 'views'.
     */
    public void setViews(final Views views) {
        this.views = views;
    }

}
