package org.compiere.process;

public class TestHelloWorld extends SvrProcess{


   @Override
    protected void prepare() {
        //
        
    }

    @Override
    protected String doIt() throws Exception {
        String s="Just Hello World";
        return s;
    }

}