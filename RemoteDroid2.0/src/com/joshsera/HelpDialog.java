package com.joshsera;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HelpDialog extends Dialog {
	
	
	public HelpDialog(Context context) {
		super(context);
		
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.help);
		this.setTitle(this.getContext().getString(R.string.txt_help));
		//
		Button but = (Button)this.findViewById(R.id.btnCloseHelp);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
