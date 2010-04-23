package com.conversant.webservice;

import javax.jws.WebService;

@WebService
public interface GenericWebService
{
	public StandardResponse createTrx(CreateTrxRequest createTrxRequest);
	public StandardResponse commitTrx(CommitTrxRequest commitTrxRequest);
	public StandardResponse rollbackTrx(RollbackTrxRequest rollbackTrxRequest);
}
