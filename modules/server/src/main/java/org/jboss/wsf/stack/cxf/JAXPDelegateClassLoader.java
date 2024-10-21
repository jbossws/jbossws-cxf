package org.jboss.wsf.stack.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jboss.ws.common.utils.DelegateClassLoader;

public class JAXPDelegateClassLoader extends DelegateClassLoader
{
    private final ClassLoader delegate;

    private final ClassLoader parent;
    private static Set<String> skipSps = new HashSet<String>(Arrays. asList(
            "META-INF/services/javax.xml.parsers.DocumentBuilderFactory",
            "META-INF/services/javax.xml.parsers.SAXParserFactory",
            "META-INF/services/javax.xml.validation.SchemaFactory",
            "META-INF/services/javax.xml.stream.XMLEventFactory",
            "META-INF/services/javax.xml.datatype.DatatypeFactory",
            "META-INF/services/javax.xml.transform.TransformerFactory",
            "META-INF/services/javax.xml.xpath.XPathFactory"
    ));

    public JAXPDelegateClassLoader(final ClassLoader delegate, final ClassLoader parent)
    {
        super(delegate, parent);
        this.delegate = delegate;
        this.parent = parent;
    }

    @Override
    public URL getResource(final String name)
    {
        URL url = null;
        if (parent != null)
        {
            url = parent.getResource(name);
        }
        return (url == null && !skipSps.contains(name)) ? delegate.getResource(name) : url;
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<URL> getResources(final String name) throws IOException
    {
        final ArrayList<Enumeration<URL>> foundResources = new ArrayList<Enumeration<URL>>();

        if (!skipSps.contains(name)) {
            foundResources.add(delegate.getResources(name));
        }
        if (parent != null)
        {
            foundResources.add(parent.getResources(name));
        }

        return new Enumeration<URL>()
        {
            private int position = foundResources.size() - 1;

            public boolean hasMoreElements()
            {
                while (position >= 0)
                {
                    if (foundResources.get(position).hasMoreElements())
                    {
                        return true;
                    }
                    position--;
                }
                return false;
            }

            public URL nextElement()
            {
                while (position >= 0)
                {
                    try
                    {
                        return (foundResources.get(position)).nextElement();
                    }
                    catch (NoSuchElementException e)
                    {
                    }
                    position--;
                }
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public InputStream getResourceAsStream(final String name)
    {
        InputStream is = null;
        if (parent != null)
        {
            is = parent.getResourceAsStream(name);
        }
        return (is == null && !skipSps.contains(name)) ? delegate.getResourceAsStream(name) : is;
    }
    public ClassLoader getDelegate() {
        return this.delegate;
    }
}
