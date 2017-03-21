/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.support.SavedHistory;
import org.opennms.features.topology.api.support.SearchQueryHistory;
import org.opennms.features.topology.api.support.ServiceLocator;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.CategorySearchProvider;
import org.opennms.features.topology.app.internal.DefaultLayout;
import org.opennms.features.topology.app.internal.jung.CircleLayoutAlgorithm;
import org.opennms.features.topology.app.internal.operations.AutoRefreshToggleOperation;
import org.opennms.features.topology.app.internal.operations.CircleLayoutOperation;
import org.opennms.features.topology.app.internal.operations.FRLayoutOperation;
import org.opennms.features.topology.app.internal.operations.ISOMLayoutOperation;
import org.opennms.features.topology.app.internal.operations.KKLayoutOperation;
import org.opennms.features.topology.app.internal.operations.ManualLayoutOperation;
import org.opennms.features.topology.app.internal.operations.RealUltimateLayoutOperation;
import org.opennms.features.topology.app.internal.operations.SimpleLayoutOperation;
import org.opennms.features.topology.app.internal.operations.SpringLayoutOperation;
import org.osgi.framework.BundleContext;

public class BundleContextHistoryManagerTest  {

    private static final String searchQuery = "search query 1";

    private static class TestVertex extends AbstractVertex {

        public TestVertex(String id) {
            super("test", id, id);
        }
    }

    private static class CustomSearchCriteria extends Criteria implements SearchCriteria {

        private final String searchString;

        private CustomSearchCriteria(String searchString) {
            this.searchString = Objects.requireNonNull(searchString);
        }

        @Override
        public String getSearchString() {
            return searchString;
        }

        @Override
        public String getId() {
            return searchQuery;
        }

        @Override
        public ElementType getType() {
            return ElementType.GRAPH;
        }

        @Override
        public String getNamespace() {
            return "nodes";
        }

        @Override
        public int hashCode() {
            return Objects.hash(searchString);
        }

        // TODO RS implement me
        @Override
        public boolean equals(Object obj) {
            return true;
        }
    }

    private static final String DATA_FILE_NAME = BundleContextHistoryManager.DATA_FILE_NAME;

    private BundleContextHistoryManager historyManager;
    private GraphContainer graphContainerMock;
    private Graph graphMock;
    private BundleContext bundleContextMock;
    private ServiceLocator serviceLocatorMock;
    //private CategorySearchProvider searchProviderMock;

    private List<SearchProvider> availableProviders;
    private List<Vertex> displayableVertices;
    private Layout selectedLayout;

    @Before
    public void setUp() {
        if (new File(DATA_FILE_NAME).exists()) {
            new File(DATA_FILE_NAME).delete();
        }

        serviceLocatorMock = EasyMock.createNiceMock(ServiceLocator.class);
    	bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
    	graphContainerMock = EasyMock.createNiceMock(GraphContainer.class);
        graphMock = EasyMock.createNiceMock(Graph.class);

        availableProviders = new ArrayList<>();
        displayableVertices = new ArrayList<>();
        selectedLayout = new DefaultLayout();

        historyManager = new BundleContextHistoryManager(bundleContextMock, serviceLocatorMock);
        historyManager.onBind(new CircleLayoutOperation());
        historyManager.onBind(new ManualLayoutOperation(null));
        historyManager.onBind(new FRLayoutOperation());
        historyManager.onBind(new SimpleLayoutOperation());
        historyManager.onBind(new ISOMLayoutOperation());
        historyManager.onBind(new SpringLayoutOperation());
        historyManager.onBind(new RealUltimateLayoutOperation());
        historyManager.onBind(new KKLayoutOperation());
        historyManager.onBind(new AutoRefreshToggleOperation());

        setBehaviour(bundleContextMock);
        setBehaviour(graphContainerMock);
        setBehaviour(graphMock);
        setBehaviour(serviceLocatorMock);

        EasyMock.replay(graphContainerMock);
        EasyMock.replay(graphMock);
        EasyMock.replay(bundleContextMock);
        EasyMock.replay(serviceLocatorMock);
    }

    @After
    public void tearDown() {
        if (new File(DATA_FILE_NAME).exists()) {
            new File(DATA_FILE_NAME).delete();
        }
    }

    @Test
    public void testHistoryManager() throws IOException {
        String admin = "admin";
        String user1 = "user1";
        displayableVertices.add(new TestVertex("100"));
        
        // simple save
        String historyHash = historyManager.saveOrUpdateHistory(admin, graphContainerMock);
        SavedHistory savedHistory = historyManager.getHistoryByFragment(historyHash);
        Properties properties = loadProperties();
        Assert.assertNotNull(savedHistory);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash));
        Assert.assertNotNull(properties.get(admin));
        properties = null;  // no access to this field after this line!

        // save again (nothing should change)
        String historyHash2 = historyManager.saveOrUpdateHistory(admin, graphContainerMock);
        SavedHistory savedHistory2 = historyManager.getHistoryByFragment(historyHash2);
        properties = loadProperties();
        Assert.assertNotNull(savedHistory2);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash2));
        Assert.assertEquals(properties.get(admin), historyHash2);
        Assert.assertNotNull(properties.get(historyHash2));

        // change entry for user "admin"
        displayableVertices.add(new TestVertex("200"));
        String historyHash3 = historyManager.saveOrUpdateHistory(admin, graphContainerMock);
        SavedHistory savedHistory3 = historyManager.getHistoryByFragment(historyHash3);
        properties = loadProperties();
        Assert.assertNotNull(savedHistory3);
        Assert.assertNotNull(properties);
        Assert.assertEquals(3, properties.size());   // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash3));
        Assert.assertTrue(properties.containsKey(historyHash2)); // this should not be removed
        Assert.assertNotNull(properties.get(historyHash3));

        // create an entry for another user, but with same historyHash
        String historyHash4 = historyManager.saveOrUpdateHistory(user1, graphContainerMock);
        SavedHistory savedHistory4 = historyManager.getHistoryByFragment(historyHash3);
        properties = loadProperties();
        Assert.assertEquals(historyHash3, historyHash4);
        Assert.assertNotNull(savedHistory4);
        Assert.assertNotNull(properties);
        Assert.assertEquals(4, properties.size()); // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(user1));
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(historyHash4));
        Assert.assertEquals(historyHash4, properties.get(admin));
        Assert.assertEquals(historyHash4, properties.get(user1));
        Assert.assertNotNull(properties.get(historyHash4));

        // change entry for user1
        displayableVertices.remove(0);
        String historyHash5 = historyManager.saveOrUpdateHistory(user1, graphContainerMock);
        SavedHistory savedHistory5 = historyManager.getHistoryByFragment(historyHash3);
        properties = loadProperties();
        Assert.assertNotNull(savedHistory5);
        Assert.assertNotNull(properties);
        Assert.assertEquals(5, properties.size());   // user -> historyId and historyId -> historyContent
        Assert.assertTrue(properties.containsKey(admin));
        Assert.assertTrue(properties.containsKey(user1));
        Assert.assertTrue(properties.containsKey(historyHash4));
        Assert.assertTrue(properties.containsKey(historyHash5));
        Assert.assertNotNull(properties.get(historyHash5));
        Assert.assertEquals(historyHash4, properties.get(admin));
        Assert.assertEquals(historyHash5, properties.get(user1));
    }

    @Test
    public void verifySearchCriteriaPersistence() {
        availableProviders.add(new CategorySearchProvider(null, null));

        graphContainerMock.addCriteria(new CustomSearchCriteria(searchQuery));
        String fragment = historyManager.saveOrUpdateHistory("admin", graphContainerMock);
        historyManager.applyHistory(fragment, graphContainerMock);

        // find search criteria in criteria list and verify that they are restored
        Criteria[] criteria = graphContainerMock.getCriteria();
        Assert.assertTrue(criteria != null && criteria.length == 1 && criteria[0] instanceof SearchCriteria &&
                         (((SearchCriteria) criteria[0]).getSearchString()).equals(searchQuery));
//        for (Criteria cr : criteria)
//        {
//            if (cr instanceof SearchCriteria)
//            {
//                Assert.assertTrue((((SearchCriteria) cr).getSearchString()).equals(searchQuery));
//            }
//        }
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(new File(DATA_FILE_NAME)));
        return props;
    }

    private void setBehaviour(CategorySearchProvider searchProvider)
    {
        EasyMock
                .expect(searchProvider.getSearchProviderNamespace())
                .andReturn("category")
                .anyTimes();

        SearchQueryHistory sqh = new SearchQueryHistory("custom", searchQuery, searchQuery);

        EasyMock.expect(searchProvider.getCriteriaFromQuery(sqh))
                .andReturn(new CustomSearchCriteria(searchQuery));
    }

    private void setBehaviour(ServiceLocator serviceLocator) {
        EasyMock
                .expect(serviceLocator.findServices(SearchProvider.class, null))
                .andReturn(availableProviders)
                .anyTimes();
    }

    private void setBehaviour(Graph graphMock) {
        EasyMock
                .expect(graphMock.getDisplayEdges())
                .andReturn(new ArrayList<Edge>())
                .anyTimes();

        EasyMock
                .expect(graphMock.getDisplayVertices())
                .andReturn(displayableVertices)
                .anyTimes();
        
        EasyMock
        	.expect(graphMock.getLayout())
        	.andReturn(selectedLayout)
        	.anyTimes();
    }

    private void setBehaviour(GraphContainer graphContainerMock) {
    	MapViewManager mapViewManagerMock = EasyMock.createNiceMock(MapViewManager.class);
        EasyMock.expect(mapViewManagerMock.getCurrentBoundingBox()).andReturn(new BoundingBox(0,0,Integer.MAX_VALUE, Integer.MAX_VALUE)).anyTimes();
        EasyMock.replay(mapViewManagerMock);
        
        SelectionManager selectionManagerMock = EasyMock.createNiceMock(SelectionManager.class);
        EasyMock.expect(selectionManagerMock.getSelectedVertexRefs()).andReturn(new ArrayList<VertexRef>()).anyTimes();
        EasyMock.replay(selectionManagerMock);
    	
    	EasyMock
                .expect(graphContainerMock.getGraph())
                .andReturn(graphMock)
                .anyTimes();

        EasyMock
                .expect(graphContainerMock.getSemanticZoomLevel())
                .andReturn(0)
                .anyTimes();

        EasyMock
        		.expect(graphContainerMock.getLayoutAlgorithm())
    			.andReturn(new CircleLayoutAlgorithm())
    			.anyTimes();
        
        EasyMock
                .expect(graphContainerMock.getMapViewManager())
                .andReturn(mapViewManagerMock)
                .anyTimes();
        
        EasyMock
        		.expect(graphContainerMock.getSelectionManager())
        		.andReturn(selectionManagerMock)
        		.anyTimes();

        EasyMock
                .expect(graphContainerMock.getCriteria())
                .andReturn(new Criteria[]{new CustomSearchCriteria(searchQuery)})
//                .andReturn(new Criteria[0])
                .anyTimes();
    }

    private void setBehaviour(BundleContext bundleContextMock) {
        EasyMock
                .expect(bundleContextMock.getDataFile(DATA_FILE_NAME))
                .andReturn(new File(DATA_FILE_NAME))
                .anyTimes();
    }
}
