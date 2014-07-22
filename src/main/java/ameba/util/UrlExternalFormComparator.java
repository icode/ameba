package ameba.util;

import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;

/**
 * @author icode
 */
public class UrlExternalFormComparator implements Comparator<URL>, Serializable
{
    @Override
    public int compare(URL url1, URL url2)
    {
        return url1.toExternalForm().compareTo(url2.toExternalForm());
    }
}
