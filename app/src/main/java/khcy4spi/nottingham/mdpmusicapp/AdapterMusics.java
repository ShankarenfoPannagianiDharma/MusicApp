package khcy4spi.nottingham.mdpmusicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Shankarenfo on 10/23/2016.
 * ArrayAdapter for music files listview
 */

public class AdapterMusics extends ArrayAdapter<FileItem>
{
    private Context c;
    private int id;
    private List<FileItem> items;

    //standard constructor for adapter
    public AdapterMusics(Context context, int textViewResourceId,
                            List<FileItem> objects)
    {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    //get an item from the list
    public FileItem getItem(int selection)
    {return items.get(selection);}

    //customise view options
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }

        FileItem item = items.get(position);
        if(item != null)
        {
            TextView showitem = (TextView) v.findViewById(R.id.txt_songname);
            showitem.setText(item.getName());
        }
        return v;
    }
}
