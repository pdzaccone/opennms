/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.SelectionListener;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;


@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent implements Action.Container, ItemSetChangeListener, PropertySetChangeListener, ValueChangeListener {

    private static final long serialVersionUID = 1L;
	
	public class MapManager {

        private int m_clientX = 0;
        private int m_clientY = 0;
        
        public void setClientX(int clientX) {
            m_clientX = clientX;
        }

        public void setClientY(int clientY) {
            m_clientY = clientY;
        }

        public int getClientX() {
            return m_clientX;
        }

        public int getClientY() {
            return m_clientY;
        }
        
        
    }
    
	private GraphContainer m_graphContainer;
	private Property m_scale;
    private TopoGraph m_graph;
	private MapManager m_mapManager = new MapManager();
    private List<MenuItemUpdateListener> m_menuItemStateListener = new ArrayList<MenuItemUpdateListener>();
    private ContextMenuHandler m_contextMenuHandler;
    private IconRepositoryManager m_iconRepoManager;
    private boolean m_panToSelection = false;
    private boolean m_fitToView = true;
    private boolean m_scaleUpdateFromUI = false;
    private String m_activeTool = "pan";
    private List<SelectionListener> m_selectionListeners;

	public TopologyComponent(GraphContainer dataSource) {
		setGraph(new TopoGraph(dataSource));
		m_graphContainer = dataSource;
		m_graphContainer.getVertexContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getVertexContainer().addListener((PropertySetChangeListener) this);
		
		m_graphContainer.getEdgeContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getEdgeContainer().addListener((PropertySetChangeListener) this);
		
		Property scale = m_graphContainer.getProperty(DisplayState.SCALE);
		setScaleDataSource(scale);
		
	}
	
	private void setScaleDataSource(Property scale) {
	    // Stops listening the old data source changes
        if (m_scale != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(m_scale.getClass())) {
            ((Property.ValueChangeNotifier) m_scale).removeListener(this);
        }

        // Sets the new data source
        m_scale = scale;

        // Listens the new data source if possible
        if (m_scale != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(m_scale.getClass())) {
            ((Property.ValueChangeNotifier) m_scale).addListener(this);
        }
    }
	
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("scale", (Double)m_scale.getValue());
        target.addAttribute("clientX", m_mapManager.getClientX());
        target.addAttribute("clientY", m_mapManager.getClientY());
        target.addAttribute("semanticZoomLevel", m_graphContainer.getSemanticZoomLevel());
        target.addAttribute("activeTool", m_activeTool);
        
        target.addAttribute("panToSelection", getPanToSelection());
        if (getPanToSelection()) {
            
        }
        setPanToSelection(false);
        
        target.addAttribute("fitToView", isFitToView());
        setFitToView(false);
        
		getGraph().paint(target, m_iconRepoManager);
        
        
    }

	public boolean isFitToView() {
        return m_fitToView;
    }
    
    public void setFitToView(boolean fitToView) {
        m_fitToView  = fitToView;
    }

    private void setPanToSelection(boolean b) {
        m_panToSelection  = b;
    }

    private boolean getPanToSelection() {
        return m_panToSelection;
    }

    /**
     * Main vaadin method for receiving communication from the Front End
     * 
     */
	@SuppressWarnings("unchecked")
	@Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if(variables.containsKey("graph")) {
            String graph = (String) variables.get("graph");
            getApplication().getMainWindow().showNotification("" + graph);
            
        }
        
        if(variables.containsKey("clickedEdge")) {
            String edgeId = (String) variables.get("clickedEdge");
            singleSelectEdge(edgeId);
            updateSelectionListeners();
        }
        
        if(variables.containsKey("deselectAllItems")) {
            deselectAllEdges();
            deselectAllVertices();
            requestRepaint();
            updateSelectionListeners();
        }
        
        if(variables.containsKey("clickedVertex")) {
        	String vertexId = (String) variables.get("clickedVertex");
            if(variables.containsKey("shiftKeyPressed") && (Boolean) variables.get("shiftKeyPressed") == true) {
        	    multiSelectVertex(vertexId);
        	}else {
        	    singleSelectVertex(vertexId);
        	}
        	
            updateSelectionListeners();
        }
        
        if(variables.containsKey("marqueeSelection")) {
            String[] vertexIds = (String[]) variables.get("marqueeSelection");
            if(variables.containsKey("shiftKeyPressed") && (Boolean) variables.get("shiftKeyPressed") == false) {
                clearAllVertexSelections();
            }
            
            bulkMultiSelectVertex(vertexIds);
        }
        
        if(variables.containsKey("updatedVertex")) {
            String vertexUpdate = (String) variables.get("updatedVertex");
            updateVertex(vertexUpdate);
            
            requestRepaint();
        }
        
        if(variables.containsKey("updateVertices")) {
            String[] vertices = (String[]) variables.get("updateVertices");
            for(String vUpdate : vertices) {
                updateVertex(vUpdate);
            }
            
            if(vertices.length > 0) {
                requestRepaint();
            }
            
        }
        
        if(variables.containsKey("mapScale")) {
            double newScale = (Double)variables.get("mapScale");
            setScaleUpdateFromUI(true);
            setScale(newScale);
        }
        
        if(variables.containsKey("clientX")) {
            int clientX = (Integer) variables.get("clientX");
            m_mapManager.setClientX(clientX);
        }
        
        if(variables.containsKey("clientY")) {
            int clientY = (Integer) variables.get("clientY");
            m_mapManager.setClientY(clientY);
        }
        
        if(variables.containsKey("contextMenu")) {
            Map<String, Object> props = (Map<String, Object>) variables.get("contextMenu");
            
            String type = (String) props.get("type");
            
            int x = (Integer) props.get("x");
            int y = (Integer) props.get("y");
            Object itemId = (Object)props.get("target");

            if (type.toLowerCase().equals("vertex")) {
	                TopoVertex vertex = getGraph().getVertexByKey((String)itemId);
	                itemId = vertex.getItemId();
            }

            getContextMenuHandler().show(itemId, x, y);
        }
        
        updateMenuItems();
    }

    private void setScaleUpdateFromUI(boolean scaleUpdateFromUI) {
        m_scaleUpdateFromUI  = scaleUpdateFromUI;
    }
    
    private boolean isScaleUpdateFromUI() {
        return m_scaleUpdateFromUI;
    }

    private void updateVertex(String vertexUpdate) {
        String[] vertexProps = vertexUpdate.split("\\|");
        
        String id = vertexProps[0].split(",")[1];
        int x = (int) Double.parseDouble(vertexProps[1].split(",")[1]);
        int y = (int) Double.parseDouble(vertexProps[2].split(",")[1]);
        boolean selected = vertexProps[3].split(",")[1].equals("true");
        
        TopoVertex vertex = getGraph().getVertexByKey(id);
        vertex.setX(x);
        vertex.setY(y);
        vertex.setSelected(selected);
    }
    
	private void clearAllVertexSelections() {
	    for(TopoVertex vertex : getGraph().getVertices()) {
	        vertex.setSelected(false);
	    }
	}
	
	private void singleSelectEdge(String edgeId) {
	    deselectAllVertices();
	    deselectAllEdges();
	    
	    if(edgeId.isEmpty()) {
	        requestRepaint();
	    }else {
	        toggleSelectedEdge(edgeId);
	    }
	}

    private void deselectAllEdges() {
        for(TopoEdge edge : getGraph().getEdges()) {
	        edge.setSelected(false);
	    }
    }
	
    private void singleSelectVertex(String vertexId) {
        deselectAllEdges();
        deselectAllVertices();
        
        if(vertexId.isEmpty()) {
            requestRepaint();
        } else {
            toggleSelectedVertex(vertexId);
        }
    }

    private void deselectAllVertices() {
        for(TopoVertex vertex : getGraph().getVertices()) {
            vertex.setSelected(false);
        }
    }
    
    public void selectVerticesByItemId(Collection<Object> itemIds) {
        deselectAllVertices();
        
        for(Object itemId : itemIds) {
            toggleSelectVertexByItemId(itemId);
        }
        
        if(itemIds.size() > 0) {
            setPanToSelection(true);
            requestRepaint();
        }
    }
    
    /**
     * Select multiple vertices at a time
     * @param vertexIds
     */
    private void bulkMultiSelectVertex(String[] vertexIds) {
        for(String vertexId : vertexIds) {
            TopoVertex vertex = getGraph().getVertexByKey((String)vertexId);
            vertex.setSelected(true);
        }
        
        requestRepaint();
    }
    
    private void multiSelectVertex(String vertexId) {
        toggleSelectedVertex(vertexId);
    }

    private void toggleSelectedVertex(String vertexId) {
		TopoVertex vertex = getGraph().getVertexByKey(vertexId);
		if(vertex != null) {
		    vertex.setSelected(!vertex.isSelected());
		}
		
		if(m_graphContainer.getVertexContainer().hasChildren(vertex.getItemId())) {
		    Collection<?> children = m_graphContainer.getVertexContainer().getChildren(vertex.getItemId());
		    for( Object childId : children) {
		        TopoVertex v = getGraph().getVertexByItemId(childId);
		        v.setSelected(true);
		    }
		}
		
		m_graphContainer.getVertexContainer().fireItemSetChange();
		setFitToView(false);
		requestRepaint();
	}
    
    private void toggleSelectVertexByItemId(Object itemId) {
        TopoVertex vertex = getGraph().getVertexByItemId(itemId);
        vertex.setSelected(!vertex.isSelected());
        
        requestRepaint();
    }
    
    private void toggleSelectedEdge(String edgeItemId) {
        TopoEdge edge = getGraph().getEdgeByKey(edgeItemId);
        edge.setSelected(!edge.isSelected());
        
        requestRepaint();
    }

    
	protected void setScale(double scale){
	    m_scale.setValue(scale);
    }
    
    protected TopoGraph getGraph() {
		return m_graph;
	}

	public void addActionHandler(Handler actionHandler) {
	}
	
	public void removeActionHandler(Handler actionHandler) {
	}
	
	public void addMenuItemStateListener(MenuItemUpdateListener listener) {
        m_menuItemStateListener.add(listener);
    }
	
	public void removeMenuItemStateListener(MenuItemUpdateListener listener) {
	    m_menuItemStateListener.remove(listener);
	}
	
	private void updateMenuItems() {
	    for(MenuItemUpdateListener listener : m_menuItemStateListener) {
	        listener.updateMenuItems();
	    }
	}

	private void setGraph(TopoGraph graph) {
		m_graph = graph;
	}
	
	public void setContainerDataSource(GraphContainer graphContainer) {
		getGraph().setDataSource(graphContainer);
		m_graphContainer = graphContainer;
		m_graphContainer.getVertexContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getVertexContainer().addListener((PropertySetChangeListener) this);
		
		m_graphContainer.getEdgeContainer().addListener((ItemSetChangeListener)this);
		m_graphContainer.getEdgeContainer().addListener((PropertySetChangeListener) this);
	}

	public void containerItemSetChange(ItemSetChangeEvent event) {
		getGraph().update();
		setFitToView(true);
		requestRepaint();
	}

	public void containerPropertySetChange(PropertySetChangeEvent event) {
		getGraph().update();
		requestRepaint();
	}

    public void valueChange(ValueChangeEvent event) {
        double scale = (Double) m_scale.getValue();
        if(scale == 0) {
            m_scale.setValue(0.01);
        }
        
        if(!isScaleUpdateFromUI()) {
            requestRepaint();
            setScaleUpdateFromUI(false);
        }else {
            setScaleUpdateFromUI(false);
        }
    }

    public ContextMenuHandler getContextMenuHandler() {
        return m_contextMenuHandler;
    }

    public void setContextMenuHandler(ContextMenuHandler contextMenuHandler) {
        m_contextMenuHandler = contextMenuHandler;
    }

    public IconRepositoryManager getIconRepoManager() {
        return m_iconRepoManager;
    }

    public void setIconRepoManager(IconRepositoryManager iconRepoManager) {
        m_iconRepoManager = iconRepoManager;
    }

    public void setActiveTool(String toolname) {
        if(!m_activeTool.equals(toolname)) {
            m_activeTool = toolname;
            requestRepaint();
        }
    }

    public Collection<?> getItemIdsForSelectedVertices() {
        Collection<?> vItemIds = m_graphContainer.getVertexContainer().getItemIds();
        List<Object> selectedIds = new ArrayList<Object>(); 
        
        for(Object itemId : vItemIds) {
            if(getGraph().getVertexByItemId(itemId).isSelected()) {
                selectedIds.add(itemId);
            }
        }
        
        return selectedIds;
    }

    /**
     * Add a listener that will listen for items to be selected in the UI.
     */
    public void addSelectionListener(SelectionListener listener) {
        if(m_selectionListeners == null) {
            m_selectionListeners = new ArrayList<SelectionListener>();
        }
        m_selectionListeners.add(listener);
    }
    
    private void updateSelectionListeners() {
        for(SelectionListener listener : m_selectionListeners) {
            listener.onSelectionUpdate(m_graphContainer.getVertexContainer());
        }
    }

}
