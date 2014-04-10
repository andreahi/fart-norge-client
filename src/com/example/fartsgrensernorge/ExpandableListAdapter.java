package com.example.fartsgrensernorge;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
 
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
 
    private Activity context;
    private Map<String, List<String>> laptopCollections;
    private List<String> laptops;
    static LinkedList<String> checked;
    
 
    public ExpandableListAdapter(Activity context, List<String> laptops,
            Map<String, List<String>> laptopCollections) {
        this.context = context;
        this.laptopCollections = laptopCollections;
        this.laptops = laptops;
        checked = new LinkedList<String>();
        
    }
 
    public Object getChild(int groupPosition, int childPosition) {
        return laptopCollections.get(laptops.get(groupPosition)).get(childPosition);
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    @Override
    public long getCombinedChildId (long groupId, long childId){
    	//Log.e("test", "combined child " + (((groupId+1) << 7) | childId) + ", id: " + childId);
    	//return ((groupId+1) << 7) | childId;
    	return laptopCollections.get(laptops.get((int) groupId)).get((int) childId).hashCode();
    	//return (groupId) * 10 + childId;
    }
    @Override
    public long getCombinedGroupId (long groupId){
    	//Log.e("test", "combined group " +  ((groupId+1) << 14) + ", id: " + groupId);
    	//return ((groupId+1) << 14);
    	return laptops.get((int) groupId).hashCode();
    }
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        final String laptop = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();
        System.out.println("group pos: " + groupPosition);
        System.out.println("child pos: " + childPosition);
         {
            convertView = inflater.inflate(R.layout.child_item, null);
        }
 
        TextView item = (TextView) convertView.findViewById(R.id.laptop);
 
        final CheckBox delete = (CheckBox) convertView.findViewById(R.id.delete);
        delete.setChecked(checked.contains(laptop));
        delete.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {
            	Log.e("test", "group pos: " + groupPosition);
            	Log.e("test", "child pos: " + childPosition);

                List<String> child =
                        laptopCollections.get(laptops.get(groupPosition));
                    String name = child.get(childPosition);
                    if(delete.isChecked())
                    	checked.add(name);
                    else
                    	checked.remove(name);
                    notifyDataSetChanged();
                    /*
            	AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to remove?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                
                            }
                        });
                builder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                */
            }
        });
 
        item.setText(laptop);
        return convertView;
    }
 
    public int getChildrenCount(int groupPosition) {
        return laptopCollections.get(laptops.get(groupPosition)).size();
    }
 
    public Object getGroup(int groupPosition) {
        return laptops.get(groupPosition);
    }
 
    public int getGroupCount() {
        return laptops.size();
    }
 
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String laptopName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_item,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.laptop);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(laptopName);
        return convertView;
    }
 
    public boolean hasStableIds() {
        return false;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}