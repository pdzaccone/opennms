/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.topo.SearchCriteria;

@XmlRootElement(name="searchQuery")
@XmlAccessorType(value= XmlAccessType.FIELD)
public class SearchQueryHistory {

	private static final String delimiter = "|";

	@XmlAttribute
	private String id;
	@XmlAttribute
	private String namespace;
	@XmlAttribute
	private String queryText;

//	public static <T extends Criteria & SearchCriteria> SearchQueryHistory generateFrom(T obj)
//	{
//		return new SearchQueryHistory(obj.getNamespace(), obj.getSearchString(), obj.getSearchString());
//	}

	// Constructor for JAXB
	public SearchQueryHistory() {

	}

	public SearchQueryHistory(String namespace, String id, String query) {
		this.namespace = namespace;
		this.queryText = query;
		this.id = id;
	}

	public SearchQueryHistory(SearchCriteria criteria) {
		this(criteria.getNamespace(), criteria.getId(), criteria.getSearchString());
	}

	public String getNamespace() {
		return namespace;
	}

	public String getQueryText() {
		return queryText;
	}

	public String getId() {
		return id;
	}
}
