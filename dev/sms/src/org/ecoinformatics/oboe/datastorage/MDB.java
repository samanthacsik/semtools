package org.ecoinformatics.oboe.datastorage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.model.ContextInstance;
import org.ecoinformatics.oboe.model.EntityInstance;
import org.ecoinformatics.oboe.model.MeasurementInstance;
import org.ecoinformatics.oboe.model.OboeModel;
import org.ecoinformatics.oboe.model.ObservationInstance;
import org.ecoinformatics.oboe.query.Constant;
import org.ecoinformatics.oboe.util.Debugger;
import org.ecoinformatics.oboe.util.Pair;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Context;
import org.ecoinformatics.sms.annotation.Mapping;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;


/**
 * Class for materialized database 
 * 
 * @author cao
 *
 */
public class MDB extends PostgresDB{
	
	private int m_query_strategy = -1;
	
	//private String m_entityInstanceTable = "entity_instance";
	final private String  m_obsInstanceTable = "observation_instance";
	final protected String m_measInstanceTable = "measurement_instance";
	final private String m_contextInstanceTable = "context_instance";
	//public static String m_entityInstanceCompressTable = "ei_compress";
	final public static String m_obsInstanceCompressTable = "oi_compress";
	final public String m_nonAggMeasView = "non_agg_meas_view";
	final public String m_nonAggMeasViewBasicInitData = "non_agg_meas_view_basic_init_data";
	final public String m_nonAggMeasViewBasic = "non_agg_meas_view_basic";
	final public String m_mergedTableView = "mtb_view"; //merge oi and mi, denormalization
	final public String m_mergedFullTable = "omi_numeric_full"; //merge oi, mi and mt, denormalization
	
	//protected String m_insertDatasetAnnot = "INSERT INTO " + m_datasetAnnotTable +"(annot_id,dataset_file) VALUES(?,?);";
	final String m_insertAnnotation = "INSERT INTO " + m_datasetAnnotTable +"(dataset_file,annot_uri) VALUES(?,?);";
	final String m_insertDatasetAnnot = "INSERT INTO " + m_datasetAnnotTable +"(dataset_file) VALUES(?);";
	//String m_insertEntityInstance ="INSERT INTO " +m_entityInstanceTable + "(eid,did,record_id,etype) VALUES(?,?,?,?);";
	//String m_insertObservationInstance ="INSERT INTO " +m_obsInstanceTable + "(oid,did,record_id,eid,otypelabel) VALUES(?,?,?,?,?);";
	final String m_insertObservationInstance ="INSERT INTO " +m_obsInstanceTable + "(oid,did,record_id,etype,otypelabel) VALUES(?,?,?,?,?);";
	final String m_insertMeasurementInstance ="INSERT INTO " +m_measInstanceTable + "(mid,did,record_id,oid,mtypelabel,mvalue) VALUES(?,?,?,?,?,?);";
	final String m_insertContextInstance ="INSERT INTO " +m_contextInstanceTable + " VALUES(?,?,?,?,?);";
	
	//String m_insertEICompress ="INSERT INTO " +m_entityInstanceCompressTable + " VALUES(?,?,?);";
	final String m_insertOICompress ="INSERT INTO " +m_obsInstanceCompressTable + " VALUES(?,?,?);";
	
	final String m_insertObservationType = "INSERT INTO " + m_obsTypeTable + " VALUES(?,?,?,?);";
	final String m_insertMeasurementType = "INSERT INTO " + m_measTypeTable + " VALUES(?,?,?,?,?,?,?)";
	final String m_insertContextType = "INSERT INTO " + m_contextTypeTable + " VALUES(?,?,?,?,?)";
	final String m_insertMap = "INSERT INTO " + m_mapTable + "(annot_id,mtypelabel,attrname,mapcond,mapval) VALUES(?,?,?,?,?);";
	
	final String m_maxAnnotationIdSql = "SELECT last_value FROM annot_id_seq;";
	final String m_maxEntityInstanceIdSql = "SELECT last_value FROM eid_seq;";
	final String m_maxObsInstanceIdSql = "SELECT last_value FROM oid_seq;";
	final String m_maxMeasInstanceIdSql = "SELECT last_value FROM mid_seq;";
	
	public MDB(String dbname){
		setDb(dbname);		
	}
	
	public String getMeasInstanceTable() {
		return m_measInstanceTable;
	}

	public String getObsInstanceTable() {
		return m_obsInstanceTable;
	}

	public String getContextInstanceTable() {
		return m_contextInstanceTable;
	}

	public int getQueryStrategy() {
		return m_query_strategy;
	}

	public void setQueryStrategy(int mQueryStrategy) {
		m_query_strategy = mQueryStrategy;
	}
	
	/**
	 * Return the maximum annotation id 
	 * @return
	 * @throws SQLException
	 */
	private long getMaxAnnotId() throws SQLException
	{
		Statement stmt = m_conn.createStatement();
		
		ResultSet rs = stmt.executeQuery(m_maxAnnotationIdSql);
		long maxAnnotId = 1;
		while(rs.next()){
			maxAnnotId = rs.getLong(1);
			break;
		}
		
		return maxAnnotId;
	}
	
	
	
	/**
	 * Return the maximum entity instance id
	 * @return
	 * @throws SQLException
	 */
	private long getMaxEntityInstanceId() throws SQLException
	{
		Statement stmt = m_conn.createStatement();
		
		ResultSet rs = stmt.executeQuery(m_maxEntityInstanceIdSql);
		long maxEntityInstanceId = 1;
		while(rs.next()){
			maxEntityInstanceId = rs.getLong(1);
			break;
		}
		
		return maxEntityInstanceId;
	}
	
	/**
	 * Return the maximum observation instance id
	 * @return
	 * @throws SQLException
	 */
	private long getMaxObsInstanceId() throws SQLException
	{
		Statement stmt = m_conn.createStatement();
		
		ResultSet rs = stmt.executeQuery(m_maxObsInstanceIdSql);
		long maxMaxObsInstanceId = 1;
		while(rs.next()){
			maxMaxObsInstanceId = rs.getLong(1);
			break;
		}
		
		return maxMaxObsInstanceId;
	}
	
	/**
	 * Return the maximum measurement instance id
	 * @return
	 * @throws SQLException
	 */
	private long getMaxMeasInstanceId() throws SQLException
	{
		Statement stmt = m_conn.createStatement();
		
		ResultSet rs = stmt.executeQuery(m_maxMeasInstanceIdSql);
		long maxMaxMeasInstanceId = 1;
		while(rs.next()){
			maxMaxMeasInstanceId = rs.getLong(1);
			break;
		}
		
		return maxMaxMeasInstanceId;
	}
	
	
	/**
	 * Set the parameters for inserting entity instances
	 * String m_insertEntityInstance ="INSERT INTO " +m_entityInstanceTable + "(did,record_id,etype) VALUES(?,?,?);";
	 * @param pstmt
	 * @param ei
	 * @throws Exception 
	 */
	private void setEntityInstanceParam(PreparedStatement pstmt, EntityInstance ei, long did) 
		throws Exception
	{
		pstmt.setLong(1, ei.getEntId());
		pstmt.setLong(2, did);
		//if(ei.getRecordId().length()>=RECORD_ID_LEN){
		//	throw new Exception("Entity record id length is bigger than 16.");
		//}
		//pstmt.setString(3,ei.getRecordId());
		pstmt.setLong(3,ei.getRecordId());
		pstmt.setString(4, ei.getEntityType().getName());		
	}
	
	/**
	 * Set the parameters for inserting measurement instances
	 * String m_insertMeasurementInstance ="INSERT INTO " +m_measInstanceTable + "(did,record_id,oid,mtypelabel,mvalue) VALUES(?,?,?,?,?);";	 
	 * 
	 * @param pstmt
	 * @param mi
	 * @throws SQLException
	 */
	private void execEICompress(PreparedStatement pstmt, EntityInstance ei,long did) 
		throws SQLException
	{	
		pstmt.setLong(1, did);
		pstmt.setLong(2, ei.getEntId());
		
		for(Long compressedRecordId: ei.getCompressedRecordIds()){
			pstmt.setLong(3, compressedRecordId);
			pstmt.execute();
		}
	}
	
	/**
	 * Set the parameters for inserting measurement instances
	 * String m_insertMeasurementInstance ="INSERT INTO " +m_measInstanceTable + "(did,record_id,oid,mtypelabel,mvalue) VALUES(?,?,?,?,?);";	 
	 * 
	 * @param pstmt
	 * @param mi
	 * @throws SQLException
	 */
	private void execOICompress(PreparedStatement pstmt, ObservationInstance oi,long did) 
		throws SQLException
	{	
		pstmt.setLong(1, did);
		pstmt.setLong(2, oi.getObsId());
		
		for(Long compressedRecordId: oi.getCompressedRecordIds()){
			pstmt.setLong(3, compressedRecordId);
			pstmt.execute();
		}
	}
	
	/**
	 * Set the parameters for inserting observation instances
	 * String m_insertObservationInstance ="INSERT INTO " +m_obsInstanceTable + "(did,record_id,eid,otypelabel) VALUES(?,?,?,?);";
	 * 
	 * @param pstmt
	 * @param ei
	 * @throws SQLException
	 */
	private void setObsInstanceParam(PreparedStatement pstmt, ObservationInstance oi, long did) 
		throws SQLException
	{
		pstmt.setLong(1, oi.getObsId());
		pstmt.setLong(2, did);
		pstmt.setLong(3, oi.getRecordId());
		pstmt.setString(4, oi.getEntity().getEntityType().getName());		
		//pstmt.setLong(4, oi.getEntity().getEntId());		
		pstmt.setString(5, oi.getObsType().getLabel());		
	}
	
	/**
	 * Set the parameters for insertion to observation type table
	 * (1) Annotation id
	 * (2) Observation type label
	 * (3) Observation entity type name
	 * (4) isDistinct
	 * 
	 * @param pstmt
	 * @param annotationId
	 * @param ot
	 * @throws SQLException
	 */
	private void setObsTypeParam(PreparedStatement pstmt, long annotationId, Observation ot) 
		throws SQLException
	{
		pstmt.setLong(1, annotationId);
		pstmt.setString(2,ot.getLabel());
		pstmt.setString(3, ot.getEntity().getName());
		pstmt.setBoolean(4, ot.isDistinct());
	}
	
	
	/**
	 * Set the parameters for insertion to context type table
	 * (1) annotation id
	 * (2) observation type label
	 * (3) context observation type label
	 * (4) context relationship name
	 * (5) is identifying
	 * 
	 * @param pstmtContext
	 * @param annotationId
	 * @param obsType
	 * @param context
	 * @throws SQLException
	 */
	private void setContextTypeParam(PreparedStatement pstmtContext, Long annotationId,
			Observation obsType, Context context) throws SQLException
	{
		pstmtContext.setLong(1, annotationId);
		pstmtContext.setString(2, obsType.getLabel());
		pstmtContext.setString(3, context.getObservation().getLabel());
		if(context.getRelationship()!=null)
			pstmtContext.setString(4, context.getRelationship().getName());
		else
			pstmtContext.setString(4, org.ecoinformatics.oboe.Constant.DEFAULT_RELATIONSHIP);
		pstmtContext.setBoolean(5, context.isIdentifying());		
	}

	
	/**
	 * Set the parameters for insertion to observation type table
	 * [1] Annotation id (long)
	 * [2] Measurement type char(16), 
	 * [3] Observation label varchar(64),
	 * [4] iskey boolean,
	 * [5] characteristic char(64),
	 * [6] standard char(16),
	 * [7] protocal varchar(256);
	 * 
	 * @param stmt
	 * @param ot
	 * @param mt
	 * @return
	 * @throws SQLException 
	 */
	private void setMeasTypeParam(PreparedStatement stmt, Long annotId,Observation ot, Measurement mt) 
		throws SQLException, Exception
	{
		String characteristicName = "";
		String standardName = "";
		String protocalName= "";
		
		if(mt.getCharacteristics()!=null){
			if(mt.getCharacteristics().size()==0){
				throw new Exception("mt does not have characteristics!");
			}
			characteristicName = mt.getCharacteristics().get(0).getName();
		}
		if(mt.getStandard()!=null){
			standardName = mt.getStandard().getName();
		}
		
		if(mt.getProtocol()!=null){
			protocalName = mt.getProtocol().getName();
		}
		
		stmt.setLong(1,annotId);
		stmt.setString(2,mt.getLabel());
		stmt.setString(3,ot.getLabel());
		stmt.setBoolean(4,mt.isKey());
		stmt.setString(5,characteristicName);
		stmt.setString(6,standardName);
		stmt.setString(7, protocalName);
	}
	

	
	/**
	 * Set the parameters for inserting measurement instances
	 * String m_insertMeasurementInstance ="INSERT INTO " +m_measInstanceTable + "(did,record_id,oid,mtypelabel,mvalue) VALUES(?,?,?,?,?);";	 
	 * 
	 * @param pstmt
	 * @param mi
	 * @throws SQLException
	 */
	private void setMeasInstanceParam(PreparedStatement pstmt, MeasurementInstance mi,long did) 
		throws SQLException
	{	
		pstmt.setLong(1, mi.getMeasId());
		pstmt.setLong(2, did);
		pstmt.setLong(3, mi.getRecordId());
		pstmt.setLong(4, mi.getObservationInstance().getObsId());		
		pstmt.setString(5, mi.getMeasurementType().getLabel());		
		pstmt.setString(6, mi.getMeasValue());
	}
	

	
	/**
	 * Set the parameters for inserting context instances
	 * 
	 * (5) is used to join with context relationship
	 * @param pstmt
	 * @param ei
	 * @throws SQLException
	 */
	private void setContextInstanceParam(PreparedStatement pstmt, ContextInstance ci,long did) 
		throws SQLException
	{
		pstmt.setLong(1, did); //did
		pstmt.setLong(2, ci.getObservationInstance().getRecordId()); //record id (inside did)
		pstmt.setLong(3, ci.getObservationInstance().getObsId()); 	 //observation instance id
		pstmt.setLong(4, ci.getContextObservationInstance().getObsId()); //context observation instance id
		if(ci.getContextType()!=null&&ci.getContextType().getRelationship()!=null)
			pstmt.setString(5, ci.getContextType().getRelationship().getName()); //relationship name
		else
			pstmt.setString(5, org.ecoinformatics.oboe.Constant.DEFAULT_RELATIONSHIP);
	}
	

	
	/**
	 * String m_insertMap = "INSERT INTO" + m_mapTable + "(annot_id,mtypelabel,attrname,mapcond,mapval) VALUES(?,?,?,?,?);";
	 * 
	 * @param pstmt
	 * @param map
	 * @param annotationId
	 * @throws SQLException 
	 */
	private void setMapParam(PreparedStatement pstmt, Mapping map, long annotationId) throws SQLException
	{
		pstmt.setLong(1, annotationId);
		pstmt.setString(2,map.getMeasurement().getLabel());
		pstmt.setString(3, map.getAttribute());
		String mapCondition ="";
		if(map.getConditions()!=null&&map.getConditions().size()>0){
			mapCondition = map.getConditions().get(0).toString(); 
		}
		pstmt.setString(4,mapCondition);
		pstmt.setString(5, map.getValue());
	}
	

	
//	/**
//	 * Insert into the dataset_annotation table the dataset file (it's did is automatically created) and its related annotation id
//	 * @param datasetFileName
//	 * @param annotation_id
//	 * @return
//	 * @throws SQLException 
//	 */
//	private long insertDatasetFile(String datasetFileName, long annotation_id) throws SQLException,Exception
//	{
//		if(annotation_id<0){
//			throw new Exception("insertDatasetFile, but new annotation id <0!!!!");
//		}
//		//Pair<Long,Long> tbid_annotid_pairs = super.getDataTableId(datasetFileName);
//		Pair<Long,String> tbid_annotid_pairs = super.getDataTableId(datasetFileName);
//		
//		long tbid=tbid_annotid_pairs.getFirst();
//		//long oldAnnotId = tbid_annotid_pairs.getSecond();
//		String oldAnnotUrl = tbid_annotid_pairs.getSecond();
//		
//		long maxDatasetId = -1L;
//		if(tbid<0){
//			PreparedStatement pstmt = m_conn.prepareStatement(m_insertDatasetAnnot);
//			//pstmt.setLong(1, annotation_id);
//			pstmt.setString(1, datasetFileName);
//			pstmt.execute();
//			pstmt.close();
//			
//			maxDatasetId = getMaxDatasetId();
//		}else{
//			//if(oldAnnotId>0)
//			if(oldAnnotUrl!=null&&oldAnnotUrl.length()>0)
//			{
//				throw new Exception(Debugger.getCallerPosition()+"update data annotation, but there is old annotation alreadyCLEAN THEM!!");
//				//System.out.println(Debugger.getCallerPosition()+"update data annotation, but there is old annotation alreadyCLEAN THEM!!");
//				//delete(datasetFileName);
//			}
//			//String sql = "UPDATE " + this.m_datasetAnnotTable + " SET annot_id="+annotation_id +" WHERE did="+tbid;
//			String sql = "UPDATE " + this.m_datasetAnnotTable + " SET annot_uri="+oldAnnotUrl +" WHERE did="+tbid;
//			System.out.println(Debugger.getCallerPosition()+"Execute: "+sql);
//			Statement stmt = m_conn.createStatement();
//			stmt.execute(sql);
//			stmt.close();
//			
//			maxDatasetId = tbid;
//		}
//
//		
//		return maxDatasetId;		
//	}
	
	/**
	 * Insert the annotation URI to the table, and get the last annotation. 
	 * 
	 * @param annotationUri
	 * @return
	 * @throws Exception 
	 */
	private long insAnnotationFile(String dataFileName,String annotationUri) 
		throws Exception
	{
		//get pure data file name
		int pos = dataFileName.lastIndexOf("/");
		String pureDataFileName = dataFileName.trim();
		if(pos>=0)pureDataFileName = dataFileName.trim().substring(pos+1);
		
		//get pure annotation uri
		pos = annotationUri.lastIndexOf("/");
		String pureAnnotationUri = annotationUri.trim();		
		if(pos>=0)pureAnnotationUri = pureAnnotationUri.trim().substring(pos+1);
		
		//get the table id-annotation url pair for a given data file
		Pair<Long,String> tbid_annoturl_pairs = super.getDataTableId(pureDataFileName);
		long tbid=tbid_annoturl_pairs.getFirst();
		String oldAnnotUrl = tbid_annoturl_pairs.getSecond();
		
		long tbId = -1L;
		if(tbid<0){
			//there is no record in the table corresponds to this data file
			PreparedStatement pstmt = m_conn.prepareStatement(m_insertAnnotation);
		
			pstmt.setString(1, pureDataFileName);
			pstmt.setString(2, pureAnnotationUri);
			pstmt.execute();
			pstmt.close();
			tbId = getMaxDatasetId();
		}else{
			tbId = tbid;
			//there is record for this data file already
			if(oldAnnotUrl==null||oldAnnotUrl.length()==0){
				Statement stmt = m_conn.createStatement();
				String updAnnotsql = "UPDATE "+ this.m_datasetAnnotTable +" SET annot_uri = '"+ pureAnnotationUri +"' WHERE did="+tbid+";";
				System.out.println(Debugger.getCallerPosition()+"3. EXECUTE: "+updAnnotsql);
				stmt.execute(updAnnotsql);
				stmt.close();
			}else{
				throw new Exception("There exists old annotation url = "+ oldAnnotUrl +" for data file="+dataFileName); 
			}
		}
		
		//long maxAnnotId = getMaxAnnotId();
		//long maxDatasetId = this.getMaxDatasetId();
		//return maxAnnotId;
		//return maxDatasetId;
		return tbId;
	}
	
	
	/**
	 * import the annotation type information
	 * @param A
	 * @throws SQLException 
	 */
	public long importAnnotation(Annotation A, String dataFileName, String annotationFileName) throws SQLException,Exception
	{
		//entity type,observation type, measurement type, context type
		List<Observation> obsTypeList= A.getObservations();
		
		String annotationUri = A.getURI();
		if(annotationUri==null){
			annotationUri = annotationFileName;
		}
		
		//insert into the annotation table
		long annotationId = insAnnotationFile(dataFileName,annotationUri);
		System.out.println(Debugger.getCallerPosition()+"annotationUri="+annotationUri+", annotationId="+annotationId);
		
		PreparedStatement pstmtObs = m_conn.prepareStatement(m_insertObservationType);
		PreparedStatement pstmtMeas = m_conn.prepareStatement(m_insertMeasurementType);
		PreparedStatement pstmtContext = m_conn.prepareStatement(m_insertContextType);
		
		for(Observation obsType: obsTypeList){
			//Add this observation type
			try{
				setObsTypeParam(pstmtObs,annotationId,obsType);			
				pstmtObs.execute();
			}catch(SQLException e){
				 System.out.println(Debugger.getCallerPosition()+"Import obsType error: "+obsType);
				 System.out.println(Debugger.getCallerPosition()+"1: annotid "+annotationId);
				 System.out.println(Debugger.getCallerPosition()+"2: obsT label "+obsType.getLabel());
				 System.out.println(Debugger.getCallerPosition()+"3: ET type "+obsType.getEntity().getName());
				 System.out.println(Debugger.getCallerPosition()+"4: OT distinct "+obsType.isDistinct());
				 throw e;
			}
				
				//Add the measurement types related to this observation type
			Measurement meas=null;
			try{
				for(int i=0; i<obsType.getMeasurements().size();i++){
					meas = obsType.getMeasurements().get(i);
					setMeasTypeParam(pstmtMeas,annotationId,obsType,meas);
					pstmtMeas.execute();
				}
			}catch(SQLException e){
				 System.out.println(Debugger.getCallerPosition()+"Import meas error: "+meas);
				 throw e;
			}
				
				//Add the context types related to this observation type
			Context context = null;
			try{
				for(int i=0;i<obsType.getContexts().size();i++){
					context  = obsType.getContexts().get(i);
					setContextTypeParam(pstmtContext,annotationId,obsType,context);
					//System.out.println(Debugger.getCallerPosition()+pstmtContext.toString());
					pstmtContext.execute();
				}
			}catch(SQLException e){
				 System.out.println(Debugger.getCallerPosition()+"Import context error: "+context);
				 throw e;
			}
		}
		pstmtContext.close();
		pstmtMeas.close();
		pstmtObs.close();
		
		// Set the mapping information to the database
		List<Mapping> mappings = A.getMappings();
		if(mappings!=null&&mappings.size()>0){
			PreparedStatement pstmtMap = m_conn.prepareStatement(m_insertMap);
			for(Mapping map: mappings){
				try{
					setMapParam(pstmtMap,map,annotationId);
					pstmtMap.execute();
				}catch(SQLException e){
					 System.out.println(Debugger.getCallerPosition()+"Import map error: "+map);
					 throw e;
				}
			}
			pstmtMap.close();
		}
		
		return annotationId;
	}
	
	/**
	 * Import the data instances
	 * @param oboe
	 * @throws Exception 
	 */
	public void importInstance(OboeModel oboe, long annotId) throws Exception
	{
		//long dId = insertDatasetFile(oboe.getDatasetFile(),annotId);
		long dId = annotId;
		//System.out.println(Debugger.getCallerPosition()+"dId="+dId+", dataset fname="+oboe.getDatasetFile());
		
		//PreparedStatement pstmtEntity = m_conn.prepareStatement(this.m_insertEntityInstance);
		PreparedStatement pstmtObs = m_conn.prepareStatement(this.m_insertObservationInstance);
		PreparedStatement pstmtMeas = m_conn.prepareStatement(this.m_insertMeasurementInstance);
		PreparedStatement pstmtContext = m_conn.prepareStatement(this.m_insertContextInstance);
		PreparedStatement pstmtObsCompressed = m_conn.prepareStatement(this.m_insertOICompress);
		
//		PreparedStatement pstmtEntityCompressed = m_conn.prepareStatement(this.m_insertEICompress);
		 //entity instance 
//		System.out.println(Debugger.getCallerPosition()+"import " + oboe.m_entityInstances.size() +" entity instances...");
//		for(EntityInstance ei: oboe.m_entityInstances){
//			 setEntityInstanceParam(pstmtEntity, ei, dId);
//			 pstmtEntity.execute();
//			 
//			 //put the EI compressed record ids
//			 execEICompress(pstmtEntityCompressed, ei, dId);
//		 }
		 
		 System.out.println(Debugger.getCallerPosition()+"import " + oboe.m_observationInstances.size() +" observation instances...");
		 //observation instance
		 for(ObservationInstance oi: oboe.m_observationInstances){
			 setObsInstanceParam(pstmtObs, oi, dId);
			 pstmtObs.execute();
			 
			//put the OI compressed record ids
			 execOICompress(pstmtObsCompressed, oi, dId);
		 }

		 //measurement instance
		 System.out.println(Debugger.getCallerPosition()+"import " + oboe.m_measurementInstances.size() +" measurement instances...");
		 for(MeasurementInstance mi: oboe.m_measurementInstances){
			 this.setMeasInstanceParam(pstmtMeas, mi, dId);
			 pstmtMeas.execute();
		 }

		 //context instance
		 System.out.println(Debugger.getCallerPosition()+"import " + oboe.m_contextInstances.size() +" context instances...");
		 for(ContextInstance ci: oboe.m_contextInstances){
			 this.setContextInstanceParam(pstmtContext, ci,dId);
			 pstmtContext.execute();
		 }
	}
	
	private void cleanMDBInstance(long tbId) throws SQLException
	{
		Statement stmt = null;
		stmt = m_conn.createStatement();
					
		//String cleanEICompresssql = "DELETE FROM "+ MDB.m_entityInstanceCompressTable +" WHERE did="+tbId;
		String cleanEICompresssql = "DELETE FROM "+ MDB.m_obsInstanceCompressTable +" WHERE did="+tbId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleanEICompresssql);
		stmt.execute(cleanEICompresssql);
		
		String cleaanCIsql = "DELETE FROM "+ this.m_contextInstanceTable +" WHERE did="+tbId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanCIsql);
		stmt.execute(cleaanCIsql);
		
		String cleaanMIsql = "DELETE FROM "+ this.m_measInstanceTable +" WHERE did="+tbId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanMIsql);
		stmt.execute(cleaanMIsql);
		
		String cleaanOIsql = "DELETE FROM "+ this.m_obsInstanceTable +" WHERE did="+tbId;
		System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanOIsql);
		stmt.execute(cleaanOIsql);
		
		//String cleaanEIsql = "DELETE FROM "+ this.m_entityInstanceTable +" WHERE did="+tbId;
		//System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanEIsql);
		//stmt.execute(cleaanEIsql);
		stmt.close();
	}
	/**
	 * Delete the materialized data contents from related table for the given data file
	 * 
	 * @param dataFileName
	 * @throws IOException
	 * @throws Exception
	 */
	public void delete(String dataFileName) throws IOException,Exception
	{
		super.open();
		int pos = dataFileName.lastIndexOf("/");
		String pureDataFileName = dataFileName.trim();		
		if(pos>=0){
			pureDataFileName = dataFileName.trim().substring(pos+1);
		} 
		//Pair<Long,Long> tbId_annotId = getDataTableId(pureDataFileName);
		Pair<Long,String> tbId_annotUrl = getDataTableId(pureDataFileName);
		
		long tbId = tbId_annotUrl.getFirst();
		if(tbId>=0){
			System.out.println(Debugger.getCallerPosition()+"1. DELETE instances for dataset id = "+tbId);
			cleanMDBInstance(tbId);
			
			//long annotId = tbId_annotId.getSecond();
			String annotUrl = tbId_annotUrl.getSecond();
			//if(annotId>=0)
			if(annotUrl!=null&&annotUrl.length()>0)
			{
				System.out.println(Debugger.getCallerPosition()+"2. DELETE instances for dataset  = "+dataFileName);
				//cleanMDBType(annotId);
				cleanMDBType(tbId);
			}
			Statement stmt = m_conn.createStatement();
			//String updAnnotsql = "UPDATE "+ this.m_datasetAnnotTable +" SET annot_id = NULL" +" WHERE did="+tbId;
			String updAnnotsql = "UPDATE "+ this.m_datasetAnnotTable +" SET annot_uri = NULL" +" WHERE did="+tbId;
			System.out.println(Debugger.getCallerPosition()+"3. EXECUTE: "+updAnnotsql);
			stmt.execute(updAnnotsql);
			
			//this order is important, because dataset_annotation table reference this key
			//String cleaanAnnotsql = "DELETE FROM "+ this.m_annotTable +" WHERE annot_id="+annotId;
			//System.out.println(Debugger.getCallerPosition()+"EXECUTE: "+cleaanAnnotsql);
			//stmt.execute(cleaanAnnotsql);
			
			stmt.close();
		}
		
		Statement stmt = m_conn.createStatement();
		String updDataAnnotsql = "DELETE FROM "+ this.m_datasetAnnotTable + " WHERE annot_uri is NULL AND with_rawdata='f'";
		System.out.println(Debugger.getCallerPosition()+"4. EXECUTE: "+updDataAnnotsql);
		stmt.execute(updDataAnnotsql);
		stmt.close();
		
		
		super.close();
	}
	
}
