package org.aksw.jena_sparql_api.cache.staging;

import java.io.InputStream;
import java.sql.SQLException;

import org.aksw.commons.collections.IClosable;
import org.aksw.jena_sparql_api.cache.extra.InputStreamProvider;


public class InputStreamProviderInputStreamClosable
    implements InputStreamProvider
{
    //private java.sql.ResultSet rs;
    private IClosable closable;
    private InputStream in;
    
    public InputStreamProviderInputStreamClosable(InputStream in, IClosable closable) {
        //this.rs = rs;
        this.in = in;
        this.closable = closable;
    }
    
    
    @Override
    public InputStream open() {
        return in;
    }
    
    @Override
    public void close() {
        if(closable != null) {
            closable.close();
        }
        //SqlUtils.close(rs);
    }
}