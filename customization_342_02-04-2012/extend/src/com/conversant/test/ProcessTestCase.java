package com.conversant.test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.X_AD_PInstance;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import test.AdempiereTestCase;

public class ProcessTestCase extends AdempiereTestCase
{
	protected MProcess process;
	protected ProcessInfo processInfo;

	protected String className;
	
	@Override
	protected void setUp() throws Exception 
	{
		super.setUp();
		
		// Make sure class name is set
		assertNotNull(className);

		// Create trx
//		setTrxName(Trx.createTrxName());
		
		// Create new process
		process = new MProcess(getCtx(), 0, getTrxName());
		process.setName(className);
		process.setClassname(className);		
		process.save();		
		
		// Create process info
		processInfo = new ProcessInfo("ProcessTest", process.get_ID());
	}
	
	protected boolean runProcess()
	{
		process.processIt(processInfo, Trx.get(getTrxName(), false));
		return processInfo.isError();
	}
	
	protected void createProcessInstance(HashMap<String, Object> parameters)
	{
		X_AD_PInstance processInstance = new X_AD_PInstance(getCtx(), 0, getTrxName()); // trxName ignored
		processInstance.setAD_Process_ID(process.getAD_Process_ID());
		processInstance.setRecord_ID(processInfo.getRecord_ID());
		processInstance.setAD_User_ID(Env.getAD_User_ID(getCtx()));
		processInstance.save(); 
		
		// Set process instance id in process info
		processInfo.setAD_PInstance_ID(processInstance.getAD_PInstance_ID());
		
		MProcessPara[] para = getProcessParameters(process);
		for (int i = 0; i < para.length; i++)
		{	
			MPInstancePara pip = new MPInstancePara (getCtx(), 0, getTrxName());
			pip.setSeqNo(para[i].getSeqNo());
			pip.setAD_PInstance_ID(processInstance.getAD_PInstance_ID());
			pip.setParameterName(para[i].getColumnName());
			pip.setInfo(para[i].getName());
			
			for (String key : parameters.keySet())
			{
				if (key.equals(pip.getParameterName()))
				{
					Object value = parameters.get(key);
					
					if (value instanceof String)
					{
						pip.setP_String((String)value);
					}
					else if (value instanceof Integer)
					{
						pip.setP_Number((Integer)value);
					}
					else if (value instanceof BigDecimal)
					{
						pip.setP_Number((BigDecimal)value);
					}
					else if (value instanceof Timestamp)
					{
						pip.setP_Date((Timestamp)value);
					}
					else
					{
						// TODO: Log error
					}
				}
			}
			
			pip.save();
		}
		
		// Set process info param (used in process prepare() method)
		setParameterFromDB(processInfo);
	}
	
	/**
	 *  Set Parameter of Process (and Client/User)
	 * 	@param pi Process Info
	 */
	public void setParameterFromDB (ProcessInfo pi)
	{
		ArrayList<ProcessInfoParameter> list = new ArrayList<ProcessInfoParameter>();
		String sql = "SELECT p.ParameterName,"         			    	//  1
			+ " p.P_String,p.P_String_To, p.P_Number,p.P_Number_To,"    //  2/3 4/5
			+ " p.P_Date,p.P_Date_To, p.Info,p.Info_To, "               //  6/7 8/9
			+ " i.AD_Client_ID, i.AD_Org_ID, i.AD_User_ID "				//	10..12
			+ "FROM AD_PInstance_Para p"
			+ " INNER JOIN AD_PInstance i ON (p.AD_PInstance_ID=i.AD_PInstance_ID) "
			+ "WHERE p.AD_PInstance_ID=? "
			+ "ORDER BY p.SeqNo";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, getTrxName());
			pstmt.setInt(1, pi.getAD_PInstance_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String ParameterName = rs.getString(1);
				//	String
				Object Parameter = rs.getString(2);
				Object Parameter_To = rs.getString(3);
				//	Big Decimal
				if (Parameter == null && Parameter_To == null)
				{
					Parameter = rs.getBigDecimal(4);
					Parameter_To = rs.getBigDecimal(5);
				}
				//	Timestamp
				if (Parameter == null && Parameter_To == null)
				{
					Parameter = rs.getTimestamp(6);
					Parameter_To = rs.getTimestamp(7);
				}
				//	Info
				String Info = rs.getString(8);
				String Info_To = rs.getString(9);
				//
				list.add (new ProcessInfoParameter(ParameterName, Parameter, Parameter_To, Info, Info_To));
				//
				if (pi.getAD_Client_ID() == null)
					pi.setAD_Client_ID (rs.getInt(10));
				if (pi.getAD_User_ID() == null)
					pi.setAD_User_ID(rs.getInt(12));
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
//			s_log.log(Level.SEVERE, sql, e);
		}
		//
		ProcessInfoParameter[] pars = new ProcessInfoParameter[list.size()];
		list.toArray(pars);
		pi.setParameter(pars);
	}   //  setParameterFromDB
	
	/**
	 * 	Get Parameters
	 *	@return parameters
	 */
	public MProcessPara[] getProcessParameters(MProcess process)
	{
		ArrayList<MProcessPara> list = new ArrayList<MProcessPara>();
		//
		String sql = "SELECT * FROM AD_Process_Para WHERE AD_Process_ID=? ORDER BY SeqNo";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, getTrxName());
			pstmt.setInt(1, process.getAD_Process_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MProcessPara(getCtx(), rs, getTrxName()));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
//			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		//
		MProcessPara[] m_parameters = new MProcessPara[list.size()];
		list.toArray(m_parameters);
		return m_parameters;
	}	//	getParameters
}
