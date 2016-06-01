package com.conversant.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.model.MBankStatement;
import org.compiere.model.MBankStatementLine;


public class ImportBankStatements extends SvrProcess
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(ImportBankStatements.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; 
	
	/** Organization 	 */
	private int AD_Org_ID = 1000001;
	
	private BigDecimal beginningBalance = Env.ZERO;
	private BigDecimal endingBalance = Env.ZERO;
	
	@Override
	protected String doIt() throws Exception 
	{
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		Env.setContext(getCtx(), "#AD_Org_ID", AD_Org_ID);
//		Get Property File
		String envName = Ini.getAdempiereHome();
		if (envName == null)
			return "Adempiere Home not set!!!!!!!!";
		
		envName += File.separator + "BNZ.properties";
		
		File envFile = new File(envName);
		if (!envFile.exists())
			return "BNZ properties file not found!!!!!!";
		
		Properties env = new Properties();
		try
		{
			FileInputStream in = new FileInputStream(envFile);
			env.load(in);
			in.close();
		}
		catch (Exception e)
		{
			return "ERROR";
		}
		String file = env.getProperty("BNZ_DDTRANSACTION_FILE_LOCATION");
		if (file != null && file.length() == 0)
			return "Please enter file location!!!";
		
        File[] BNZDDFile = getFileList(file);
        
        if(BNZDDFile.length == 0)
        {
        	return "No transaction file found!!!!";
        }
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(BNZDDFile[0]));
			
			MBankStatement bankStatement = new MBankStatement(getCtx(),0,get_TrxName());
            bankStatement.setC_BankAccount_ID(1000000);
            bankStatement.setStatementDate(new Timestamp(System.currentTimeMillis()-24*60*60*1000));
            bankStatement.setName(new Timestamp(System.currentTimeMillis()-24*60*60*1000).toString());
            bankStatement.setBeginningBalance(Env.ZERO);
            bankStatement.setEndingBalance(Env.ZERO);
            bankStatement.setStatementDifference(Env.ZERO);
            bankStatement.save();
            
			while ((line = br.readLine()) != null)
			{
	            // use comma as separator
				String[] bankStatementArray = line.split(cvsSplitBy);
	
				if (bankStatementArray[0].equals("3"))
				{
					MBankStatementLine statementLine = new MBankStatementLine(bankStatement);
					statementLine.setDescription("BNZ Direct Debit Transaction ");
					statementLine.setDateAcct(new Timestamp(System.currentTimeMillis()-24*60*60*1000));
					statementLine.setStatementLineDate(new Timestamp(System.currentTimeMillis()-24*60*60*1000));
					statementLine.setC_Currency_ID(121);
					statementLine.setStmtAmt(new BigDecimal(bankStatementArray[3]));
					statementLine.setTrxAmt(new BigDecimal(bankStatementArray[3]));	
					String sql =null;
					if(bankStatementArray[6] != null || !bankStatementArray[6].equals(""))
					{
						String bp_ID = bankStatementArray[6].replace("\"", "");
						sql = "SELECT C_BPARTNER_ID FROM C_BPARTNER WHERE VALUE LIKE ? AND AD_CLIENT_ID = ?";
						int C_BPartner_ID = DB.getSQLValue(get_TrxName(), sql, bp_ID.trim(),AD_Client_ID);
						if (C_BPartner_ID > 0)
							statementLine.setC_BPartner_ID(C_BPartner_ID);
						statementLine.setEftMemo(bp_ID);
					}
					
					if(bankStatementArray[7] != null || !bankStatementArray[7].equals(""))
					{
						String memo = bankStatementArray[7].replace("\"", "");
						statementLine.setMemo(memo);
					}
					
					if(bankStatementArray[8] != null || !bankStatementArray[8].equals(""))
					{
						String invoice_ID = bankStatementArray[8].replace("\"", "");
						sql = "SELECT C_INVOICE_ID FROM C_INVOICE WHERE DOCUMENTNO LIKE ? AND AD_CLIENT_ID = ?";
						int C_Invoice_ID = DB.getSQLValue(get_TrxName(), sql, invoice_ID.trim(),AD_Client_ID);
						if (C_Invoice_ID > 0)
							statementLine.setC_Invoice_ID(C_Invoice_ID);
						statementLine.setEftReference(invoice_ID);
					}
					
					if(bankStatementArray[9] != null || !bankStatementArray[9].equals(""))
					{
						String eftPayee = bankStatementArray[9].replace("\"", "");
						statementLine.setEftPayee(eftPayee);
					}
					
					if(bankStatementArray.length > 14 )
					{
						if(bankStatementArray[14] != null || !bankStatementArray[14].equals(""))
						{
							String eftPayeeAccount = bankStatementArray[14].replace("\"", "");
							statementLine.setEftPayee(eftPayeeAccount);
						}
					}
					
					statementLine.save();
				}
				else if (bankStatementArray[0].equals("5"))
				{
					beginningBalance = new BigDecimal(bankStatementArray[3]);
				}
				else if (bankStatementArray[0].equals("6"))
				{
					endingBalance = new BigDecimal(bankStatementArray[3]);
				}
				else continue;

			}

			BNZDDFile[0].renameTo(new File(file+"/"+BNZDDFile[0].getName()+".DONE"));
			updateHeader(bankStatement.get_ID());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done creating Bank Statement");
		
		// TODO Auto-generated method stub
		return "Bank statement created successfully";
	}

	
	/**
	 * Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		log.log(Level.INFO, "Unknown Parameter: ");
	}

	private File[] getFileList(String dirPath)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        final String todaysDate = dateFormat.format(System.currentTimeMillis()-24*60*60*1000);
		File dir = new File(dirPath);  
		String ext = todaysDate+".TRN";
		GenericExtFilter filter = new GenericExtFilter(ext);
        
		// list out all the file name and filter by the extension
		File[] list = dir.listFiles(filter);
		return list;
    }
	
	// inner class, generic extension filter
	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}
	
	private void updateHeader(int C_BankStatement_ID)
	{
		String sql = "UPDATE C_BankStatement bs"
			+ " SET BeginningBalance= "+beginningBalance 
			+ "WHERE C_BankStatement_ID=" + C_BankStatement_ID;
		DB.executeUpdate(sql, get_TrxName());
		
		sql = "UPDATE C_BankStatement bs"
			+ " SET StatementDifference=(SELECT COALESCE(SUM(StmtAmt),0) FROM C_BankStatementLine bsl "
				+ "WHERE bsl.C_BankStatement_ID=bs.C_BankStatement_ID AND bsl.IsActive='Y') "
			+ "WHERE C_BankStatement_ID=" + C_BankStatement_ID;
		DB.executeUpdate(sql, get_TrxName());
		
		sql = "UPDATE C_BankStatement bs"
			+ " SET EndingBalance=BeginningBalance+StatementDifference "
			+ "WHERE C_BankStatement_ID=" + C_BankStatement_ID;
		DB.executeUpdate(sql, get_TrxName());
	}	//	updateHeader
}
