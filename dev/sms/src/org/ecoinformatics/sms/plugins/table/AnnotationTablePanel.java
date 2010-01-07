/**
 *  '$RCSfile: PersistentTableModel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2004-04-06 01:27:05 $'
 * '$Revision: 1.15 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ecoinformatics.sms.plugins.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;

/**
 * Panel for viewing and editing data-centric Annotations
 * @author leinfelder
 *
 */
public class AnnotationTablePanel extends JPanel implements StateChangeListener  {

	public static final Dimension rowHeaderDim = new Dimension(100, 16);
	private JTable annotationTable;
	private JScrollPane annotationScrollPane;

	public AnnotationTablePanel(AnnotationTableModel annotationTableModel) {
		super(new BorderLayout(0,0));
		
		// add the main table
		annotationTable = new AnnotationTable(annotationTableModel);
		annotationTable.setColumnSelectionAllowed(true);
		annotationTable.setRowSelectionAllowed(true);
		annotationTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    annotationTable.getTableHeader().setReorderingAllowed(true);
	    annotationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    
	    Dimension dim = new Dimension(100, 96);
		annotationTable.setPreferredScrollableViewportSize(dim);
		
		// put it in a scrollpane
		annotationScrollPane = new JScrollPane(annotationTable);
		
		// add the row headers
	    JList rowheaders = new JList(annotationTableModel.getRowHeaders());
	    rowheaders.setPreferredSize(rowHeaderDim);
		rowheaders.setCellRenderer(new RowHeaderRenderer(annotationTable));
		annotationScrollPane.setRowHeaderView(rowheaders);
		
		// add the upper left corner
		JButton reorder = new JButton();
		reorder.setAction(new AbstractAction("Reorder") {

			public void actionPerformed(ActionEvent e) {
				((AnnotationTable)annotationTable).reorder(true);	
			}
			
		});
		annotationScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, reorder);
		
		this.add(BorderLayout.CENTER, annotationScrollPane);
		
	}

	public void handleStateChange(StateChangeEvent event) {
		AnnotationTableModel model = (AnnotationTableModel) annotationTable.getModel();
		model.fireTableRowsUpdated(0, model.getRowCount());
		model.fireTableStructureChanged();
		
		JList rowheaders = new JList(model.getRowHeaders());
		rowheaders.setCellRenderer(new RowHeaderRenderer(annotationTable));
		annotationScrollPane.setRowHeaderView(rowheaders);
	}

	public JTable getAnnotationTable() {
		return annotationTable;
	}

	public void setAnnotationTable(JTable annotationTable) {
		this.annotationTable = annotationTable;
	}

	public JScrollPane getAnnotationScrollPane() {
		return annotationScrollPane;
	}

	public void setAnnotationScrollPane(JScrollPane annotationScrollPane) {
		this.annotationScrollPane = annotationScrollPane;
	}
	
	
}

/**
 * Renderer for the Row Headers - supposed to mimic the Column header LaF
 * @author leinfelder
 *
 */
class RowHeaderRenderer extends JLabel implements ListCellRenderer, TableCellRenderer {  
    RowHeaderRenderer(JTable table) {
      JTableHeader header = table.getTableHeader();
      setOpaque(true);
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      //setBorder(header.getBorder());
      setHorizontalAlignment(CENTER);
      setForeground(header.getForeground());
      //setBackground(header.getBackground());
      setFont(header.getFont());
      setPreferredSize(AnnotationTablePanel.rowHeaderDim);
    }
   
    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {
      setText((value == null) ? "" : value.toString());
      return this;
    }

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		return getTableCellRendererComponent(null, value, isSelected, cellHasFocus, index, 0);
	}
}