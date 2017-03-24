package com.conversant.process;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.model.MBankStatement;
import org.compiere.model.MBankStatementLine;
import org.apache.commons.io.filefilter.WildcardFileFilter;



public class ImportBankStatements extends SvrProcess
{
	/** Logger 																	*/
	private static CLogger log = CLogger.getCLogger(ImportBankStatements.class);
	
	/** Conversant Client														*/
	private int AD_Client_ID = 1000000; 
	
	/** Organization 	 */
	private int AD_Org_ID = 1000001; // Conversant
	
	private int p_C_Bank_ID ;
	private String p_trxType;
	
	private BigDecimal beginningBalance = Env.ZERO;
	private BigDecimal endingBalance = Env.ZERO;
	private final String INITIAL_DATE_FORMAT = "dd/MM/yy";
	private final String FINAL_DATE_FORMAT = "yyyy-MMM-dd";
	private final String COMMA_DELIMITER = ",";
	
	@Override
	protected String doIt() throws Exception 
	{
		int originalAD_Client_ID = Env.getAD_Client_ID(getCtx());
		Env.setContext(getCtx(), "#AD_Client_ID", AD_Client_ID);
		
		int originalAD_Org_ID = Env.getAD_Org_ID(getCtx());
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
	
        for(int i=0;i<BNZDDFile.length;i++)
        {
			try {	
					String[] accounts = getAccountNumbers(BNZDDFile[i]);
					
					for(int j = 0; j< accounts.length ; j++)
					{
						br = new BufferedReader(new FileReader(BNZDDFile[i]));
						MBankStatement bankStatement = new MBankStatement(getCtx(),0,get_TrxName());
						String account = accounts[j].replaceAll("[\"-]", "");
						int bankAcctId = getBankAccountId(account);
						if (bankAcctId <= 0)
							return "Invalid Bank Account";
			            bankStatement.setC_BankAccount_ID(bankAcctId);
			            if(p_trxType != null)
			            	if(p_trxType.equals("CRD"))
			            		bankStatement.setDescription("BNZ Credit Card Transaction ");
			            	else if(p_trxType.equals("CIF"))
			            		bankStatement.setDescription("BNZ Direct Debit Transaction ");
			            bankStatement.setStatementDate(new Timestamp(System.currentTimeMillis()-24*60*60*1000));
			            bankStatement.setName(new Timestamp(System.currentTimeMillis()).toString());
			            bankStatement.setBeginningBalance(Env.ZERO);
			            bankStatement.setEndingBalance(Env.ZERO);
			            bankStatement.setStatementDifference(Env.ZERO);
			            bankStatement.save();
			            
						while ((line = br.readLine()) != null)
						{
				            // use comma as separator
							String[] bankStatementArray = line.split(COMMA_DELIMITER);
				
							if (bankStatementArray[0].equals("3") && bankStatementArray[2].equals(accounts[j]))
							{
								MBankStatementLine statementLine = new MBankStatementLine(bankStatement);
								statementLine.setDateAcct(new Timestamp(System.currentTimeMillis()-24*60*60*1000));
								if(bankStatementArray[10].equals(null))
									statementLine.setStatementLineDate(new Timestamp(System.currentTimeMillis()-24*60*60*1000));
								else
								{
									String date=bankStatementArray[10].replace("\"", "");    // take a string  date
									String formattedDate=formatDate(date, INITIAL_DATE_FORMAT, FINAL_DATE_FORMAT);
									SimpleDateFormat fm = new SimpleDateFormat(FINAL_DATE_FORMAT);
									Date d = fm.parse(formattedDate);
								    if(d!=null){  // simple null check
								    	statementLine.setStatementLineDate(new Timestamp(d.getTime()));
								  }
								}	
									
								statementLine.setC_Currency_ID(getCurrencyId());
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
									statementLine.setMemo(bp_ID);
								}
								
								if(bankStatementArray[7] != null || !bankStatementArray[7].equals(""))
								{
									String memo = bankStatementArray[7].replace("\"", "");
									statementLine.setMemo(memo);
								}
								
								if(bankStatementArray[8] != null || !bankStatementArray[8].equals(""))
								{
									if(bankStatementArray[8].equalsIgnoreCase("CREDIT CARD"))
										statementLine.addDescription(bankStatementArray[8]);
									else
									{
									String invoice_ID = bankStatementArray[8].replace("\"", "");
									sql = "SELECT C_INVOICE_ID FROM C_INVOICE WHERE DOCUMENTNO LIKE ? AND AD_CLIENT_ID = ?";
									int C_Invoice_ID = DB.getSQLValue(get_TrxName(), sql, invoice_ID.trim(),AD_Client_ID);
									if (C_Invoice_ID > 0)
										statementLine.setC_Invoice_ID(C_Invoice_ID);
									statementLine.setEftReference(invoice_ID);
									}
								}
								
								if(bankStatementArray[9] != null || !bankStatementArray[9].equals(""))
								{
									String eftPayee = bankStatementArray[9].replace("\"", "");
									statementLine.setEftPayee(eftPayee);
									statementLine.setReferenceNo(eftPayee);
								}
								
								if(bankStatementArray[0].equals("3"))
								{
									String stmtIndicator = bankStatementArray[12].replace("\"", "");
									statementLine.setDescription(stmtIndicator);
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
							else if (bankStatementArray[0].equals("5") && bankStatementArray[2].equals(accounts[j]))
							{
								beginningBalance = new BigDecimal(bankStatementArray[3]);
							}
							else if (bankStatementArray[0].equals("6") && bankStatementArray[2].equals(accounts[j]))
							{
								endingBalance = new BigDecimal(bankStatementArray[3]);
							}
							else continue;
			
						}
						updateHeader(bankStatement.get_ID());
						addLog(getProcessInfo().getAD_Process_ID(), new Timestamp(System.currentTimeMillis()), null, "Bank Statment created [ " + bankStatement.getName()+" ]");

					}// for statement for each account
						BNZDDFile[i].renameTo(new File(file+"/"+BNZDDFile[i].getName()+".DONE"));
						
				
				
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
		
	}

		System.out.println("Done creating Bank Statement");
		
		Env.setContext(getCtx(), "#AD_Client_ID", originalAD_Client_ID);
		Env.setContext(getCtx(), "#AD_Org_ID", originalAD_Org_ID);
		
		// TODO Auto-generated method stub
		return "Bank statement created successfully";
	}

	
	/**
	 * Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_Bank_ID"))
			{
				p_C_Bank_ID = ((BigDecimal)para[i].getParameter()).intValue();
			}
			else if (name.equals("Transaction Type"))
			{
				p_trxType = para[i].getParameter().toString();
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}	

		log.log(Level.INFO, "Unknown Parameter: ");
	}

	private File[] getFileList(String dirPath)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        final String todaysDate = dateFormat.format(System.currentTimeMillis());
		File dir = new File(dirPath);  
		String ext = "*."+todaysDate+".TRN";
		if(p_trxType != null)
			ext = "*."+p_trxType +".*"+todaysDate+".TRN";
		FileFilter fileFilter = new WildcardFileFilter(ext);
        
		// list out all the file name and filter by the extension
		File[] list = dir.listFiles(fileFilter);
		return list;
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
	
	private int getBankAccountId(String accountNumber)
	{
		String bankSql = "SELECT C_BankAccount_ID FROM C_BankAccount c, C_Bank l WHERE c.C_Bank_ID = l.C_Bank_ID AND c.AD_Org_ID = ?";
		if(p_C_Bank_ID > 0)
			bankSql += " AND l.C_Bank_ID = "+p_C_Bank_ID;
		/*else if (p_C_Bank_ID == 0)
			bankSql+= " AND c.isDefault = 'Y'";*/
		bankSql+= " AND c.AccountNo LIKE '" + accountNumber +"'";
		int C_BankAccount_ID = DB.getSQLValue(null, bankSql, AD_Org_ID);
		return C_BankAccount_ID;
	}
	
	private int getCurrencyId()
	{
		int C_Currency_ID = DB.getSQLValue(null, "SELECT C_Currency_ID FROM AD_OrgInfo c  WHERE c.AD_Org_ID = ?", AD_Org_ID);
		if (C_Currency_ID > 0)
			return C_Currency_ID;
		
		return 121;
	}
	
	private String formatDate (String date, String initDateFormat, String endDateFormat) throws ParseException {

	    Date initDate = new SimpleDateFormat(initDateFormat).parse(date);
	    SimpleDateFormat formatter = new SimpleDateFormat(endDateFormat);
	    String parsedDate = formatter.format(initDate);
	    
	    return parsedDate;
	}
	
	private String[] getAccountNumbers(File brFile)
	{
		BufferedReader br;
		
		String line = "";
		ArrayList<String> accounts = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(brFile));
			while ((line = br.readLine()) != null)
			{
				String[] bankStatementArray = line.split(COMMA_DELIMITER);
				
				if (bankStatementArray[0].equals("5"))
				{
					accounts.add(bankStatementArray[2]);
				}
				
			}
		} 
		catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return accounts.toArray(new String[0]);
	}

}
