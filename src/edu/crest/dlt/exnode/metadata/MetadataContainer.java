/*
 * Created on Nov 25, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.crest.dlt.exnode.metadata.Metadata;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class MetadataContainer
{
    private Map<String, Metadata> metadata;

    public MetadataContainer() {
        metadata = new HashMap<String, Metadata>();
    }

    public void add( Metadata md ) {
        metadata.put( md.name, md );
    }

    public Iterator<Metadata> iterator() {
        Set<String> keySet = metadata.keySet();
        ArrayList<Metadata> list = new ArrayList<Metadata>();
        Iterator<String> i = keySet.iterator();
        String name;
        while ( i.hasNext() )
        {
            name = (String) i.next();
            list.add( metadata.get( name ) );
        }
        return (list.iterator());
    }

    public Metadata get( String name ) {
        return ((Metadata) metadata.get( name ));
    }
}