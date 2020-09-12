package khcy4spi.nottingham.mdpmusicapp;

/**
 * Created by Shankarenfo on 10/22/2016.
 * an instance of a selectable file in the listview
 */

public class FileItem
{
    private String name;
    private String path;

    //constructor
    public FileItem(String n, String p)
    {
        name = n;
        path = p;
    }

    public String getName()
    {return name;}

    public String getPath()
    {return path;}
}
