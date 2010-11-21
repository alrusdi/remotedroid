package com.joshsera;

import android.content.*;
import android.database.DataSetObserver;
import android.view.*;
import android.widget.*;
import java.util.*;

public class FoundHostsAdapter implements ListAdapter {
	//
	private Vector<String> hosts;
	private Context context;
	
	public FoundHostsAdapter(Vector<String> hosts, Context context) {
		this.hosts = hosts;
		this.context = context;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return this.hosts.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return this.hosts.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View view, ViewGroup parent) {
		// 
		view = this.inflateView(view);
		TextView tv = (TextView)view.findViewById(R.id.hostEntry);
		tv.setText(this.hosts.get(position));
		return view;
	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return this.hosts.size() == 0;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}
	
	private View inflateView(View cell) {
		if (cell == null) {
			LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			cell = inflater.inflate(R.layout.savedhost, null);
		}
		return cell;
	}

	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return true;
	}
}
