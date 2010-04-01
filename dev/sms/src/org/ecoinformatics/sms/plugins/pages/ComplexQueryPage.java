/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Ben Leinfelder
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
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

package org.ecoinformatics.sms.plugins.pages;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ecoinformatics.sms.SMS;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Protocol;
import org.ecoinformatics.sms.annotation.Standard;
import org.ecoinformatics.sms.annotation.Triple;
import org.ecoinformatics.sms.annotation.search.Criteria;
import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.plugins.AnnotationPlugin;
import org.ecoinformatics.sms.plugins.search.CriteriaRenderer;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ComplexQueryPage extends AbstractUIPage {

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	private final String pageID = null;
	private final String pageNumber = "0";
	private final String title = "Complex Query";
	private final String subtitle = "";

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *
		
	// query list
	private CustomList queryList;
	
	// the final query
	private Query query;
	
	// docids from the annotation query
	private List<String> docids = new ArrayList<String>();
	private JCheckBox anyAll;
	

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	public ComplexQueryPage() {
		init();
	}

	/**
	 * initialize method does frame-specific design - i.e. adding the widgets
	 * that are displayed only in this frame (doesn't include prev/next buttons
	 * etc)
	 */
	private void init() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel desc = WidgetFactory
				.makeHTMLLabel(
						"<b>Annotation Search</b> "
								+ "Specify one or more search criteria",
						2);
		this.add(desc);
		this.add(WidgetFactory.makeDefaultSpacer());
		
		this.add(WidgetFactory.makeDefaultSpacer());
		
		// Query list		
		String[] colNames = new String[] {"Conditions"};
		Object[] editors = new Object[] {new CriteriaRenderer(true) };
		queryList = WidgetFactory.makeList(
				colNames, 
				editors, 
				10, //displayRows, 
				true, //showAddButton, 
				false, //showEditButton, 
				false, //showDuplicateButton, 
				true, //showDeleteButton, 
				false, //showMoveUpButton, 
				false //showMoveDownButton
				);		
		// add
		queryList.setCustomAddAction(new AbstractAction("Add Group") {
			public void actionPerformed(ActionEvent e) {
	
				Criteria criteria = new Criteria();
				criteria.setGroup(true);
				
				List rowList = new ArrayList();
				rowList.add(criteria);
				queryList.addRow(rowList);
				
				
			}
		});

		// the any/all checkbox
		anyAll = WidgetFactory.makeCheckBox("Match All", false);

		JPanel queryListPanel = WidgetFactory.makePanel();
		
		queryListPanel.add(anyAll);
		queryListPanel.add(queryList);
		
		queryListPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
				8 * WizardSettings.PADDING));
		this.add(queryListPanel);

		this.add(WidgetFactory.makeDefaultSpacer());

	}
	
	public List<String> getDocids() {
		return docids;
	}
	
	public Query getQuery() {
		
		// generate query
		generateQuery();
		
		return query;
	}

	private void generateQuery() {

		List<OntologyClass> characteristics = new ArrayList<OntologyClass>();
		List<OntologyClass> standards = new ArrayList<OntologyClass>();
		List<OntologyClass> protocols = new ArrayList<OntologyClass>();
		List<OntologyClass> entities = new ArrayList<OntologyClass>();
		List<Triple> contexts = new ArrayList<Triple>();

		
		for (int i = 0; i < queryList.getListOfRowLists().size(); i++) {
			
			// get the Criteria object from the list
			List rowList = (List) queryList.getListOfRowLists().get(i);
			Criteria criteria = (Criteria) rowList.get(0);
			
			if (criteria.isGroup()) {
				for (Criteria subcriteria: criteria.getSubCriteria()) {
					
					// handle context
					if (criteria.isContext()) {
						Triple context = criteria.getContextTriple();
						if (context != null) {
							contexts.add(context);
						}
					} else {
						// what criteria were given?
						OntologyClass subject = subcriteria.getSubject();
						OntologyClass value = subcriteria.getValue();
						if (value == null) {
							continue;
						}
						if (subject != null && subject.equals(AnnotationPlugin.OBOE_CLASSES.get(Entity.class))) {
							entities.add(value);
						}
						if (subject != null && subject.equals(AnnotationPlugin.OBOE_CLASSES.get(Characteristic.class))) {
							characteristics.add(value);
						}
						if (subject != null && subject.equals(AnnotationPlugin.OBOE_CLASSES.get(Standard.class))) {
							standards.add(value);
						}
						if (subject != null && subject.equals(AnnotationPlugin.OBOE_CLASSES.get(Protocol.class))) {
							protocols.add(value);
						}
					}
				}
			}
			else {
			// TODO: handle no grouping
			}
			
		}
		
		// TODO: handle ANY/ALL
		// TODO: handle is/is not
		// TODO: handle groups
		
		// generate the query
		List<Annotation> annotations = SMS.getInstance().getAnnotationManager().getMatchingAnnotations(
				entities, 
				characteristics, 
				standards,
				protocols,
				contexts,
				true);
		
		
		// get the query text
		String querySpec = AnnotationPlugin.getDocQuery(annotations);
		
		// make it
		query = new Query(querySpec, Morpho.thisStaticInstance);
		query.setSearchLocal(true);
		query.setSearchMetacat(false);
		
		// set the docids
		docids.clear();
		for (Annotation a: annotations) {
			docids.add(a.getEMLPackage());
		}
		
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	// *

	/**
	 * The action to be executed when the page is displayed. May be empty
	 */
	public void onLoadAction() {
	}

	/**
	 * The action to be executed when the "Prev" button is pressed. May be empty
	 * 
	 */
	public void onRewindAction() {

	}

	/**
	 * The action to be executed when the "Next" button (pages 1 to
	 * last-but-one) or "Finish" button(last page) is pressed. May be empty, but
	 * if so, must return true
	 * 
	 * @return boolean true if wizard should advance, false if not (e.g. if a
	 *         required field hasn't been filled in)
	 */
	public boolean onAdvanceAction() {
		return true;
	}

	/**
	 * gets the OrderedMap object that contains all the key/value paired
	 * settings for this particular wizard page
	 * 
	 * @return data the OrderedMap object that contains all the key/value paired
	 *         settings for this particular wizard page
	 */
	private OrderedMap returnMap = new OrderedMap();

	public OrderedMap getPageData() {
		return getPageData(null);
	}

	/**
	 * gets the Map object that contains all the key/value paired settings for
	 * this particular wizard page
	 * 
	 * @param rootXPath
	 *            the root xpath to prepend to all the xpaths returned by this
	 *            method
	 * @return data the Map object that contains all the key/value paired
	 *         settings for this particular wizard page
	 */
	public OrderedMap getPageData(String rootXPath) {

		return returnMap;
	}

	/**
	 * gets the unique ID for this wizard page
	 * 
	 * @return the unique ID String for this wizard page
	 */
	public String getPageID() {
		return pageID;
	}

	/**
	 * gets the title for this wizard page
	 * 
	 * @return the String title for this wizard page
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * gets the subtitle for this wizard page
	 * 
	 * @return the String subtitle for this wizard page
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * Returns the ID of the page that the user will see next, after the "Next"
	 * button is pressed. If this is the last page, return value must be null
	 * 
	 * @return the String ID of the page that the user will see next, or null if
	 *         this is te last page
	 */
	public String getNextPageID() {
		return nextPageID;
	}

	/**
	 * Returns the serial number of the page
	 * 
	 * @return the serial number of the page
	 */
	public String getPageNumber() {
		return pageNumber;
	}

	public boolean setPageData(OrderedMap data, String _xPathRoot) {
		return true;
	}

}
